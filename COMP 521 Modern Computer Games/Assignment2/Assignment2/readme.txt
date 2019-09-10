This is readme file for Assignment2 by Jingyuan Wang.

This scenario is implemented by Unity2d, and all scripts are in folder Assignment2/Assets/Scripts.
Please open with Unity and run the program by pressing 'Play' as usual.
Below are descriptions for each script file:

1.Cannon.cs:	Cannon rotation and firing.
2.Cannonball.cs:	All physical behaviour, affected by wind and gravity and other collide force.
			Collision detection and resolutions with mountain and wall.
			Collision detection and certain effect with ground, screen bounds and turkeys.
3.Clouds.cs:	SpriteRenderer, affected by wind force.
4.Mountain.cs:	Implemented by LineRenderer, randomly generated.
5.Turkey.cs:	All movement with turkeys, including collision with ground, wall and mountain, etc.
		Affected by cannonball if is hitten.
		I set parameters to avoid the turkey distort too much, 
		but the line segments were transformed when it move fast as you can see.
6.TurkeyCreator.cs:	Generate new turkey if last one hit the right part of the mountain.
7.TurkeyEye.cs:	Drawing the turkeyEye, move with the center point of the turkey.
8.Wind.cs:	Randomly generated wind force.

Please also refer to constraints.jpg in this folder, drawing the constraints that keeps the turkey in shape.
In fact, in this project I maintained constraints between every keypoint of the turkey and the center point.