package comp557.a4;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple camera object, which could be extended to handle a variety of 
 * different camera settings (e.g., aperature size, lens, shutter)
 */
public class Camera {
	
	/** Camera name */
    public String name = "camera";

    /** The eye position */
    public Point3d from = new Point3d(0,0,10);
    
    /** The "look at" position */
    public Point3d to = new Point3d(0,0,0);
    
    /** Up direction, default is y up */
    public Vector3d up = new Vector3d(0,1,0);
    
    /** Vertical field of view (in degrees), default is 45 degrees */
    public double fovy = 45.0;
    
    /** The rendered image size */
    public Dimension imageSize = new Dimension(640,480);

    /**
     * Default constructor
     */
    public Camera() {
    	// do nothing
    }
    
    //Clone another camera
    public Camera(Camera cam) {
    	this.name = cam.name;
    	this.from.set(cam.from);
    	this.to.set(cam.to);
    	this.up.set(cam.up);
    	this.fovy = cam.fovy;
    	this.imageSize.setSize(cam.imageSize);
    }
    
    public void setCameraFrom(double x, double y, double z) {
    	this.from.set( x, y, z);
    }
}

