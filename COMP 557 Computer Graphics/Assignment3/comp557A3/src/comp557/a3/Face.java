package comp557.a3;

import java.awt.KeyboardFocusManager;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

/**
 * Simple face class
 */
public class Face {    
    
	/** sure, why not keep a normal for flat shading? */
    public Vector3d n = new Vector3d();

    /** Plane equation */
	Vector4d p = new Vector4d();

    /** Quadratic function for the plane equation */
    public Matrix4d K = new Matrix4d();

    /** Some half edge on the face */
    HalfEdge he;
    
    /** 
     * Constructs a face from a half edge, and computes the flat normal
     * @param he
     */
    public Face( HalfEdge he ) {
        this.he = he;
        HalfEdge loop = he;
        do {
            loop.leftFace = this;
            loop = loop.next;
        } while ( loop != he );
        recomputeNormal();
    }
    
    public void recomputeNormal() {
    	Point3d p0 = he.head.p;
        Point3d p1 = he.next.head.p;
        Point3d p2 = he.next.next.head.p;
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();
        v1.sub(p1,p0);
        v2.sub(p2,p1);
        n.cross( v1,v2 );
        n.normalize();
        
        // TODO: Objective 4: you might compute the plane and matrix K for the quadric error metric here (or you could do it elsewhere)
        double d = -n.x*p0.x-n.y*p0.y-n.z*p0.z;
        p = new Vector4d(n.x,n.y,n.z,d);
        K.m00 = p.x*p.x;
        K.m01 = p.x*p.y;
        K.m02 = p.x*p.z;
        K.m03 = p.x*p.w;
        K.m10 = p.y*p.x;
        K.m11 = p.y*p.y;
        K.m12 = p.y*p.z;
        K.m13 = p.y*p.w;
        K.m20 = p.z*p.x;
        K.m21 = p.z*p.y;
        K.m22 = p.z*p.z;
        K.m23 = p.z*p.w;
        K.m30 = p.w*p.x;
        K.m31 = p.w*p.y;
        K.m32 = p.w*p.z;
        K.m33 = p.w*p.w;
    }
}
