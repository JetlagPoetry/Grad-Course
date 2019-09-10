package comp557.a1;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JPanel;

import mintools.parameters.BooleanParameter;
import mintools.viewer.EasyViewer;

import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * @author 260860682 Jingyuan Wang
 */
public class CanvasRecorder {

    /** Image for sending to the image processor */
    private BufferedImage image;
    
    /** Image Buffer for reading pixels */
    private Buffer imageBuffer;
    
    BooleanParameter record = new BooleanParameter( "record", false );
    
    private String dumpName = "dump";
    
    private int nextFrameNum = 0;
    
    private NumberFormat format = new DecimalFormat("00000");
    
    public JPanel getControls() {
    	return record.getControls();
    }
    
    /**
     * Saves a snapshot of the current canvas to a file.
     * The image is saved in png format and will be of the same size as the canvas.
     * Note that if you are assembling frames saved in this way into a video, 
     * for instance, using virtualdub, then you'll need to take care that the 
     * canvas size is nice (i.e., a multiple of 16 in each dimension), or add 
     * a filter in virtualdub to resize the image to be a codec friendly size.
     * @param drawable
     * @param file
     * @return true on success
     */
    public void snapshot( GLAutoDrawable drawable ) {
    	
    	if ( !record.getValue() ) return;
    	int width = drawable.getSurfaceWidth();
    	int height = drawable.getSurfaceHeight();
    	if ( image == null || image.getWidth() != width || image.getHeight() != height ) {
            //image = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );            
            image = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
            imageBuffer = ByteBuffer.wrap(((DataBufferByte)image.getRaster().getDataBuffer()).getData());
    	}
    	
        // write the frame
        File file = new File( "stills/" + dumpName + format.format(nextFrameNum) + ".png" );                                             
        nextFrameNum++;
        file = new File(file.getAbsolutePath().trim());
    	
        GL2 gl = drawable.getGL().getGL2();
        //gl.glReadPixels( 0, 0, width, height, GL2.GL_ABGR_EXT, GL.GL_UNSIGNED_BYTE, imageBuffer );            
        gl.glReadPixels( 0, 0, width, height, GL2.GL_BGR, GL.GL_UNSIGNED_BYTE, imageBuffer );
        ImageUtil.flipImageVertically(image);
        
        try {
            if ( ! ImageIO.write( image, "png", file) ) {
                System.err.println("Error writing file using ImageIO (unsupported file format?)");
                return;
            }
        } catch (IOException e) {    
            System.err.println("trouble writing " + file );
            e.printStackTrace();
            return;
        }
        
        // print a message in the display window
        EasyViewer.beginOverlay( drawable );
        String text =  "RECORDED: "+ file.toString();
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glColor4f( 1, 0, 0, 1 );           
        EasyViewer.printTextLines( drawable, text, 10, drawable.getSurfaceHeight()-20, 10, GLUT.BITMAP_HELVETICA_10 );
        gl.glEnable( GL2.GL_LIGHTING );
        EasyViewer.endOverlay(drawable);
    }

}
