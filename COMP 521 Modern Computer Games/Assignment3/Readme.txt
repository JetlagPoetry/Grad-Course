In this assignment, task 1-5 are all implemented.
The green capsule could be controlled by player while the purple one is the AI agent.
The AI can deal with multiple conditions except for one when enemies are suddenly respawned 
on one of the doorways where the agents locate. I have omitted this circumstance on purpose
since a normal player cannot avoid it as well and the 100% surviving ai would sacrifice certain 
item collecting speed.
I have used NavMeshAgent in Unity to do the navigation. But other collision and decision making by myself.

For the HTN question, please refer to the image file HTN.jpg.
But I will list the conditions m1-m6 here in case you need to know.

m1: When there are items left and no need to teleport the player.
m2: When there are less than 4 items left and the player's current location is closer to most of them.
m3: When the enemy is close with it's face to the AI and 
	this enemy does not have a long way to the target doorway.
m4: When the enemy is close with it's back to the AI and 
	this enemy does not have a long way to the target doorway.
m5: When the enemy is close with it's back to the AI and 
	this enemy still has a long way the aimed doorway.
	(To prevent the agent from keep following the enemy's pace)
m6: When the enemy is close and blocking the way, and there are fewer items left on the side across 
	the enemy than in the other side.