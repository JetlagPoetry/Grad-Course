package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Color4f;

/**
 * A class defining the material properties of a surface, 
 * such as colour and specularity. 
 */
public class Material {
	
	/** Static member to access all the materials */
	public static Map<String,Material> materialMap = new HashMap<String,Material>();
	
	/** Material name */
    public String name = "";
    
    /** Diffuse colour, defaults to white */
    public Color4f diffuse = new Color4f(1,1,1,1);
    
    /** Specular colour, default to black (no specular highlight) */
    public Color4f specular = new Color4f(0,0,0,0);
    
    /** Specular hardness, or exponent, default to a reasonable value */ 
    public float shinyness = 64;
 
    public String type= "normal";
    
    //Fresnel reflection coefficient
    public Color4f fresnel = new Color4f(0.8f,0.8f,0.8f,0);
    
    //Mirror reflection coeeficient
    public Color4f mirror = new Color4f(0.8f,0.8f,0.8f,0);
    
    /**
     * Default constructor
     */
    public Material() {
    	// do nothing
    }
    
}
