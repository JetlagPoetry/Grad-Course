package comp557.a2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JPanel;

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Class for managing the drawing of a moderately interesting scene.
 * 
 * @author kry
 */
public class Scene {

	private GLUT glut = new GLUT();

	public Scene() {
		// do nothing
	}
	
    // The scene parameters are for adjusting how the scene looks, e.g., put it in front or behind the screen rectangle.
    
    private DoubleParameter sceneDisplacementFromScreen = new DoubleParameter("scene displacement", -0.01, -2, 1);    
    private DoubleParameter sceneScale = new DoubleParameter("scene scale", 0.01, 0.001, 0.1);    
    private DoubleParameter sceneTilt = new DoubleParameter("scene tilt", 10, -90, 90 );
    private DoubleParameter sceneRotate = new DoubleParameter("scene rotate", 0, -90, 90 );

    private DoubleParameter x = new DoubleParameter("light x", 2, -5, 5);
    private DoubleParameter y = new DoubleParameter("light y", 5, 0, 5);
    private DoubleParameter z = new DoubleParameter("light z", 5, -5, 10);

    private DoubleParameter a = new DoubleParameter("attenuation a (constant)", 1, 0, 1);
    private DoubleParameter b = new DoubleParameter("attenuation b (linear)", 0, 0, .1);
    private DoubleParameter c = new DoubleParameter("attenuation c (quadratic)", 0, 0, .1);
      
	private final float[] white = {1,1,1,1};
	private final float[] grey = {0.75f,0.75f,0.75f,1f};

	private final float[] black = {0,0,0,1};
	private float[][] colours = new float[][] {
		{.75f,1,1,1},
		{1,.75f,1,1},
		{1,1,.75f,1},
		{.75f,.75f,1,1},
		{1,.75f,.75f,1},
		{0.25f,0.25f,0.25f,1},
		{.75f,1,.75f,1}
	};

    private void setLights( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();
		gl.glEnable( GL2.GL_LIGHTING );
		gl.glEnable( GL2.GL_LIGHT0 );
		// WATCH OUT: need to provide homogeneous coordinates to many calls!! 
		float[] lightPosition = {x.getFloatValue(),y.getFloatValue(),z.getFloatValue(),1}; 
		float[] dark = new float[] {0.1f,0.1f,0.1f,1};
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0 );
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_DIFFUSE, white, 0 );
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_AMBIENT, black, 0 );
		gl.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, dark, 0 );
        gl.glLightf( GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, a.getFloatValue() ); 
        gl.glLightf( GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, b.getFloatValue() );
        gl.glLightf( GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, c.getFloatValue() ); 
    }
    
    /** We'll use a display list to speed up rendering */
    private int list = -1;
    
    /**
     * Draws a test scene.  Position and size are adjustable with the controls.
     * @param drawable
     */
    public void display( GLAutoDrawable drawable ) {
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glPushMatrix();
        // put the scene behind the screen
        gl.glTranslated( 0,0, sceneDisplacementFromScreen.getValue());
        gl.glRotated( sceneTilt.getValue(), 1,0,0);
        gl.glRotated( sceneRotate.getValue(), 0, 1, 0 );
        double scale = sceneScale.getValue();
        gl.glScaled( scale,scale,scale );
        
        setLights( drawable );
        
        // Now draw all the geometry in the scene
                
        if ( list != -1 ) {
        	gl.glCallList(list);
        } else {
        	list = gl.glGenLists(1);
        	gl.glNewList(list, GL2.GL_COMPILE_AND_EXECUTE );
        
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, white, 0 );
	        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 127 );
	        
	        for ( int i = -20; i < 20; i++ ) {
	        	for ( int j = -20; j <= 10; j++ ) {
	                gl.glBegin( GL2.GL_QUAD_STRIP );
	                gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ((i+j)%2)==0?grey:white, 0 );
	                gl.glNormal3f(0,1,0);
	                gl.glVertex3d( i, -1, j );
	                gl.glVertex3d( i, -1, j+1 );
	                gl.glVertex3d( i+1, -1, j );
	                gl.glVertex3d( i+1, -1, j+1 );        
	                gl.glEnd();
	        	}
	        }
	        
	        gl.glTranslated(-3.5,0,3.5);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[0], 0 );
	        gl.glPushMatrix();                
	        gl.glRotated( -10, 0,1,0);
	        gl.glTranslated( 0,-.25,0);
	        glut.glutSolidTeapot(1);
	        gl.glPopMatrix();
	        
	        gl.glTranslated(0.85,0,-3);
	        gl.glRotated(-30,0,1,0);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[1], 0 );
	        glut.glutSolidCylinder(1, 1, 20, 20);
	        
	        gl.glTranslated(0.85,0,-3);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[2], 0 );
	        glut.glutSolidDodecahedron();
	
	        gl.glTranslated(0.85,0,-3);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[3], 0 );
	        glut.glutSolidRhombicDodecahedron();
	        
	        gl.glTranslated(0.85,0,-3);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[4], 0 );
	        glut.glutSolidCone(1, 2, 10, 10);
	        
	        gl.glTranslated(0.85,0,-3);
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[5], 0 );
	        glut.glutSolidSphere(1, 20, 20 );
	        
	        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[6], 0 );
	
	        gl.glTranslated( 1,0,-3 );
	        gl.glRotated(-45,0,1,0);
	        glut.glutSolidTorus( 0.25, 0.75, 10, 25 );	
	        
	        gl.glDisable( GL2.GL_LIGHTING );
	        gl.glPopMatrix();
	
	    	gl.glEndList();
	    }
    }
    
    public JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( sceneTilt.getSliderControls(false));       
        vfp.add( sceneRotate.getSliderControls(false));
        vfp.add( sceneScale.getSliderControls(true ) );
        vfp.add ( sceneDisplacementFromScreen.getSliderControls(false) );
        vfp.add( a.getSliderControls(false));
        vfp.add( b.getSliderControls(false));
        vfp.add( c.getSliderControls(false));
        vfp.add( x.getSliderControls(false));
        vfp.add( y.getSliderControls(false));
        vfp.add( z.getSliderControls(false));
    	return vfp.getPanel();
    }
}
