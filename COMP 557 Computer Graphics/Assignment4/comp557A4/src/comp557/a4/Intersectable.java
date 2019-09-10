package comp557.a4;

/**
 * Abstract class for an intersectable surface 
 */
public abstract class Intersectable {
	
	/** Material for this intersectable surface */
	public Material material;
	
	/** 
	 * Default constructor, creates the default material for the surface
	 */
	public Intersectable() {
		this.material = new Material();
	}
	
	/**
	 * Test for intersection between a ray and this surface. This is an abstract
	 *   method and must be overridden for each surface type.
	 * @param ray
	 * @param result
	 */
    public abstract void intersect(Ray ray, IntersectResult result);
    
}
