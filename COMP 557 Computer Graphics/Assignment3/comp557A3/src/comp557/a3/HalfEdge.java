package comp557.a3;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;

/**
 * Class containing the half edge pointers, and a method for drawing
 * the half edge for debugging and evaluation.
 */
public class HalfEdge {
    
    public HalfEdge twin;
    public HalfEdge next;
    public Vertex head;
    public Face leftFace;
    
    /** 
     * While perhaps wasting space, it may be convenient to
     * have a common edge object for each pair of half edges to 
     * store information about the error metric, optimal vertex
     * location on collapse, and the error.
     */
    public Edge e;
    
    /** @return the previous half edge (could just be stored) */
    public HalfEdge prev() {
        HalfEdge prev = this;
        while ( prev.next != this ) prev = prev.next;        
        return prev;
    }
    
    /**
     * Computes the valence by walking around the vertex at head.
     * @return valence of the vertex at the head of this half edge
     */
    public int valence() {
    	HalfEdge loop = this;
    	int v = 0;
    	do {
    		v++;
    		loop = loop.next.twin;
    	} while ( loop != this );
    	return v;
    }
    
    /**
     * Displays the half edge as a half arrow pointing to the head vertex.
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
                
        Point3d p0 = prev().head.p;
        Point3d p1 = head.p;
        Point3d p2 = next.head.p;
        double x,y,z;
        
        gl.glLineWidth(3);
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glBegin( GL.GL_LINE_STRIP );
        gl.glColor4f(1,1,1,0.8f);
        x = p0.x * 0.8 + (p1.x + p2.x) * 0.1;
        y = p0.y * 0.8 + (p1.y + p2.y) * 0.1;
        z = p0.z * 0.8 + (p1.z + p2.z) * 0.1;
        gl.glVertex3d( x, y, z );
        x = p1.x * 0.8 + (p0.x + p2.x) * 0.1;
        y = p1.y * 0.8 + (p0.y + p2.y) * 0.1;
        z = p1.z * 0.8 + (p0.z + p2.z) * 0.1;
        gl.glVertex3d( x, y, z );
        x = p1.x * 0.7 + p0.x * 0.1 + p2.x * 0.2;
        y = p1.y * 0.7 + p0.y * 0.1 + p2.y * 0.2;
        z = p1.z * 0.7 + p0.z * 0.1 + p2.z * 0.2;
        gl.glVertex3d( x, y, z );        
        gl.glEnd();
        gl.glLineWidth(1);
        gl.glEnable( GL2.GL_LIGHTING );
    }
    
}
