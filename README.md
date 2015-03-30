Cyclop-Android-Template
=======================

Paris, December 4th 2014

This project is designed for image processing researchers and algorithms designers
to showcase their work on Android gear, without getting too much into Android development.

The "Cyclop-Android-Template" GitHub repository contains the main building blocks 
of a basic image processing app: java files, resources and the manifest. 
To be able to use them, create a new Android project (e.g. via Android Studio http://developer.android.com/sdk/index.html) and 
import or copy/paste these building blocks (*.java, /res/, AndroidManifest.xml) into the appropriate folders.
Once you're ready to go, you should only modify the "YourLab" class
and make the process() method modify the image as you wish.
The parent class is roughly a float[][][] array named "floats" with the dimensions of the image "nx" and "ny":
- floats[0] is the red channel
- floats[1] is the green channel 
- floats[2] is the blue channel
- floats[c] is a nx * ny 2-dimensional array

This project is open source.
Have fun!

Samuel O. Ronsin | Cyclop

samuel@cyclop.io

http://cyclop.io
