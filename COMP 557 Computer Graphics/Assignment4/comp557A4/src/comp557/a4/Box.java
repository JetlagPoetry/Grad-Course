package comp557.a4;

import javax.vecmath.Point3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		double txMax = Math.max((min.x-ray.eyePoint.x)/ray.viewDirection.x, (max.x-ray.eyePoint.x)/ray.viewDirection.x),
				tyMax = Math.max((min.y-ray.eyePoint.y)/ray.viewDirection.y, (max.y-ray.eyePoint.y)/ray.viewDirection.y),
				tzMax = Math.max((min.z-ray.eyePoint.z)/ray.viewDirection.z, (max.z-ray.eyePoint.z)/ray.viewDirection.z),
				txMin = Math.min((min.x-ray.eyePoint.x)/ray.viewDirection.x, (max.x-ray.eyePoint.x)/ray.viewDirection.x),
				tyMin = Math.min((min.y-ray.eyePoint.y)/ray.viewDirection.y, (max.y-ray.eyePoint.y)/ray.viewDirection.y),
				tzMin = Math.min((min.z-ray.eyePoint.z)/ray.viewDirection.z, (max.z-ray.eyePoint.z)/ray.viewDirection.z);
		double tMin = Math.max(txMin,Math.max(tyMin, tzMin)),
				tMax = Math.min(txMax,Math.min(tyMax, tzMax));
		if(tMax>=tMin && tMin>0) {
			result.t = tMin;
			result.p.scaleAdd(tMin, ray.viewDirection, ray.eyePoint);
			result.material = material;
			double threshold = 1e-8;
			if(Math.abs(result.p.x-min.x)<threshold)
				result.n.set(-1,0,0);
			else if(Math.abs(result.p.x-max.x)<threshold)
				result.n.set(1,0,0);
			else if(Math.abs(result.p.y-min.y)<threshold)
				result.n.set(0,-1,0);
			else if(Math.abs(result.p.y-max.y)<threshold)
				result.n.set(0,1,0);
			else if(Math.abs(result.p.z-min.z)<threshold)
				result.n.set(0,0,-1);
			else if(Math.abs(result.p.z-max.z)<threshold)
				result.n.set(0,0,1);
		}
	}	

}
