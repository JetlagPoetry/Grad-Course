package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 2: intersection of ray with sphere
    	Vector3d d = new Vector3d(ray.viewDirection);
     	Vector3d e = new Vector3d(ray.eyePoint);
     	Vector3d p =new Vector3d();
     	p.scaleAdd(-1, center, e);
     	if(Math.pow(d.dot(p), 2) - d.dot(d)*(p.dot(p)-radius*radius) >= 0 && d.dot(d)!=0) {
     		double t1 = (-d.dot(p)+Math.sqrt(Math.pow(d.dot(p), 2) - d.dot(d)*(p.dot(p)-radius*radius))/d.dot(d)),
     				t2 = (-d.dot(p)-Math.sqrt(Math.pow(d.dot(p), 2) - d.dot(d)*(p.dot(p)-radius*radius))/d.dot(d));
     		double t = t1<=t2?t1:t2;
     		if( t > 0 && t < result.t) {
     			result.t = t;
     			result.p.set(d);
             	result.p.scale(result.t);
             	result.p.add(e);
             	result.n = new Vector3d();
             	result.n.scaleAdd(-1, center, result.p);
         		result.n.normalize();

             	result.material = this.material;
     		}
     	}
    }
}
