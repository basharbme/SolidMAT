/*
 * Copyright 2018 Murat Artim (muratartim@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package element;

import java.util.Vector;

import section.Section;

import boundary.ElementTemp;
import material.Material;
import math.GaussQuadrature;
import math.Interpolation2D;
import matrix.DMat;
import matrix.DVec;
import node.Node;

/**
 * Class for Element26. Properties of element are; Mechanics: Mindlin/Reissner
 * plate, Geometric: Linear, Material: Orthotropic/Isotropic, Interpolation
 * degree: Biquadratic, Interpolation family: Lagrange, Number of nodes: 6,
 * Geometry: Triangular, Dofs (per node): ux, uy, uz, rx, ry, rz (global), u3,
 * r1, r2 (local), Mechanical loading: fx, fy, fz, mx, my, mz (global), f3, m1,
 * m2 (local), Temperature loading: no, Displacements: u3, r1, r2, Strains: e11,
 * e22, e12, e13, e23 Stresses: s11, s22, s33, s12, s13, s23, Internal forces:
 * F13, H23, K22, M11, T12. Note: Transverse shear terms are under-integrated
 * against shear locking.
 * 
 * @author Murat
 * 
 */
public class Element26 extends Element2D {

	private static final long serialVersionUID = 1L;

	/** The nodes of element. */
	private Node[] nodes_ = new Node[6];

	/** The element global dof array (per node). */
	private int[] globalDofArray_ = { Element.ux_, Element.uy_, Element.uz_,
			Element.rx_, Element.ry_, Element.rz_ };

	/** The element local dof array (per node). */
	private int[] localDofArray_ = { Element.u3_, Element.r1_, Element.r2_ };

	/**
	 * Creates Element18 element. Nodes should be given in counter-clockwise
	 * order.
	 * 
	 * @param n1
	 *            The first node of element (bottom-right).
	 * @param n2
	 *            The second node of element (top).
	 * @param n3
	 *            The third node of element (bottom-left).
	 * @param n4
	 *            The fourth node of element (between n1 and n2).
	 * @param n5
	 *            The fifth node of element (between n2 and n3).
	 * @param n6
	 *            The sixth node of element (between n3 and n1).
	 */
	public Element26(Node n1, Node n2, Node n3, Node n4, Node n5, Node n6) {
		nodes_[0] = n1;
		nodes_[1] = n2;
		nodes_[2] = n3;
		nodes_[3] = n4;
		nodes_[4] = n5;
		nodes_[5] = n6;
	}

	/**
	 * Sets new nodes to element.
	 */
	public void setNodes(Node[] nodes) {
		nodes_ = nodes;
	}

	/**
	 * Returns the type of element.
	 * 
	 * @return The type of element.
	 */
	public int getType() {
		return ElementLibrary.element26_;
	}

	/**
	 * Returns the geometry of element.
	 */
	public int getGeometry() {
		return Element2D.triangular_;
	}

	/**
	 * Returns the nodes of element.
	 * 
	 * @return The nodes of element.
	 */
	public Node[] getNodes() {
		return nodes_;
	}

	/**
	 * Returns local approximated displacements of element.
	 * 
	 * @param eps1
	 *            Natural coordinate-1.
	 * @param eps2
	 *            Natural coordinate-2.
	 * @return The local approximated displacement vector of element.
	 */
	public DVec getDisplacement(double eps1, double eps2, double eps3) {

		// get nodal unknowns
		DVec uGlobal = new DVec(36);
		DVec u1 = getNodes()[0].getUnknown(Node.global_);
		DVec u2 = getNodes()[1].getUnknown(Node.global_);
		DVec u3 = getNodes()[2].getUnknown(Node.global_);
		DVec u4 = getNodes()[3].getUnknown(Node.global_);
		DVec u5 = getNodes()[4].getUnknown(Node.global_);
		DVec u6 = getNodes()[5].getUnknown(Node.global_);
		for (int i = 0; i < 6; i++) {
			uGlobal.set(i, u1.get(i));
			uGlobal.set(i + 6, u2.get(i));
			uGlobal.set(i + 12, u3.get(i));
			uGlobal.set(i + 18, u4.get(i));
			uGlobal.set(i + 24, u5.get(i));
			uGlobal.set(i + 30, u6.get(i));
		}

		// transform into local coordinates
		DMat tr = computeTransformation();
		DVec uLocal = uGlobal.transform(tr, DMat.toLocal_);

		// get interpolation function
		Interpolation2D shapeF = getInterpolation();

		// compute local approximated displacements
		double[] disp = new double[6];
		for (int i = 0; i < nodes_.length; i++) {
			disp[2] += uLocal.get(3 * i) * shapeF.getFunction(eps1, eps2, i);
			disp[3] += uLocal.get(3 * i + 1)
					* shapeF.getFunction(eps1, eps2, i);
			disp[4] += uLocal.get(3 * i + 2)
					* shapeF.getFunction(eps1, eps2, i);
		}

		// return
		return new DVec(disp);
	}

	/**
	 * Returns the elastic strain tensor of element.
	 * 
	 * @param eps1
	 *            Natural coordinate-1.
	 * @param eps2
	 *            Natural coordinate-2.
	 * @return The elastic strain tensor of element.
	 */
	public DMat getStrain(double eps1, double eps2, double eps3) {

		// get nodal unknowns
		DVec uGlobal = new DVec(36);
		DVec u1 = getNodes()[0].getUnknown(Node.global_);
		DVec u2 = getNodes()[1].getUnknown(Node.global_);
		DVec u3 = getNodes()[2].getUnknown(Node.global_);
		DVec u4 = getNodes()[3].getUnknown(Node.global_);
		DVec u5 = getNodes()[4].getUnknown(Node.global_);
		DVec u6 = getNodes()[5].getUnknown(Node.global_);
		for (int i = 0; i < 6; i++) {
			uGlobal.set(i, u1.get(i));
			uGlobal.set(i + 6, u2.get(i));
			uGlobal.set(i + 12, u3.get(i));
			uGlobal.set(i + 18, u4.get(i));
			uGlobal.set(i + 24, u5.get(i));
			uGlobal.set(i + 30, u6.get(i));
		}

		// transform into local coordinates
		DMat tr = computeTransformation();
		DVec uLocal = uGlobal.transform(tr, DMat.toLocal_);

		// get interpolation function
		Interpolation2D shapeF = getInterpolation();

		// compute derivatives of displacements
		double u31 = 0.0, u32 = 0.0;
		double r11 = 0.0, r12 = 0.0, r21 = 0.0, r22 = 0.0, r1 = 0.0, r2 = 0.0;
		for (int i = 0; i < nodes_.length; i++) {
			u31 += uLocal.get(3 * i) * shapeF.getDer1Function(eps1, eps2, i);
			u32 += uLocal.get(3 * i) * shapeF.getDer2Function(eps1, eps2, i);
			r11 += uLocal.get(3 * i + 1)
					* shapeF.getDer1Function(eps1, eps2, i);
			r12 += uLocal.get(3 * i + 1)
					* shapeF.getDer2Function(eps1, eps2, i);
			r21 += uLocal.get(3 * i + 2)
					* shapeF.getDer1Function(eps1, eps2, i);
			r22 += uLocal.get(3 * i + 2)
					* shapeF.getDer2Function(eps1, eps2, i);
			r1 += uLocal.get(3 * i + 1) * shapeF.getFunction(eps1, eps2, i);
			r2 += uLocal.get(3 * i + 2) * shapeF.getFunction(eps1, eps2, i);
		}

		// compute geometry approximations
		double geo11 = getGeoAppx(eps1, eps2, 0, 1);
		double geo12 = getGeoAppx(eps1, eps2, 0, 2);
		double geo21 = getGeoAppx(eps1, eps2, 1, 1);
		double geo22 = getGeoAppx(eps1, eps2, 1, 2);

		// compute determinant of jacobian
		double jacDet = getJacobian(eps1, eps2).determinant();

		// get thickness
		double h = getSection().getDimension(0);

		// compute local approximated strains
		DMat strain = new DMat(3, 3);
		strain.set(0, 0, (h / 2.0 * (r21 * geo22 - r22 * geo21)) / jacDet);
		strain.set(1, 1, (h / 2.0 * (r11 * geo12 - r12 * geo11)) / jacDet);
		strain.set(0, 1, ((-r11 * geo22 + r12 * geo21 - r21 * geo12 + r22
				* geo11)
				* h / 2.0)
				/ (2.0 * jacDet));
		strain.set(0, 2, ((u31 * geo22 - u32 * geo21) / jacDet + r2) / 2.0);
		strain.set(1, 2, ((-u31 * geo12 + u32 * geo11) / jacDet - r1) / 2.0);
		strain = strain.mirror();

		// return strain tensor
		return strain;
	}

	/**
	 * Returns the Cauchy stress tensor of element.
	 * 
	 * @param eps1
	 *            Natural coordinate-1.
	 * @param eps2
	 *            Natural coordinate-2.
	 * @return The Cauchy stress tensor of element.
	 */
	public DMat getStress(double eps1, double eps2, double eps3) {

		// get material values
		Material m = getMaterial();
		DMat c = m.getC(Material.threeD_);
		DVec phi = m.getAlpha(Material.threeD_);
		phi.set(2, 0.0);

		// get thermal influences
		double theta = 0.0;
		Vector<ElementTemp> ml = getTempLoads();
		for (int i = 0; i < ml.size(); i++)
			theta += ml.get(i).getValue();

		// get strain vector
		DVec strainV = new DVec(6);
		DMat strainM = getStrain(eps1, eps2, 0.0);
		strainV.set(0, strainM.get(0, 0));
		strainV.set(1, strainM.get(1, 1));
		strainV.set(2, strainM.get(2, 2));
		strainV.set(3, 2.0 * strainM.get(1, 2));
		strainV.set(4, 2.0 * strainM.get(0, 2));
		strainV.set(5, 2.0 * strainM.get(0, 1));

		// compute stress vector
		DVec stressV = c.multiply(strainV.subtract(phi.scale(theta)));

		// construct and return stress tensor
		DMat stressM = new DMat(3, 3);
		stressM.set(0, 0, stressV.get(0));
		stressM.set(1, 1, stressV.get(1));
		stressM.set(2, 2, stressV.get(2));
		stressM.set(1, 2, stressV.get(3));
		stressM.set(0, 2, stressV.get(4));
		stressM.set(0, 1, stressV.get(5));
		stressM = stressM.mirror();
		return stressM;
	}

	/**
	 * Returns the demanded internal force of element.
	 * 
	 * @param type
	 *            The type of internal force.
	 * @param eps1
	 *            Natural coordinate-1.
	 * @param eps2
	 *            Natural coordinate-2.
	 * @return The internal force of element.
	 */
	public double getInternalForce(int type, double eps1, double eps2,
			double eps3) {

		// get constants
		Material m = getMaterial();
		DMat s = m.getS(Material.threeD_);
		double s44 = s.get(3, 3);
		double s55 = s.get(4, 4);
		double s66 = s.get(5, 5);
		double h = getSection().getDimension(0);

		// get nodal unknowns
		DVec uGlobal = new DVec(36);
		DVec u1 = getNodes()[0].getUnknown(Node.global_);
		DVec u2 = getNodes()[1].getUnknown(Node.global_);
		DVec u3 = getNodes()[2].getUnknown(Node.global_);
		DVec u4 = getNodes()[3].getUnknown(Node.global_);
		DVec u5 = getNodes()[4].getUnknown(Node.global_);
		DVec u6 = getNodes()[5].getUnknown(Node.global_);
		for (int i = 0; i < 6; i++) {
			uGlobal.set(i, u1.get(i));
			uGlobal.set(i + 6, u2.get(i));
			uGlobal.set(i + 12, u3.get(i));
			uGlobal.set(i + 18, u4.get(i));
			uGlobal.set(i + 24, u5.get(i));
			uGlobal.set(i + 30, u6.get(i));
		}

		// transform into local coordinates
		DMat tr = computeTransformation();
		DVec uLocal = uGlobal.transform(tr, DMat.toLocal_);

		// get interpolation function
		Interpolation2D shapeF = getInterpolation();

		// compute derivatives of displacements
		double u31 = 0.0, u32 = 0.0;
		double r11 = 0.0, r12 = 0.0, r21 = 0.0, r22 = 0.0, r1 = 0.0, r2 = 0.0;
		for (int i = 0; i < nodes_.length; i++) {
			u31 += uLocal.get(3 * i) * shapeF.getDer1Function(eps1, eps2, i);
			u32 += uLocal.get(3 * i) * shapeF.getDer2Function(eps1, eps2, i);
			r11 += uLocal.get(3 * i + 1)
					* shapeF.getDer1Function(eps1, eps2, i);
			r12 += uLocal.get(3 * i + 1)
					* shapeF.getDer2Function(eps1, eps2, i);
			r21 += uLocal.get(3 * i + 2)
					* shapeF.getDer1Function(eps1, eps2, i);
			r22 += uLocal.get(3 * i + 2)
					* shapeF.getDer2Function(eps1, eps2, i);
			r1 += uLocal.get(3 * i + 1) * shapeF.getFunction(eps1, eps2, i);
			r2 += uLocal.get(3 * i + 2) * shapeF.getFunction(eps1, eps2, i);
		}

		// compute geometry approximations
		double geo11 = getGeoAppx(eps1, eps2, 0, 1);
		double geo12 = getGeoAppx(eps1, eps2, 0, 2);
		double geo21 = getGeoAppx(eps1, eps2, 1, 1);
		double geo22 = getGeoAppx(eps1, eps2, 1, 2);

		// compute determinant of jacobian
		double jacDet = getJacobian(eps1, eps2).determinant();

		// initialize internal force
		double val = 0.0;

		// shear force F13 demanded
		if (type == Element2D.F13_)
			val = 5.0 * h * ((u31 * geo22 - u32 * geo21) / jacDet + r2)
					/ (6.0 * s55);

		// shear force H23 demanded
		else if (type == Element2D.H23_)
			val = 5.0 * h * ((-u31 * geo12 + u32 * geo11) / jacDet - r1)
					/ (6.0 * s44);

		// twisting moment T12 demanded
		else if (type == Element2D.T12_)
			val = h * h * h
					* (-r11 * geo22 + r12 * geo21 - r21 * geo12 + r22 * geo11)
					/ (12.0 * s66 * jacDet);

		// bending moment K22 demanded
		else if (type == Element2D.K22_) {
			DMat k = s.getSubMatrix(0, 0, 1, 1).invert();
			DMat x = new DMat(2, 1);
			x.set(0, 0, (r21 * geo22 - r22 * geo21) / jacDet);
			x.set(1, 0, (r11 * geo12 - r12 * geo11) / jacDet);
			x = x.scale(h * h * h / 12.0);
			val = k.multiply(x).get(0, 0);
		}

		// bending moment M11 demanded
		else if (type == Element2D.M11_) {
			DMat k = s.getSubMatrix(0, 0, 1, 1).invert();
			DMat x = new DMat(2, 1);
			x.set(0, 0, (r21 * geo22 - r22 * geo21) / jacDet);
			x.set(1, 0, (r11 * geo12 - r12 * geo11) / jacDet);
			x = x.scale(h * h * h / 12.0);
			val = k.multiply(x).get(1, 0);
		}

		// return internal force
		return val;
	}

	/**
	 * Returns element dof array (per node).
	 */
	public int[] getDofArray(int coordinateSystem) {

		// check coordinate system
		if (coordinateSystem < 0 || coordinateSystem > 1)
			exceptionHandler("Illegal coordinate system for dof array of element!");

		// return array
		if (coordinateSystem == Element.global_)
			return globalDofArray_;
		else
			return localDofArray_;
	}

	/**
	 * Returns interpolation function of element.
	 */
	protected Interpolation2D getInterpolation() {

		// set geometry and degree
		int geometry = Interpolation2D.triangular_;
		int degree = Interpolation2D.biquadratic_;

		// return interpolation function
		return new Interpolation2D(degree, geometry);
	}

	/**
	 * Computes element stiffness matrix.
	 * 
	 * @return Element stiffness matrix.
	 */
	protected DMat computeStiffnessMatrix() {

		// get uncondensed element stiffness matrix
		DMat kLocal = getUncondensedStiffness();

		// apply static condensation
		kLocal = kLocal.condense(18);

		// node-wise sorting of element stiffness matrix
		// sort by columns
		DMat kLocal1 = new DMat(18, 18);
		for (int i = 0; i < 18; i++)
			for (int j = 0; j < 3; j++)
				for (int k = 0; k < 6; k++)
					kLocal1.set(i, j + 3 * k, kLocal.get(i, 6 * j + k));

		// sort by rows
		DMat kLocal2 = new DMat(18, 18);
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 18; j++)
				for (int k = 0; k < 6; k++)
					kLocal2.set(i + 3 * k, j, kLocal1.get(6 * i + k, j));

		// compute global stiffness matrix
		DMat tr = computeTransformation();
		return kLocal2.transform(tr, DMat.toGlobal_);
	}

	/**
	 * Retuns element mass matrix.
	 * 
	 * @return Element mass matrix.
	 */
	protected DMat computeMassMatrix() {

		// get constants
		double ro = getMaterial().getVolumeMass();
		double h = getSection().getDimension(0);

		// compute factors
		double i0 = ro * h;
		double i2 = ro * h * h * h / 12.0;

		// compute sub-matrix k1
		DMat k1 = new DMat(6, 6);
		k1 = getSubMatrix(0, k1);

		// compute local mass matrix
		DMat mLocal = new DMat(18, 18);
		for (int i = 0; i < k1.rowCount(); i++) {
			for (int j = 0; j < k1.columnCount(); j++) {
				mLocal.set(i, j, i0 * k1.get(i, j));
				mLocal.set(i + 6, j + 6, i2 * k1.get(i, j));
				mLocal.set(i + 12, j + 12, i2 * k1.get(i, j));
			}
		}

		// node-wise sorting of element mass matrix
		// sort by columns
		DMat mLocal1 = new DMat(18, 18);
		for (int i = 0; i < 18; i++)
			for (int j = 0; j < 3; j++)
				for (int k = 0; k < 6; k++)
					mLocal1.set(i, j + 3 * k, mLocal.get(i, 6 * j + k));

		// sort by rows
		DMat mLocal2 = new DMat(18, 18);
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 18; j++)
				for (int k = 0; k < 6; k++)
					mLocal2.set(i + 3 * k, j, mLocal1.get(6 * i + k, j));

		// compute global mass matrix
		DMat tr = computeTransformation();
		return mLocal2.transform(tr, DMat.toGlobal_);
	}

	/**
	 * Computes element stability matrix.
	 * 
	 * @return Element stability matrix.
	 */
	protected DMat computeStabilityMatrix() {

		// get thickness
		double h = getSection().getDimension(Section.thickness_);

		// set number of Gauss points
		int nog = 7;

		// create Quadrature
		GaussQuadrature q = new GaussQuadrature(nog,
				GaussQuadrature.twoDimensional_);
		q.setGeometry(GaussQuadrature.triangle_);

		// loop over Gauss points
		DMat gLocal = new DMat(18, 18);
		for (int i = 0; i < nog; i++) {

			// get weight factor and supporting points
			double alpha = q.getWeight(i);
			double supp1 = q.getSupport1(i);
			double supp2 = q.getSupport2(i);

			// get jacobian and compute determinant
			DMat jac = getJacobian(supp1, supp2);
			double jacDet = jac.determinant();

			// compute initial stress matrix
			DMat sm = computeInitialStress(supp1, supp2);

			// get G operator and its transpose
			DMat gop = computeGop(supp1, supp2, jac);
			DMat gopTr = gop.transpose();

			// compute function to be integrated
			DMat func = gopTr.multiply(sm.multiply(gop));
			func = func.scale(0.5 * alpha * h * jacDet);

			// add to stiffness matrix
			gLocal = gLocal.add(func);
		}

		// compute global stability matrix
		DMat tr = computeTransformation();
		return gLocal.transform(tr, DMat.toGlobal_);
	}

	/**
	 * Returns element thermal load vector.
	 * 
	 * @return Element thermal load vector.
	 */
	protected DVec computeTempLoadVector() {
		DVec tLoad1 = new DVec(18);
		DMat tr = computeTransformation();
		return tLoad1.transform(tr, DMat.toGlobal_);
	}

	/**
	 * Returns the demanded stiffness sub-matrix.
	 * 
	 * @param index
	 *            The index of demanded sub-matrix. 0 -> k1, 1 -> k2, 2 -> k3, 3 ->
	 *            k4, 4 -> k5, 5 -> k6.
	 * @param kSub
	 *            The sub-matrix to be returned.
	 * @return The demanded stiffness sub-matrix.
	 */
	private DMat getSubMatrix(int index, DMat kSub) {

		// get shape function
		Interpolation2D intF = getInterpolation();

		// create Quadrature
		int nog = 7;
		if (index == 3 || index == 4 || index == 5)
			nog = 4;
		int dim = GaussQuadrature.twoDimensional_;
		GaussQuadrature q = new GaussQuadrature(nog, dim);
		q.setGeometry(GaussQuadrature.triangle_);

		// loop over rows of sub-matrix
		for (int i = 0; i < kSub.rowCount(); i++) {

			// loop over columns of sub-matrix
			for (int j = 0; j < kSub.columnCount(); j++) {

				// loop over Gauss points
				for (int k = 0; k < nog; k++) {

					// get weight factor and supports
					double alpha = q.getWeight(k);
					double supp1 = q.getSupport1(k);
					double supp2 = q.getSupport2(k);
					double value = 0.0;

					// sub-matrix k1-k4
					if (index == 0 || index == 3) {

						// get jacobian and compute determinant
						DMat jac = getJacobian(supp1, supp2);
						double jacDet = jac.determinant();

						// get values of functions
						double ni = intF.getFunction(supp1, supp2, i);
						double nj = intF.getFunction(supp1, supp2, j);

						// compute integration
						value = 0.5 * alpha * ni * nj * jacDet;
					}

					// sub-matrix k2-k5
					else if (index == 1 || index == 4) {

						// get values of shape functions
						double ni = intF.getFunction(supp1, supp2, i);
						double nj1 = intF.getDer1Function(supp1, supp2, j);
						double nj2 = intF.getDer2Function(supp1, supp2, j);

						// get geometry approximations
						double geo1 = getGeoAppx(supp1, supp2, 1, 1);
						double geo2 = getGeoAppx(supp1, supp2, 1, 2);

						// compute integration
						value = 0.5 * alpha * ni * (nj1 * geo2 - nj2 * geo1);
					}

					// sub-matrix k3-k6
					else if (index == 2 || index == 5) {

						// get values of shape functions
						double ni = intF.getFunction(supp1, supp2, i);
						double nj1 = intF.getDer1Function(supp1, supp2, j);
						double nj2 = intF.getDer2Function(supp1, supp2, j);

						// get geometry approximations
						double geo1 = getGeoAppx(supp1, supp2, 0, 1);
						double geo2 = getGeoAppx(supp1, supp2, 0, 2);

						// compute integration
						value = 0.5 * alpha * ni * (-nj1 * geo2 + nj2 * geo1);
					}

					// add value to sub-matrix
					kSub.add(i, j, value);
				}
			}
		}

		// return sub-matrix
		return kSub;
	}

	/**
	 * Returns uncondensed element stiffness matrix.
	 * 
	 * @return The uncondensed element stiffness matrix.
	 */
	private DMat getUncondensedStiffness() {

		// get constants
		Material m = getMaterial();
		DMat s = m.getS(Material.threeD_);
		double s11 = s.get(0, 0);
		double s22 = s.get(1, 1);
		double s44 = s.get(3, 3);
		double s55 = s.get(4, 4);
		double s66 = s.get(5, 5);
		double s12 = s.get(0, 1);
		double h = getSection().getDimension(0);

		// compute factors
		double lambda = -6.0 * s55 / (5.0 * h);
		double beta = -6.0 * s44 / (5.0 * h);
		double curlPhi = -12.0 * s11 / (h * h * h);
		double capLamb = -12.0 * s12 / (h * h * h);
		double mu = -12.0 * s22 / (h * h * h);
		double kappa = -12.0 * s66 / (h * h * h);

		// compute sub-matrix k1
		DMat k1 = new DMat(6, 6);
		k1 = getSubMatrix(0, k1);

		// compute sub-matrix k2
		DMat k2 = new DMat(6, 6);
		k2 = getSubMatrix(1, k2);

		// compute sub-matrix k3
		DMat k3 = new DMat(6, 6);
		k3 = getSubMatrix(2, k3);

		// compute sub-matrix k4 (under-integrated)
		DMat k4 = new DMat(6, 6);
		k4 = getSubMatrix(3, k4);

		// compute sub-matrix k5 (under-integrated)
		DMat k5 = new DMat(6, 6);
		k5 = getSubMatrix(4, k5);

		// compute sub-matrix k6 (under-integrated)
		DMat k6 = new DMat(6, 6);
		k6 = getSubMatrix(5, k6);

		// insert sub-matrices to stiffness matrix
		DMat kLocal = new DMat(48, 48);
		for (int i = 0; i < k1.rowCount(); i++) {
			for (int j = 0; j < k1.columnCount(); j++) {
				kLocal.set(i, j, lambda * k1.get(i, j));
				kLocal.set(i, j + 30, k5.get(i, j));
				kLocal.set(i, j + 42, k4.get(i, j));
				kLocal.set(i + 6, j + 6, beta * k1.get(i, j));
				kLocal.set(i + 6, j + 30, k6.get(i, j));
				kLocal.set(i + 6, j + 36, -k4.get(i, j));
				kLocal.set(i + 12, j + 12, curlPhi * k1.get(i, j));
				kLocal.set(i + 12, j + 18, capLamb * k1.get(i, j));
				kLocal.set(i + 12, j + 42, k2.get(i, j));
				kLocal.set(i + 18, j + 18, mu * k1.get(i, j));
				kLocal.set(i + 18, j + 36, -k3.get(i, j));
				kLocal.set(i + 24, j + 24, kappa * k1.get(i, j));
				kLocal.set(i + 24, j + 36, -k2.get(i, j));
				kLocal.set(i + 24, j + 42, k3.get(i, j));
			}
		}
		kLocal = kLocal.mirror();

		// return uncondensed stiffness matrix
		return kLocal;
	}

	/**
	 * Computes and returns element G operator.
	 * 
	 * @param eps1
	 *            Natural coordinate-1.
	 * @param eps2
	 *            Natural coordinate-2.
	 * @param jac
	 *            The jacobian matrix.
	 * @return Element G operator.
	 */
	private DMat computeGop(double eps1, double eps2, DMat jac) {

		// compute determinant of jacobian
		double jacDet = jac.determinant();

		// get interpolation of element
		Interpolation2D intF = getInterpolation();

		// compute and return G operator matrix
		DMat gop = new DMat(2, 18);

		// loop over nodes of element
		for (int i = 0; i < 6; i++) {

			// compute nodal G operator
			DMat gopNode = new DMat(2, 3);
			double der1 = intF.getDer1Function(eps1, eps2, i);
			double der2 = intF.getDer2Function(eps1, eps2, i);
			double val1 = jac.get(1, 1) * der1 - jac.get(0, 1) * der2;
			double val2 = -jac.get(1, 0) * der1 + jac.get(0, 0) * der2;
			gopNode.set(0, 0, val1);
			gopNode.set(1, 0, val2);
			gopNode.set(2, 1, val1);
			gopNode.set(3, 1, val2);
			gopNode = gopNode.scale(1.0 / jacDet);

			// set nodal G operator to element G operator
			gop = gop.setSubMatrix(gopNode, 0, 3 * i);
		}

		// return element G operator
		return gop;
	}
}
