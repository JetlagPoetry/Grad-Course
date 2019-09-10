In this assignment, I have successfully implemented steps 1-7.
Below are some specific choices in my project.
1.In the project submited, I did not use glPointSize to show the optimal edge collapse location.
2.After a new optimal vertex is created, its Q is set to Qi+Qj, with i and j refer to the 2 vertices of the halfedge collapsed.
3.My currentHE in MeshSimplificationApp.java is always set to the next collapsiable halfedge, 
	which means SHIFT+C can set every geometry to a tetrahedron and also makes pressing G not to useful.
4.Before running a collapse, I redo every halfedge in the redolist, to prevent further problems when collapsing.

For the bonus question, I have changed the constructor to successfully show the monkey. 
Did not deal with boundaries in collapse and undo redo steps.