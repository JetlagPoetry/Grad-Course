package comp557.a3;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector4d;

/**
 * A class to store information concerning mesh simplificaiton
 * that is common to a pair of half edges.  Speicifically, 
 * the error metric, optimal vertex location on collapse, 
 * and the error.
 * @author kry
 */
public class Edge implements Comparable<Edge> {
	
	/** One of the two half edges */
	HalfEdge he;
	
	/** Optimal vertex location on collapse */
	Vector4d v = new Vector4d();
	
	/** Error metric for this edge */
	Matrix4d Q = new Matrix4d();
	
	/** The error involved in performing the collapse of this edge */
	double error;
	
	@Override
	public int compareTo(Edge o) {
		if (error < o.error ) return -1;
		if (error > o.error ) return 1;
		return 0;
	}
	
}
