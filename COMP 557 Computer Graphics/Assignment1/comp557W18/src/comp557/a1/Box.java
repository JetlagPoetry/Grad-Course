package comp557.a1;

import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * @author 260860682 Jingyuan Wang
 */
public class Box extends DAGNode {
	
	private static GLUT glut = new GLUT();
	private Tuple3d centre, color, scale;

	//uniformed geometry DAGNode
	public Box( String name ) {
		super(name);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// TODO: Objective 1: implement the FreeJoint display method
		gl.glPushMatrix();
		if(centre!=null) gl.glTranslated(centre.x, centre.y, centre.z);
		if(color!=null) gl.glColor3d(color.x, color.y, color.z);
		if(scale!=null) gl.glScaled(scale.x, scale.y, scale.z);
		glut.glutSolidCube(1.0f);
		gl.glFlush();
		
		super.display(drawable);
		gl.glPopMatrix();
	}
	
	public void setCentre(Tuple3d centre) {
		this.centre = centre;
	}

	public void setColor(Tuple3d color) {
		this.color = color;
	}

	public void setScale(Tuple3d scale) {
		this.scale = scale;
	}
	
}
