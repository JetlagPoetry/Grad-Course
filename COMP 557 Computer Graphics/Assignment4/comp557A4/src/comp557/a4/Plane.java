package comp557.a4;

import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
        // TODO: Objective 4: intersection of ray with plane
    	Vector3d d = new Vector3d(ray.viewDirection);
     	Vector3d p = new Vector3d(ray.eyePoint);
     	double t = -p.y/d.y;
     	if(t > 0 && t<result.t) {
     		result.t = t;
     		result.n.set(n);
         	result.p.scaleAdd(result.t, d, p);
         	if(Math.floor(result.p.x)%2==0&&Math.floor(result.p.z)%2==0||
         			Math.floor(result.p.x)%2!=0&&Math.floor(result.p.z)%2!=0||material2==null) {
         		result.material = material;
         	}else {
         		result.material = material2;
         	}
     	}
     	
    }
    
}
