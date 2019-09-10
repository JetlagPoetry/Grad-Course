package comp557.a1;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.DoubleParameter;

/**
 * @author 260860682 Jingyuan Wang
 */
public class HingeJoint extends DAGNode {

	private Tuple3d position, axis;
	DoubleParameter r;
		
	public HingeJoint( String name ) {
		super(name);	
		dofs.add( r = new DoubleParameter( name+" r", 0, -90, 90 ) );
	}
	
	public HingeJoint( String name, Tuple2d limit) {
		super(name);	
		dofs.add( r = new DoubleParameter( name+" r", 0, limit==null?-90:limit.x, limit==null?90:limit.y ) );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// TODO: Objective 1: implement the FreeJoint display method
		gl.glPushMatrix();
	    gl.glTranslated(position.x, position.y, position.z);  
	    if(axis.x == 1) gl.glRotated(r.getFloatValue(), 1.0f, 0.0f, 0.0f);
	    if(axis.y == 1) gl.glRotated(r.getFloatValue(), 0.0f, 1.0f, 0.0f);
	    if(axis.z == 1) gl.glRotated(r.getFloatValue(), 0.0f, 0.0f, 1.0f);
		gl.glFlush();

		super.display(drawable);
		gl.glPopMatrix();
	}
	
	public void setPosition(Tuple3d position) {
		this.position = position;
	}

	public void setAxis(Tuple3d axis) {
		this.axis = axis;
	}
	
}
