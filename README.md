# SolidMAT
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![HitCount](http://hits.dwyl.io/muratartim/SolidMAT.svg)](http://hits.dwyl.io/muratartim/SolidMAT)
[![Java Version](https://img.shields.io/badge/java-8-orange.svg)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

SolidMAT is an object-oriented software system which has been designed & developed for finite element and lifetime analysis of small-to-medium scaled structural applications. The user can configure, start and monitor the different successive steps involved in analysis procedures. The major features of the software package can be highlighted as follows,
- Preprocessing (discretization and mesh generation, material information, boundary conditions and etc.),
- Processing (starting and monitoring the demanded analysis type),
- Postprocessing (displaying the resulting values in various formats such as tables, 2D and 3D plots and etc.).

## Showcases
The slides below showcase some of the real-life benchmark problems analyzed using SolidMAT.

![solidmat_demo](https://user-images.githubusercontent.com/13915745/41032662-5efa0010-6984-11e8-8db4-c20e8c48f7bd.gif)

## Screenshot
![sc3](https://user-images.githubusercontent.com/13915745/40977697-f7a41cf0-68d1-11e8-8f38-0fa9070e04fc.jpg)

## Development languages
The software is mainly written in Java (90%), including user interfaces, analysis modules and data visualisations. Some of the linear equation solvers are written in Fortran and comipled into standalone executables which are called by the main application when needed. 

Some C++ libraries are included into the developed software package in order to be used in fatigue analysis. Because whole software package has been developed in Java programming language, native interfaces needed to be implemented to establish connection between the package and the libraries. This has been realized by the use of Java Native Interface (JNI).

## Software architecture
Following sub-sections detail the overall software architecture employed for the development process.

### Package structure
Project has been partitioned into four major packages for simultaneous development. The <b>fea</b> package contains packages necessary for the Finite Element Analysis, the <b>mesh</b> package contains automatic mesh generation classes, the <b>output</b> package includes classes for visualizer and table output writer classes, respectively. The <b>gui</b> package contains all the user interface classes.

![package](https://user-images.githubusercontent.com/13915745/41026852-dfe34f7e-6975-11e8-8a26-34979304a14a.jpg)

### UML class diagrams
The overall UML diagram for the <b>fea</b> package is shown in the following figure.

![uml_fea](https://user-images.githubusercontent.com/13915745/41027166-a27ac2ba-6976-11e8-8947-216fad56834d.JPG)

Overall UML diagram for the <b>output</b> package is shown in the figure below. The <b>visualize</b> package contains two fundamental classes called <b>PreVisualizer</b> and <b>PostVisualizer</b> which serve as connection hubs to more specific visualizer classes depending on the object type to be visualized.

![uml_visualize](https://user-images.githubusercontent.com/13915745/41027475-6fc14d52-6977-11e8-9868-38efb37b5d16.JPG)

## Software usage
Following sub-sections detail some of the important feautures of the software.

### Analysis types
The software system enables the user to perform different linear analyses of the following types:
- Linear static (displacement & stress),
- Modal (free vibration),
- Linear transient (time history),
- Linear buckling (stability),
- Fatigue (lifetime).

Analysis type to be performed can be selected via the following dialog through user interface.

![screen shot 2018-06-06 at 11 17 19](https://user-images.githubusercontent.com/13915745/41029325-b0ebbf7a-697b-11e8-9447-c12c57132190.png)

### Element types
Software package contains an extensive element library. All element types are given with different classifications in the following table.

![eltypes](https://user-images.githubusercontent.com/13915745/41027758-3555a568-6978-11e8-98aa-8be03fda7f01.jpg)

An element navigator dialog has been implemented for selecting and creating the demanded element types (see figure below). This dialog enables the user to easily navigate through elements by properties such as mechanics, geometry and interpolation degree.

![elnav](https://user-images.githubusercontent.com/13915745/41027929-90452840-6978-11e8-9ba2-56c3172c768c.jpg)

### Equation solvers
A variety of equation solvers have been implemented and included from linear algebra packages (such as MTJ - Matrix Toolkits for Java and JLAPACK – Linear Algebra Package for Java) to the software package in order to provide efficient solutions for different kinds of problems to be analyzed. These solvers are collected under two major types as linear equation solvers and generalized eigenvalue solvers, respectively.

![solvers](https://user-images.githubusercontent.com/13915745/41028174-200788f6-6979-11e8-8b86-1f9976fe8714.jpg)

Depending on the analysis type, user can select a suitable solver through the solver library dialog shown in the following figure.

![solvernav](https://user-images.githubusercontent.com/13915745/41028348-7d859a9a-6979-11e8-8ba0-28dcc0f5ede6.jpg)

### Mesh generators
Mesh generators available in the software package are composed of element sub dividers for elements having line, quadrilateral, triangular and hexahedral geometries. Figure below shows a dialog to mesh solids and some examples of meshed areas and solids.

![mesh](https://user-images.githubusercontent.com/13915745/41028522-eb4db080-6979-11e8-912c-38aac66d89bc.jpg)
