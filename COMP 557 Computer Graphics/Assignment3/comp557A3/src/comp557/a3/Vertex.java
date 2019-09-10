package comp557.a3;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 * Simple vertex class
 */
public class Vertex {
	
	/** position of this vertex */
    public Point3d p = new Point3d();
    
    /** Error metric, v^T Q v gives sum of distances squared to the planes of all faces adjacent to this vertex */
    Matrix4d Q = new Matrix4d();
    
}
