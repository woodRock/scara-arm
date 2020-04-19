# ENGR110-SCARA
![Java CI](https://github.com/woodRock/scara-arm/workflows/Java%20CI/badge.svg)

## Details
The project includes firmware to simulate and control a robotic arm. The arm was controlled using a RasberryPi through PWM.

## Maven
This project is a refactored and production version of a first year engineering project. The Maven framework has been added to the project. This allows the project to easily be installed and tested on other machines.

### Configuration
Before we install the jar, we must add the _ecs100_ 3rd party jar.

```bash 
mvn install:install-file -Dfile=../ecs100.jar -DgroupId=nz.ac.ecs -DartifactId=ecs100 -Dversion=1.0 -Dpackaging=jar
```
This maven command must be run in the _JavaSimulation_ directory.


### Installation
This command creates two jars in the target folder.

```bash 
mvn clean install
```
This maven command must be run in the _JavaSimulation_ directory.


### Run
To run the application we need to use the jar with dependencies.

The _scara-X.Y-jar-with-dependencies.jar_ is located in the target folder.
```bash 
java -jar target/scara-2.0-jar-with-dependencies.jar
```

This maven command must be run in the _JavaSimulation_ directory.

