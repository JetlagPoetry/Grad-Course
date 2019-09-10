package comp557.a2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.ControlFrame;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;
import mintools.viewer.Interactor;
import mintools.viewer.TrackBallCamera;

/**
 * Assignment 2 - depth of field blur, and anaglyphys
 * 
 * For additional information, see the following paper, which covers
 * more on quality rendering, but does not cover anaglyphs.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 * GPU Gems [2007] has a slightly more recent survey of techniques.
 *
 * @author Jingyuan Wang
 */
public class A2App implements GLEventListener, Interactor {

	/** TODO: Put your name in the window title */
	private String name = "Comp 557 Assignment 2 - JINGYUAN WANG";
	
    /** Viewing mode as specified in the assignment */
    int viewingMode = 1;
        
    /** eye Z position in world coordinates */
    private DoubleParameter eyeZPosition = new DoubleParameter( "eye z", 0.5, 0.25, 3 ); 
    /** near plane Z position in world coordinates */
    private DoubleParameter nearZPosition = new DoubleParameter( "near z", 0.25, -0.2, 0.5 ); 
    /** far plane Z position in world coordinates */
    private DoubleParameter farZPosition  = new DoubleParameter( "far z", -0.5, -2, -0.25 ); 
    /** focal plane Z position in world coordinates */
    private DoubleParameter focalPlaneZPosition = new DoubleParameter( "focal z", 0, -1.5, 0.4 );     
    
    /** Samples for drawing depth of field blur */    
    private IntParameter samples = new IntParameter( "samples", 5, 1, 100 );   
    
    /** 
     * Aperture size for drawing depth of field blur
     * In the human eye, pupil diameter ranges between approximately 2 and 8 mm
     */
    private DoubleParameter aperture = new DoubleParameter( "aperture size", 0.003, 0, 0.01 );
    
    /** x eye offsets for testing (see objective 4) */         
    private DoubleParameter eyeXOffset = new DoubleParameter("eye offset in x", 0.0, -0.3, 0.3);
    /** y eye offsets for testing (see objective 4) */
    private DoubleParameter eyeYOffset = new DoubleParameter("eye offset in y", 0.0, -0.3, 0.3);
    
    private BooleanParameter drawCenterEyeFrustum = new BooleanParameter( "draw center eye frustum", true );    
    
    private BooleanParameter drawEyeFrustums = new BooleanParameter( "draw left and right eye frustums", true );
    
	/**
	 * The eye disparity should be constant, but can be adjusted to test the
	 * creation of left and right eye frustums or likewise, can be adjusted for
	 * your own eyes!! Note that 63 mm is a good inter occular distance for the
	 * average human, but you may likewise want to lower this to reduce the
	 * depth effect (images may be hard to fuse with cheap 3D colour filter
	 * glasses). Setting the disparity negative should help you check if you
	 * have your left and right eyes reversed!
	 */
    private DoubleParameter eyeDisparity = new DoubleParameter("eye disparity", 0.063, -0.1, 0.1 );

    private GLUT glut = new GLUT();
    private GLU glu = new GLU();
    
    private Scene scene = new Scene();
    private FastPoissonDisk fastPossonDisk = new FastPoissonDisk();

    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        new A2App();
    }
    
    GLCanvas glCanvas;
    
    /** Main trackball for viewing the world and the two eye frustums */
    TrackBallCamera tbc = new TrackBallCamera();
    /** Second trackball for rotating the scene */
    TrackBallCamera tbc2 = new TrackBallCamera();
    
    /**
     * Creates the application
     */
    public A2App() {      
        Dimension controlSize = new Dimension(640, 640);
        Dimension size = new Dimension(640, 480);
        ControlFrame controlFrame = new ControlFrame("Controls");
        controlFrame.add("Camera", tbc.getControls());
        controlFrame.add("Scene TrackBall", tbc2.getControls());
        controlFrame.add("Scene", getControls());
        controlFrame.setSelectedTab("Scene");
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glc = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glc );
        glCanvas.setSize( size.width, size.height );
        glCanvas.setIgnoreRepaint( true );
        glCanvas.addGLEventListener( this );
        glCanvas.requestFocus();
        FPSAnimator animator = new FPSAnimator( glCanvas, 60 );
        animator.start();        
        tbc.attach( glCanvas );
        tbc2.attach( glCanvas );
        // initially disable second trackball, and improve default parameters given our intended use
        tbc2.enable(false);
        tbc2.setFocalDistance( 0 );
        tbc2.panRate.setValue(5e-5);
        tbc2.advanceRate.setValue(0.005);
        this.attach( glCanvas );        
        JFrame frame = new JFrame( name );
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( glCanvas, BorderLayout.CENTER );
        frame.setLocation(0,0);        
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible( true );        
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// nothing to do
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // do nothing
    }
    
    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_7) {
                    viewingMode = e.getKeyCode() - KeyEvent.VK_1 + 1;
                }
                // only use the tbc trackball camera when in view mode 1 to see the world from
                // first person view, while leave it disabled and use tbc2 ONLY FOR ROTATION when
                // viewing in all other modes
                if ( viewingMode == 1 ) {
                	tbc.enable(true);
                	tbc2.enable(false);
	            } else {
                	tbc.enable(false);
                	tbc2.enable(true);
	            }
            }
        });
    }
    
    /**
     * @return a control panel
     */
    public JPanel getControls() {     
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        
        VerticalFlowPanel vfp2 = new VerticalFlowPanel();
        vfp2.setBorder(new TitledBorder("Z Positions in WORLD") );
        vfp2.add( eyeZPosition.getSliderControls(false));        
        vfp2.add( nearZPosition.getSliderControls(false));
        vfp2.add( farZPosition.getSliderControls(false));        
        vfp2.add( focalPlaneZPosition.getSliderControls(false));     
        vfp.add( vfp2.getPanel() );
        
        vfp.add ( drawCenterEyeFrustum.getControls() );
        vfp.add ( drawEyeFrustums.getControls() );        
        vfp.add( eyeXOffset.getSliderControls(false ) );
        vfp.add( eyeYOffset.getSliderControls(false ) );        
        vfp.add ( aperture.getSliderControls(false) );
        vfp.add ( samples.getSliderControls() );        
        vfp.add( eyeDisparity.getSliderControls(false) );
        VerticalFlowPanel vfp3 = new VerticalFlowPanel();
        vfp3.setBorder( new TitledBorder("Scene size and position" ));
        vfp3.add( scene.getControls() );
        vfp.add( vfp3.getPanel() );        
        return vfp.getPanel();
    }
             
    public void init( GLAutoDrawable drawable ) {
    	drawable.setGL( new DebugGL2( drawable.getGL().getGL2() ) );
        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_NORMALIZE );
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do 
        gl.glLineWidth( 2 );                        // slightly fatter lines by default!
    }   

	// TODO: Objective 1 - adjust for your screen resolution and dimension to something reasonable.
	double screenWidthPixels = 1920;
	double screenWidthMeters = 0.31;
	double metersPerPixel = screenWidthMeters / screenWidthPixels;    
    @Override
    public void display(GLAutoDrawable drawable) {        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT|GL2.GL_DEPTH_BUFFER_BIT);    

        double w = drawable.getSurfaceWidth() * metersPerPixel;
        double h = drawable.getSurfaceHeight() * metersPerPixel;

    	FlatMatrix4d P = new FlatMatrix4d();
    	FlatMatrix4d Pinv = new FlatMatrix4d();
        
        //Objective 1 - draw yellow frame
    	gl.glDisable(GL2.GL_LIGHTING);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 0f);
        gl.glVertex3d(-w/2, -h/2, 0);
        gl.glVertex3d(-w/2, h/2, 0);
        gl.glVertex3d(w/2, h/2, 0);
        gl.glVertex3d(w/2, -h/2, 0);
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
        
        
        if ( viewingMode == 1 ) {
        	// We will use a trackball camera, but also apply an 
        	// arbitrary scale to make the scene and frustums a bit easier to see
        	// (note the extra scale could have been part of the initializaiton of
        	// the tbc track ball camera, but this is eaiser)
        	
                      
            // TODO: Objective 2 - draw camera frustum if drawCenterEyeFrustum is true
            if(drawCenterEyeFrustum.getValue()) {
            	
            	//Objective 1 - draw the eye
            	gl.glPushMatrix();
                gl.glColor3f(1f, 1f, 1f);
                gl.glTranslated( eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
                gl.glDisable(GL2.GL_LIGHTING);
                glut.glutSolidSphere(0.0125f, 32, 32);
                gl.glPopMatrix();
                gl.glEnable(GL2.GL_LIGHTING);
                
              //Objective 3 - draw the focal plane rectangle
                double focalLeft = -w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
                		focalRight = w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
                		focalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
                		focalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();
                            			
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                gl.glVertex3d( focalLeft, focalBottom, focalPlaneZPosition.getValue());
                gl.glVertex3d( focalLeft, focalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( focalRight, focalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( focalRight, focalBottom, focalPlaneZPosition.getValue());
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);
                
                //compute frustum
            	double frustumLeft = (focalLeft - eyeXOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumRight = (focalRight - eyeXOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumTop = (focalTop - eyeYOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumBottom = (focalBottom - eyeYOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue());

            	tbc.prepareForDisplay(drawable);
                gl.glScaled(15,15,15);        
                
                gl.glPushMatrix();
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);
             	
                gl.glPopMatrix();
      
                
                //draw center frustum
            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	gl.glFrustum( frustumLeft, frustumRight, frustumBottom, frustumTop,
            			eyeZPosition.getValue() - nearZPosition.getValue(), 
            			eyeZPosition.getValue() - farZPosition.getValue());
            	gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, P.asArray(), 0 );
        		P.reconstitute();
        		Pinv.getBackingMatrix().invert( P.getBackingMatrix() );
        		gl.glPopMatrix();
        		
        		gl.glMatrixMode(GL2.GL_MODELVIEW);
        		gl.glPushMatrix();
        		
        		gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
        		gl.glMultMatrixd(Pinv.asArray(), 0);

                gl.glColor3f(1f, 1f, 1f);
        		glut.glutWireCube(2f);
        		gl.glPopMatrix();
            }
            
            // TODO: Objective 6 - draw left and right eye frustums if drawEyeFrustums is true
            if(drawEyeFrustums.getValue()) {
            	//draw left focal plane
            	double lFocalLeft = (-w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() - 0.5 * eyeDisparity.getValue(),
            			lFocalRight = (w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() - 0.5 * eyeDisparity.getValue(),
            			lFocalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
						lFocalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();
                            			
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                gl.glVertex3d( lFocalLeft, lFocalBottom, focalPlaneZPosition.getValue());
                gl.glVertex3d( lFocalLeft, lFocalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( lFocalRight, lFocalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( lFocalRight, lFocalBottom, focalPlaneZPosition.getValue());
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);

                //draw right focal plane
                double rFocalLeft = (-w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() + 0.5 * eyeDisparity.getValue(),
            			rFocalRight = (w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() + 0.5 * eyeDisparity.getValue(),
            			rFocalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
						rFocalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();
                            			
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                gl.glVertex3d( rFocalLeft, rFocalBottom, focalPlaneZPosition.getValue());
                gl.glVertex3d( rFocalLeft, rFocalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( rFocalRight, rFocalTop, focalPlaneZPosition.getValue());
                gl.glVertex3d( rFocalRight, rFocalBottom, focalPlaneZPosition.getValue());
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);

            	//draw left and right eye
            	gl.glPushMatrix();
            	gl.glDisable(GL2.GL_LIGHTING);
            	gl.glColor3f(1f, 0f, 0f);
            	gl.glTranslated( -0.5 * eyeDisparity.getValue(), 0, eyeZPosition.getValue());
            	glut.glutSolidSphere(0.0125f, 32, 32);
            	gl.glEnable(GL2.GL_LIGHTING);
            	gl.glPopMatrix();
            	
            	gl.glPushMatrix();
            	gl.glDisable(GL2.GL_LIGHTING);
            	gl.glColor3f(0f, 1f, 1f);
            	gl.glTranslated( 0.5 * eyeDisparity.getValue(), 0, eyeZPosition.getValue());
            	glut.glutSolidSphere(0.0125f, 32, 32);
            	gl.glEnable(GL2.GL_LIGHTING);
            	gl.glPopMatrix();
            	
            	//draw left frustum in red
            	double lFrustumLeft = (-w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
                		lFrustumRight = (w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
        				lFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
						lFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	gl.glFrustum( lFrustumLeft, lFrustumRight, lFrustumBottom, lFrustumTop,
            			eyeZPosition.getValue() - nearZPosition.getValue(), 
            			eyeZPosition.getValue() - farZPosition.getValue());
            	gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, P.asArray(), 0 );
        		P.reconstitute();
        		Pinv.getBackingMatrix().invert( P.getBackingMatrix() );
        		gl.glPopMatrix();
        		
        		gl.glMatrixMode(GL2.GL_MODELVIEW);
        		gl.glPushMatrix();
        		
        		gl.glTranslated(-0.5 * eyeDisparity.getValue(), 0, eyeZPosition.getValue());
        		gl.glMultMatrixd(Pinv.asArray(), 0);

        		gl.glDisable(GL2.GL_LIGHTING);
                gl.glColor3f(1f, 0f, 0f);
        		glut.glutWireCube(2f);
        		gl.glEnable(GL2.GL_LIGHTING);
        		gl.glPopMatrix();
        		
        		//draw right frustum in cyan
        		double rFrustumLeft = (-w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
                		rFrustumRight = (w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
        				rFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
						rFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	gl.glFrustum( rFrustumLeft, rFrustumRight, rFrustumBottom, rFrustumTop,
            			eyeZPosition.getValue() - nearZPosition.getValue(), 
            			eyeZPosition.getValue() - farZPosition.getValue());
            	gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, P.asArray(), 0 );
        		P.reconstitute();
        		Pinv.getBackingMatrix().invert( P.getBackingMatrix() );
        		gl.glPopMatrix();
        		
        		gl.glMatrixMode(GL2.GL_MODELVIEW);
        		gl.glPushMatrix();
        		
        		gl.glTranslated(0.5 * eyeDisparity.getValue(), 0, eyeZPosition.getValue());
        		gl.glMultMatrixd(Pinv.asArray(), 0);

        		gl.glDisable(GL2.GL_LIGHTING);
                gl.glColor3f(0f, 1f, 1f);
        		glut.glutWireCube(2f);
        		gl.glEnable(GL2.GL_LIGHTING);
        		gl.glPopMatrix();
            }
            
        } else if ( viewingMode == 2 ) {
        	
        	// TODO: Objective 2 - draw the center eye camera view
        	double focalLeft = -w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalRight = w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();

        	double frustumLeft = (focalLeft - eyeXOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
            		frustumRight = (focalRight - eyeXOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
            		frustumTop = (focalTop - eyeYOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
            		frustumBottom = (focalBottom - eyeYOffset.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue());

        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glLoadIdentity();
        	gl.glFrustum( frustumLeft, frustumRight, frustumBottom, frustumTop,
        			eyeZPosition.getValue() - nearZPosition.getValue(), 
        			eyeZPosition.getValue() - farZPosition.getValue());

        	gl.glMatrixMode(GL2.GL_MODELVIEW);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	glu.gluLookAt(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue(), 0, 0, -1, 0, 1, 0);
        	scene.display(drawable);
        	gl.glPopMatrix();

        } else if ( viewingMode == 3 ) { 
        	// TODO: Objective 5 - draw center eye with depth of field blur
        	double focalLeft = -w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalRight = w/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
            		focalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();

        	int n = samples.getValue();
        	for(int i = 0 ; i < n ; i++ ) {
        		Point2d p = new Point2d();
        		fastPossonDisk.get(p, i, n);
        		p.scale(aperture.getValue());
        		double frustumLeft = (focalLeft - p.x) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumRight = (focalRight - p.x) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumTop = (focalTop - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                		frustumBottom = (focalBottom - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        		
        		//apply frustum and draw all scenes
        		gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glLoadIdentity();
            	gl.glFrustum( frustumLeft, frustumRight, frustumBottom, frustumTop,
            			eyeZPosition.getValue() - nearZPosition.getValue(), 
            			eyeZPosition.getValue() - farZPosition.getValue());
            	gl.glMatrixMode(GL2.GL_MODELVIEW);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	glu.gluLookAt(p.x, p.y, eyeZPosition.getValue(), 0, 0, -1, 0, 1, 0);
            	gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
            	scene.display(drawable);
            	if(i == 0) {
            		gl.glAccum(GL2.GL_LOAD, 1f/n );
            	}else {
            		gl.glAccum(GL2.GL_ACCUM, 1f/n );
            	}
            	gl.glPopMatrix();
        	}
        	gl.glAccum(GL2.GL_RETURN, 1);
            
        } else if ( viewingMode == 4 ) {
        	
            // TODO: Objective 6 - draw the left eye view
        	double lFrustumLeft = (-w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
            		lFrustumRight = (w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
    				lFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
					lFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glLoadIdentity();
        	gl.glFrustum( lFrustumLeft, lFrustumRight, lFrustumBottom, lFrustumTop,
        			eyeZPosition.getValue() - nearZPosition.getValue(), 
        			eyeZPosition.getValue() - farZPosition.getValue());

        	gl.glMatrixMode(GL2.GL_MODELVIEW);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	glu.gluLookAt(-0.5 * eyeDisparity.getValue() , 0, eyeZPosition.getValue(), -0.5 * eyeDisparity.getValue(), 0, -1, 0, 1, 0);
        	scene.display(drawable);
        	gl.glPopMatrix();
        	
        } else if ( viewingMode == 5 ) {  
        	
        	// TODO: Objective 6 - draw the right eye view
        	double rFrustumLeft = (-w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
            		rFrustumRight = (w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
    				rFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
					rFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glLoadIdentity();
        	gl.glFrustum( rFrustumLeft, rFrustumRight, rFrustumBottom, rFrustumTop,
        			eyeZPosition.getValue() - nearZPosition.getValue(), 
        			eyeZPosition.getValue() - farZPosition.getValue());

        	gl.glMatrixMode(GL2.GL_MODELVIEW);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	glu.gluLookAt(0.5 * eyeDisparity.getValue() , 0, eyeZPosition.getValue(), 0.5 * eyeDisparity.getValue(), 0, -1, 0, 1, 0);
        	scene.display(drawable);
        	gl.glPopMatrix();
        	                               
        } else if ( viewingMode == 6 ) {            
        	
        	// TODO: Objective 7 - draw the anaglyph view using glColorMask

        	//draw left eye view
        	double lFrustumLeft = (-w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
            		lFrustumRight = (w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
    				lFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
					lFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glLoadIdentity();
        	gl.glFrustum( lFrustumLeft, lFrustumRight, lFrustumBottom, lFrustumTop,
        			eyeZPosition.getValue() - nearZPosition.getValue(), 
        			eyeZPosition.getValue() - farZPosition.getValue());
        	gl.glMatrixMode(GL2.GL_MODELVIEW);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	glu.gluLookAt(-0.5 * eyeDisparity.getValue() , 0, eyeZPosition.getValue(), -0.5 * eyeDisparity.getValue(), 0, -1, 0, 1, 0);
        	gl.glColorMask( true, false, false, true);
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	scene.display(drawable);
        	gl.glPopMatrix();

        	//draw right eye view
        	double rFrustumLeft = (-w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
            		rFrustumRight = (w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
    				rFrustumTop = h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue(),
					rFrustumBottom = -h/2 * (eyeZPosition.getValue() - nearZPosition.getValue()) / eyeZPosition.getValue();

        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glLoadIdentity();
        	gl.glFrustum( rFrustumLeft, rFrustumRight, rFrustumBottom, rFrustumTop,
        			eyeZPosition.getValue() - nearZPosition.getValue(), 
        			eyeZPosition.getValue() - farZPosition.getValue());
        	gl.glMatrixMode(GL2.GL_MODELVIEW);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	glu.gluLookAt(0.5 * eyeDisparity.getValue() , 0, eyeZPosition.getValue(), 0.5 * eyeDisparity.getValue(), 0, -1, 0, 1, 0);
        	gl.glColorMask( false, true, true, true);         	
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	scene.display(drawable);
        	gl.glPopMatrix();
        	
        	gl.glColorMask( true, true, true, false);
        	
        } else if ( viewingMode == 7 ) {            
        	
        	// TODO: Bonus Ojbective 8 - draw the anaglyph view with depth of field blur
        	double lFocalLeft = (-w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() - 0.5 * eyeDisparity.getValue(),
        			lFocalRight = (w/2 + 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() - 0.5 * eyeDisparity.getValue(),
        			lFocalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
					lFocalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();
        	
        	gl.glColorMask( true, false, false, true);
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	
        	int n = samples.getValue();
         	for(int i = 0 ; i < n ; i++ ) {
         		Point2d p = new Point2d();
         		fastPossonDisk.get(p, i, n);
         		p.scale(aperture.getValue());
         		double frustumLeft = (lFocalLeft - p.x + eyeDisparity.getValue()/2) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumRight = (lFocalRight - p.x + eyeDisparity.getValue()/2) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumTop = (lFocalTop - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumBottom = (lFocalBottom - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue());
         		
         		//apply frustum and draw all scenes
         		gl.glMatrixMode(GL2.GL_PROJECTION);
             	gl.glLoadIdentity();
             	gl.glFrustum( frustumLeft, frustumRight, frustumBottom, frustumTop,
             			eyeZPosition.getValue() - nearZPosition.getValue(), 
             			eyeZPosition.getValue() - farZPosition.getValue());
             	gl.glMatrixMode(GL2.GL_MODELVIEW);
             	gl.glPushMatrix();
             	gl.glLoadIdentity();
             	glu.gluLookAt(p.x - eyeDisparity.getValue()/2, p.y, eyeZPosition.getValue(), p.x - eyeDisparity.getValue()/2, 0, -5, 0, 1, 0);
             	gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
             	scene.display(drawable);
             	if(i == 0) {
             		gl.glAccum(GL2.GL_LOAD, 1f/n );
             	}else {
             		gl.glAccum(GL2.GL_ACCUM, 1f/n );
             	}
             	gl.glPopMatrix();
         	}
         	gl.glAccum(GL2.GL_RETURN, 1);
         	
         	gl.glColorMask( false, true, true, true);
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	
        	double rFocalLeft = (-w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() + 0.5 * eyeDisparity.getValue(),
        			rFocalRight = (w/2 - 0.5 * eyeDisparity.getValue()) * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue() + 0.5 * eyeDisparity.getValue(),
        			rFocalTop = h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue(),
					rFocalBottom = -h/2 * (eyeZPosition.getValue() - focalPlaneZPosition.getValue()) / eyeZPosition.getValue();
        	
        	gl.glColorMask( false, true, true, true);
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        	
        	int m = samples.getValue();
         	for(int i = 0 ; i < m ; i++ ) {
         		Point2d p = new Point2d();
         		fastPossonDisk.get(p, i, m);
         		p.scale(aperture.getValue());
         		double frustumLeft = (rFocalLeft - p.x - eyeDisparity.getValue()/2) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumRight = (rFocalRight - p.x - eyeDisparity.getValue()/2) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumTop = (rFocalTop - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue()),
                 		frustumBottom = (rFocalBottom - p.y) * (eyeZPosition.getValue() - nearZPosition.getValue()) / (eyeZPosition.getValue() - focalPlaneZPosition.getValue());
         		
         		//apply frustum and draw all scenes
         		gl.glMatrixMode(GL2.GL_PROJECTION);
             	gl.glLoadIdentity();
             	gl.glFrustum( frustumLeft, frustumRight, frustumBottom, frustumTop,
             			eyeZPosition.getValue() - nearZPosition.getValue(), 
             			eyeZPosition.getValue() - farZPosition.getValue());
             	gl.glMatrixMode(GL2.GL_MODELVIEW);
             	gl.glPushMatrix();
             	gl.glLoadIdentity();
             	glu.gluLookAt(p.x + eyeDisparity.getValue()/2, p.y, eyeZPosition.getValue(), p.x + eyeDisparity.getValue()/2, 0, -5, 0, 1, 0);
             	gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
             	scene.display(drawable);
             	if(i == 0) {
             		gl.glAccum(GL2.GL_LOAD, 1f/m );
             	}else {
             		gl.glAccum(GL2.GL_ACCUM, 1f/m );
             	}
             	gl.glPopMatrix();
         	}
         	gl.glAccum(GL2.GL_RETURN, 1);

        	gl.glColorMask( true, true, true, false);
        	
        }        
    }
}
