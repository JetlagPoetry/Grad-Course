package comp557.a1;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

/**
 * @author 260860682 Jingyuan Wang
 */
public class BallJoint extends DAGNode {
	
	private Tuple3d position;
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
	
	public BallJoint( String name) {
		super(name);
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -90, 90 ) );
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -90, 90 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -90, 90 ) );
	}
	
	public BallJoint( String name, Tuple2d xLimit, Tuple2d yLimit, Tuple2d zLimit) {
		super(name);	
		dofs.add( rx = new DoubleParameter( name+" rx", 0, xLimit==null?-90:xLimit.x, xLimit==null?90:xLimit.y ) );
		dofs.add( ry = new DoubleParameter( name+" ry", 0, yLimit==null?-90:yLimit.x, yLimit==null?90:yLimit.y ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, zLimit==null?-90:zLimit.x, zLimit==null?90:zLimit.y ) );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// TODO: Objective 1: implement the FreeJoint display method
		gl.glPushMatrix();
	    if(position!=null) gl.glTranslated(position.x, position.y, position.z);         
	    gl.glRotated(rx.getFloatValue(), 1.0f, 0.0f, 0.0f);
	    gl.glRotated(ry.getFloatValue(), 0.0f, 1.0f, 0.0f);
	    gl.glRotated(rz.getFloatValue(), 0.0f, 0.0f, 1.0f);
	    
		super.display(drawable);
		gl.glPopMatrix();
	}
	
	public void setPosition(Tuple3d position) {
		this.position = position;
	}
	
}
