package comp557.a4;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Color3f;

/**
 * An object to define the image and parameters specific to this render job.
 * For instance, in addition to a camera, this could also include parameters 
 * to specify how to perform anti-aliasing, the maximum depth for reflection and
 * refraction rays, a gamma value for creating the image, or any other parameter 
 * that influences image generation at this level. 
 */
public class Render extends WindowAdapter {
       
	/** Simple internal class used by the render window. */
	@SuppressWarnings("serial")
	class ImagePanel extends JPanel {		
		private BufferedImage image;		
		public ImagePanel(BufferedImage image) {
			this.image = image;
		}	
		@Override
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
	}
	
	/** The render camera */
    public Camera camera;
    
    /** Samples per pixel */
    public int samples;
    
    /** The output filename */
    public String output;
    
    /** The background color */
    public Color3f bgcolor;
    
    /** Buffered image of the render, updated as it progresses */    
    public BufferedImage image;
    
    /** Drawing panel */
    private ImagePanel panel;

    /** Flag to indicate when rendering should stop */
    private boolean done;
    
    /** Flag whether to do with anti-aliasing*/
    public boolean jitter;
    
    /**
     * Default constructor. Creates a default camera and black background color.
     * @param dataNode
     */
    public Render() {
    	this.done = false;
    	this.camera = new Camera();
    	this.image = null;
    	this.output = "render.png";
    	this.bgcolor = new Color3f( 0, 0, 0 );
    	this.panel = null;
    	this.samples = 1;
    	this.jitter = false;
    }
    
    /**
     * Creates a buffered image using ARGB integer ordering for color components.
     * Also, this method creates and displays a render window.  Closing this window will
     * cancel rendering.
     * 
     * @param width
     * @param height
     */
    public void init(int width, int height, boolean showPanel) {
    	image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
    	Graphics2D g2 = (Graphics2D)image.getGraphics();
    	if ( g2 != null ) {
	    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    	g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
	    	g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
    	}    	
    	if ( showPanel ) {    	
	    	panel = new ImagePanel(image);
	    	panel.setPreferredSize( new Dimension(width, height) );	    	
	    	JFrame frame = new JFrame();
	    	frame.addWindowListener(this);
	    	frame.setLayout( new BorderLayout() );
	    	frame.add( panel, BorderLayout.CENTER );
	    	frame.setResizable( false );
	    	frame.pack();
	    	frame.setVisible( true );	
	    	g2 = (Graphics2D) panel.getGraphics();
	    	if ( g2 != null ) {
		    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		    	g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		    	g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
	    	}
    	}
    }
    
    public void setPixel(int x, int y, int argb) {
    	// update the image
    	image.setRGB(x, y, argb);    	
    	// redraw the image
    	if( panel != null ) panel.repaint();
    }    
    
    /**
     * Saves the rendered image to a PNG file.
     */
    public void save() {    	
        File file = new File( output );
        try {
            if ( !ImageIO.write( image, "png", file) ) {
                System.err.println("Render: Error writing file using ImageIO (unsupported file format?)");
            }
        } catch (IOException e) {    
            System.err.println("Render: Trouble writing " + file );
            e.printStackTrace();
        }         	
    }
    
    /**
     * Checks if the renderer is done. 
     * @return Returns true if the user closed the render window; false otherwise.
     */
    public boolean isDone() {
    	return done;
    }
    
    /**
     * Forces the current thread to block until the done == true 
     */
    public void waitDone() {
        while( !done ) {
        	try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Window listener function.
     */
    @Override
    public void windowClosing(WindowEvent event) {
    	done = true;
    }

}
