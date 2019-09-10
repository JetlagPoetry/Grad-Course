package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

/**
 * @author 260860682 Jingyuan Wang
 */
public class FreeJoint extends DAGNode {

	DoubleParameter tx;
	DoubleParameter ty;
	DoubleParameter tz;
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
		
	public FreeJoint( String name ) {
		super(name);
		dofs.add( tx = new DoubleParameter( name+" tx", 0, -2, 2 ) );		
		dofs.add( ty = new DoubleParameter( name+" ty", 0, -2, 2 ) );
		dofs.add( tz = new DoubleParameter( name+" tz", 0, -2, 2 ) );
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -180, 180 ) );		
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -180, 180 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -180, 180 ) );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// TODO: Objective 1: implement the FreeJoint display method
		gl.glLoadIdentity();
		gl.glPushMatrix();
	    gl.glTranslated(0.0f,0.0f,-14.0f);              
	    gl.glTranslated(tx.getFloatValue(), ty.getFloatValue(), tz.getFloatValue());
	    gl.glRotated(rx.getFloatValue(), 1.0f, 0.0f, 0.0f);
	    gl.glRotated(ry.getFloatValue(), 0.0f, 1.0f, 0.0f);
	    gl.glRotated(rz.getFloatValue(), 0.0f, 0.0f, 1.0f);
		
		super.display(drawable);
		gl.glPopMatrix();
	}
	
}
