package comp557.a4;

import javax.vecmath.Vector3d;

public class Quadric extends Intersectable{
	
	/**
     * Default constructor
     */
    public Quadric() {
    	super();
    }
    
    public double a2=1;
    public double b2=2;
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) { 
    	
    	Vector3d d=new Vector3d(ray.viewDirection);
    	Vector3d e=new Vector3d(ray.eyePoint);
    	//At^2+Bt+C=0
    	double A,B,C,t;
    	A = b2*d.x*d.x + a2*d.y*d.y;
    	B = 2*b2*e.x*d.x + 2*a2*e.y*d.y - 2*a2*b2*d.z;
    	C = b2*e.x*e.x + a2*e.y*e.y - 2*a2*b2*e.z;
    	
    	double sqrt=B*B-4*A*C;
    	if(sqrt>=0 && A!=0){
        	double t1=(-B+Math.sqrt(sqrt))/(A*2);
        	double t2=(-B-Math.sqrt(sqrt))/(A*2);
        	t = t2>0?t2:t1;
        	
        	if(t<result.t && t>0){
	       		result.t=t;
	       		result.p.scaleAdd(t, d, e);
	           	result.n.set(2/a2*result.p.x, 2/b2*result.p.y, -2);
	           	result.n.normalize();

	           	result.material=material;
        	}
    	}
    }
}