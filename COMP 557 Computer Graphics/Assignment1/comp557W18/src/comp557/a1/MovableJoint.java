package comp557.a1;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

/**
 * @author 260860682 Jingyuan Wang
 */
public class MovableJoint extends DAGNode {
	
	private Tuple3d position;
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
	
	public MovableJoint( String name) {
		super(name);
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -2, 2 ) );
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -2, 2 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -2, 2 ) );
	}
	
	public MovableJoint( String name, Tuple2d xLimit, Tuple2d yLimit, Tuple2d zLimit) {
		super(name);	
		dofs.add( rx = new DoubleParameter( name+" rx", 0, xLimit==null?-2:xLimit.x, xLimit==null?2:xLimit.y ) );
		dofs.add( ry = new DoubleParameter( name+" ry", 0, yLimit==null?-2:yLimit.x, yLimit==null?2:yLimit.y ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, zLimit==null?-2:zLimit.x, zLimit==null?2:zLimit.y ) );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// TODO: Objective 1: implement the FreeJoint display method
		gl.glPushMatrix();
	    if(position!=null) gl.glTranslated(position.x, position.y, position.z);         
	    gl.glTranslated(rx.getFloatValue(), ry.getFloatValue(), rz.getFloatValue());
		super.display(drawable);
		gl.glPopMatrix();
	}
	
	public void setPosition(Tuple3d position) {
		this.position = position;
	}
	
}
