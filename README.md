# Android Augmented Reality GPS Points

**Author: Alexandre Louisnard alouisnard@gmail.com**  
**Android Java Library**  
**2017** 

## DESCRIPTION
Show points with GPS localization on the device camera preview.  

**Functionalities:** 
* Show markers with the name of the point, its altitude and its distance from the user on a camera preview background.
* Import GPX files.

## USAGE
**argps**: the library  
**argpsapp** module: a sample application

### Library usage :
**Gradle dependency**  

* Add the following to your project level `build.gradle`:
```gradle
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```
* Add this to your app `build.gradle`:
```gradle
dependencies {
    compile 'com.github.AlexandreLouisnard:android-augmented-reality-gps-points:master-SNAPSHOT'
}
```

You can extend AugmentedRealityActivity to get a simple example.

See JAVADOC for the rest.

## CHANGELOG

## BACKLOG
