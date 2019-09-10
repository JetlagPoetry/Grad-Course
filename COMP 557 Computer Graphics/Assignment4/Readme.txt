In this assignment, I have implemented all required questions, i.e. 1-10.
And for the remaining marks, I chose the following options:
Mirror Reflection, Refraction, Depth of field blur, Area lights, Quadrics and multi-threaded
parallelization, which should be 4 marks in all.

All codes are provided in the src folder and basic xml files are run again after coding, so 
the images should be real and correct output of my project. You can check xml files in either
'xml files'folder or 'a4data'folder within the project and the output images are in 'png images'
as well as root folder of project 'comp557A4', either is ok. Below I will expain some special
images and xml files as well as notable features.

Question 10: Novel Scene
I have created a scene with a man figure and some geometries. All kinds of geometry are included
(For meshes, I have imported the tetrahedron meshes from Assignment3) and all features mentioned
above are set up.
Please see NovelScene.xml and NovelScene.png

Question11: Mirror Reflection
Implemented a basic mirror reflection. I have modified the parser so with ' type="mirror" ', the
scene will see this material as performing reflection. A constant coefficient of 0.8 was added to
the reflection color(a variable in Scene.java).
I have slightly changed the material of object in 2 given xml files, so please see file:
TwoSpheresPlane-Reflection.xml & TwoSpheresPlane-Reflection.png
Cornell-Reflection.xml & Cornell-Reflection.png

Question11: Refraction
Same as last objective. Need to specify a material with type="refraction" with a default refractivity
of 0.8.
See Cornell-Refraction.xml & Cornell-Refraction.png

Question11: Depth of blur
Find the corresponding point on the focal plane to each pixel and do a re-raycast with each eye offset.
I have used FastPossionDisk to random eye samples. To run this, need to give attributes to scene tag.
'blur="true" blursample="xx" focallength="xx"', seperately indicating to create depth of blur effect,
specifying eye amounts and focal length.
See BoxStacks-DepthBlur.xml & BoxStacks-DepthBlur.png

Question11: Area lights
Change the type of lights to direction to show this feature. Can give a certain 'sourcecount' to this
Light to adjust number of lights in this 2*2 square on the y-plane of this 'from' point.
See TwoSpheresPlane-Arealight.xml & TwoSpheresPlane-Arealight.png

Question11: Quadrics
Created a new Intersectable class Quadrics. In this project, implemented a paraboloid.
Please see Quadrics.xml & Quadrics.png & Quadric.java

Question11: Multi-threaded parallelization
Used java features to make the rendering process multi-threaded.
Can specify thread amounts with a 'thread="xx"' attr in scene tag.
You can check it with BoxStacks-MultiThread.xml (Some of my xml files are all set with this option)
BoxStacks-MultiThread.png is the result output of this xml file, but the image has no difference with 
the original one.

Due to the implementation of multi-threaded programme, I have changed the code so that the image can
only be saved to png files after we close the pop-out window.

If you have any question, please contact me with mcgill email:
jingyuan.wang2@mail.mcgill.ca

Thank you!