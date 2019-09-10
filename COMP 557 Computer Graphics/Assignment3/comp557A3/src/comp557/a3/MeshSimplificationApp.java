package comp557.a3;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.Parameter;
import mintools.parameters.ParameterListener;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.Interactor;
import mintools.viewer.SceneGraphNode;

/**
 * COMP557 - Mesh simplification application
 */
public class MeshSimplificationApp implements SceneGraphNode, Interactor {

    public static void main(String[] args) {
        new MeshSimplificationApp();
    }
    
    private PolygonSoup soup;
    
    private HEDS heds;
    
    private HalfEdge currentHE;
    
    private int whichSoup = 0;
    
    private String[] soupFiles = {
    		"meshdata/tetrahedron.obj",
    		"meshdata/topologytest.obj",
    		"meshdata/ico-sphere-tris.obj",
    		"meshdata/icosphere2.obj",
    		"meshdata/cube.obj",
    		"meshdata/cube2obj.obj",
    		"meshdata/bunny.obj",
    		"meshdata/cow.obj",    		
            "meshdata/monkey.obj",   
        };
    
    public MeshSimplificationApp() {    
        loadSoupBuildAndSubdivide( soupFiles[0] );
        EasyViewer ev = new EasyViewer("Comp 557 Mesh Simplification - Jingyuan Wang", this, new Dimension(400, 400), new Dimension(600, 650) );
        ev.addInteractor(this);
    }
    
    /**
     * Loads the currently 
     */
    private void loadSoupBuildAndSubdivide( String filename ) {
        soup = new PolygonSoup( filename );
        heds = new HEDS( soup , regularizationWeight.getValue());
//        if ( heds.faces.size() > 0 ) {
//        	currentHE = heds.faces.iterator().next().he;
//        }
        try {
            currentHE = heds.nextCollapseEdge();
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        if ( ! cullFace.getValue() ) {             
        	gl.glDisable( GL.GL_CULL_FACE );
        } else {
        	gl.glEnable( GL.GL_CULL_FACE );
        }

        if ( scaleWithLOD.getValue() ) {
        	// don't go all the way to zero...  Just 1%
        	double s = 0.99 * LOD.getValue() + 0.01;
        	s = s*s; // or could do Math.pow( s, factor ) to adjust how the scaling goes with LOD, but quadratic seems good 
        	gl.glScaled( s, s, s );
        }
        
        if ( !wireFrame.getValue()) {
            // if drawing with lighting, we'll set the material
            // properties for the font and back surfaces, and set
            // polygons to render filled.
            gl.glEnable(GL2.GL_LIGHTING);
            final float frontColour[] = {.7f,.7f,0,1};
            final float backColour[] = {0,.7f,.7f,1};
            final float[] shinyColour = new float[] {1f, 1f, 1f, 1};            
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glMaterialfv( GL.GL_FRONT,GL2.GL_AMBIENT_AND_DIFFUSE, frontColour, 0 );
            gl.glMaterialfv( GL.GL_BACK,GL2.GL_AMBIENT_AND_DIFFUSE, backColour, 0 );
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK,GL2.GL_SPECULAR, shinyColour, 0 );
            gl.glMateriali( GL.GL_FRONT_AND_BACK,GL2.GL_SHININESS, 50 );
            gl.glLightModelf(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_FILL );            
        } else {
            // if drawing without lighting, we'll set the colour to white
            // and set polygons to render in wire frame
            gl.glDisable( GL2.GL_LIGHTING );
            gl.glColor4f(.7f,.7f,0.0f,1);
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_LINE );
        }    
        
        
        if ( drawHEDSMesh.getValue() ) heds.display( drawable );
        
        
        if ( drawPolySoup.getValue() ) {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glEnable( GL.GL_BLEND );
            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glColor4f(.7f,.7f,7.0f,0.5f);
            gl.glLineWidth(1);
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_LINE );
            soup.display( drawable );
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_FILL );
        }
        
        if ( drawHalfEdge.getValue() && currentHE != null ) {
            currentHE.display( drawable );
        }
        
        gl.glColor4f(1,1,1,1);
        EasyViewer.beginOverlay(drawable);
        EasyViewer.printTextLines(drawable, soupFiles[whichSoup] + " Faces = " + heds.faces.size(), 10,20,15, GLUT.BITMAP_8_BY_13 );
        EasyViewer.endOverlay(drawable);
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL( new DebugGL2(drawable.getGL().getGL2()) );
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL2.GL_POINT_SMOOTH );
        gl.glEnable( GL2.GL_NORMALIZE );
        gl.glShadeModel( GL2.GL_SMOOTH ); // Enable smooth shading, though everything should be flat!
    }

    private BooleanParameter drawPolySoup = new BooleanParameter( "draw soup mesh (wire frame)", false );    
    private BooleanParameter drawHEDSMesh = new BooleanParameter( "draw HEDS mesh", true );
    private BooleanParameter cullFace = new BooleanParameter( "cull face", true );
    private BooleanParameter wireFrame = new BooleanParameter( "wire frame", false );    
    private BooleanParameter drawHalfEdge = new BooleanParameter( "draw test half edge", true );    
    private DoubleParameter regularizationWeight = new DoubleParameter( "regularizaiton", 0.01, 1e-6, 1e2 );
    private BooleanParameter drawEdgeErrors = new BooleanParameter("draw edge errors", true );
    private DoubleParameter LOD = new DoubleParameter( "LOD", 1, 0, 1 );   
    private BooleanParameter scaleWithLOD = new BooleanParameter( "scale with LOD", false );
    
    @Override
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( drawPolySoup.getControls() );
        vfp.add( drawHEDSMesh.getControls() );                           
        vfp.add( cullFace.getControls() );
        vfp.add( wireFrame.getControls() );
        vfp.add( drawHalfEdge.getControls() );
        vfp.add( regularizationWeight.getSliderControls(true) );
        vfp.add( drawEdgeErrors.getControls() );
        vfp.add( LOD.getSliderControls(false) );
        
        LOD.addParameterListener( new ParameterListener<Double>() {
		@Override
		public void parameterChanged(Parameter<Double> parameter) {
				int N = heds.undoList.size() + heds.redoListHalfEdge.size();
				double v = 1 - parameter.getValue();
				int k = (int) (v * N);
				if ( heds.undoList.size() < k ) {
					while (heds.undoList.size() < k ) heds.redoCollapse();
				} else {
					while (heds.undoList.size() > k ) heds.undoCollapse();
				}
			}
        });
        vfp.add( scaleWithLOD.getControls() );
        JTextArea ta = new JTextArea(
        		"   space - half edge twin \n" +
        		"   n - half edge next \n" +
        		"   c - collapse current half edge \n" +
        		"   C - collapse in loop until no more to do \n" +
        		"   g - goto best candidate edge \n" +
        		"   z - undo collapse \n" +
        		"   y - redo collapse \n" +
        		"   home - display mesh subdivision level - 1\n" +
        		"   end - display subdivision level + 1\n" +
        		"   page up - previous model\n" +
        		"   page down - next model\n" );                  
        ta.setEditable(false);
        ta.setBorder( new TitledBorder("Keyboard controls") );
        vfp.add( ta );
        return vfp.getPanel();
    }

    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if ( currentHE.twin != null ) currentHE = currentHE.twin;                    
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    if ( currentHE.next != null ) currentHE = currentHE.next;
                } else if ( e.getKeyCode() == KeyEvent.VK_PAGE_UP ) {
                    if ( whichSoup > 0 ) whichSoup--;                    
                    loadSoupBuildAndSubdivide( soupFiles[whichSoup] );
                } else if ( e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ) {
                    if ( whichSoup < soupFiles.length -1 ) whichSoup++;                    
                    loadSoupBuildAndSubdivide( soupFiles[whichSoup] );
                } else if ( e.getKeyCode() == KeyEvent.VK_C ) { 
                	if ( e.isShiftDown() ) {
                		heds.setRegularizationWeight(regularizationWeight.getValue());
                		do {
                			do{
                    			if(heds.noMoreCollapse(currentHE)) {
                    				currentHE = heds.nextCollapseEdge();
                    				if(currentHE==null) return;
                    			}else {
                    				break;
                    			}
                    		}while(true);
                    		currentHE = heds.collapse(currentHE);
                		}while(true);
                	} else {
                    	// TODO: Objective 2: handle C keypress to collapse an edge
                		heds.setRegularizationWeight(regularizationWeight.getValue());
                		do{
                			if(heds.noMoreCollapse(currentHE)) {
                				currentHE = heds.nextCollapseEdge();
                				if(currentHE==null) return;
                			}else {
                				break;
                			}
                		}while(true);
                		currentHE = heds.collapse(currentHE);
                	}
                } else if ( e.getKeyCode() == KeyEvent.VK_G ) {
                	// TODO: Objective 5: handle G keypress to set the halfedge to the best candidate
                	currentHE = currentHE;
                } else if ( e.getKeyCode() == KeyEvent.VK_Z ) {
                	heds.undoCollapse();
                } else if ( e.getKeyCode() ==  KeyEvent.VK_Y ) {
                	heds.redoCollapse();
                }
            }
        });
    }

}
