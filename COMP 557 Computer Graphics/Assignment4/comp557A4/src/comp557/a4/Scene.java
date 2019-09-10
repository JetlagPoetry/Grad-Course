package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    //For multi-threaded parallelization
    public int threadCount = 1;
   
    //For depth of blur
    public boolean blur = false;
    public int blurSamples = 10;
    public double focalLength = 10;
    private FastPoissonDisk fastPossonDisk = new FastPoissonDisk();
    
    //For reflection
    private int reflectionLevel = 4;
    
    //For refraction
    private float refractivity = 0.8f;
    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    //Offset
    private final double[] offset = new double[2];
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        render.init(w, h, showPanel);
        
        //Move the render process to new thread
        for(int i=0; i<threadCount; i++) {
        	RenderThread t = new RenderThread(this,cam,w,h,i);
        	t.start();
        }
        
        // wait for render viewer to close
        render.waitDone();

        // save the final render image
        render.save();
    }
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		
		// TODO: Objective 1: generate rays given the provided parmeters
		//Create camera coordinates
		Vector3d W = new Vector3d();
		W.scaleAdd(-1, cam.to, cam.from);
		double d = W.length();
		W.normalize();
		Vector3d U = new Vector3d();
		U.cross(cam.up, W);
		U.normalize();
		Vector3d V = new Vector3d();
		V.cross(W, U);
		V.normalize();
		
		//Use the formula on slides, direction = uU+vV-dW
		double t = -d*Math.tan(Math.toRadians(cam.fovy/2.0)), b = -t, 
				r = -t*(double)cam.imageSize.width/cam.imageSize.height, l = -r;
		double u = l+(r-l)*(j+0.5+offset[1])/cam.imageSize.width,
				v = b+(t-b)*(i+0.5+offset[0])/cam.imageSize.height;
		Vector3d direction = new Vector3d();
		direction.scale(u, U);
		direction.scaleAdd(v, V, direction);
		direction.scaleAdd(-d, W, direction);
		direction.normalize();
		ray.set(cam.from, direction);
		
	}
  
	
	/**
	 * Compute three kinds of lighting and shadings
	 * 
	 * @param result Intersection result from raytracing
	 * @param ray Ray indicating view point
	 */
	private Color4f computeLighting(IntersectResult result, Ray ray, int reflection) {
		Color4f color = new Color4f(0, 0, 0, 0);
		//Ambient
		if("normal".equals(result.material.type)) {
			color.set(ambient.x, ambient.y, ambient.z,0);
		}
		
		for(Map.Entry<String, Light> entry:lights.entrySet()) {
			Light light = entry.getValue();
			if("direction".equals(light.type)) {
				List<Light> lightList = new ArrayList<Light>();
				for(int i=0;i<light.sourceCount;i++) {
					Light temp = new Light(light);
					temp.setLightFrom(light.from.x+Math.random(),light.from.y,light.from.z+Math.random());
					lightList.add(temp);
				}
				
				Color4f shading = new Color4f();
				for(int i=0;i<lightList.size();i++) {
					IntersectResult shadowResult = new IntersectResult();
					Ray shadowRay = new Ray();
					if(inShadow(result, lightList.get(i), surfaceList, shadowResult, shadowRay)) {
						continue;
					}

					//Mirror Reflection
					if("mirror".equals(result.material.type) && reflection>0) {
						Ray reflectionRay = new Ray();
						reflectionRay.viewDirection.scaleAdd(-2*ray.viewDirection.dot(result.n), result.n, ray.viewDirection);
						reflectionRay.viewDirection.normalize();
						reflectionRay.eyePoint.scaleAdd(1e-10, reflectionRay.viewDirection, result.p);
						IntersectResult reflectionResult = new IntersectResult();
						for(Intersectable surface:surfaceList) {
							surface.intersect(reflectionRay, reflectionResult);
						}
						if(reflectionResult.t<Double.POSITIVE_INFINITY) {
							Color4f reflectionColor = computeLighting(reflectionResult,reflectionRay,reflection-1);
							reflectionColor = new Color4f(reflectionColor.x*result.material.mirror.x, reflectionColor.y*result.material.mirror.y,
									reflectionColor.z*result.material.mirror.z, 1);
							shading.add(reflectionColor);
						}else {
							color.set(render.bgcolor.x, render.bgcolor.y, render.bgcolor.z, 1);
						}
					}else if("refraction".equals(result.material.type) && reflection>0) {
						Ray refractionRay = new Ray();
						double cosi = ray.viewDirection.dot(result.n);
						cosi = cosi<1?cosi:1;
						cosi = cosi>-1?cosi:-1;
						double etai = 1, etat = refractivity;
						Vector3d n = new Vector3d();
						if(cosi>0) {
							etai = 1.0;
							etat = refractivity;
							cosi = -cosi;
							n.scale(-1, result.n);
						}else {
							etai = refractivity;
							etat = 1.0;
							n.set(result.n);
						}
						double eta = etai/etat;
						double k = 1-eta*eta*(1-cosi*cosi);
						if(k<=0) {
//							refractionRay.viewDirection.set(result.n);
							refractionRay.viewDirection.set(new Vector3d(0,0,0));
						}else {
							n.scale((eta * cosi) - Math.sqrt(k));
							refractionRay.viewDirection.scaleAdd(eta, ray.viewDirection, n);
						}
						refractionRay.viewDirection.normalize();
						refractionRay.eyePoint.scaleAdd(1e-10, refractionRay.viewDirection, result.p);
						IntersectResult refractionResult = new IntersectResult();
						for(Intersectable surface:surfaceList) {
							surface.intersect(refractionRay, refractionResult);
						}
						if(refractionResult.t<Double.POSITIVE_INFINITY) {
							Color4f reflectionColor = computeLighting(refractionResult,refractionRay,reflection-1);
							reflectionColor = new Color4f(reflectionColor.x, reflectionColor.y,
									reflectionColor.z, 1);
							shading.add(reflectionColor);
						}else {
							color.set(render.bgcolor.x, render.bgcolor.y, render.bgcolor.z, 1);
						}
					}else {
						//Diffuse-Lambertian
						Color4f lambertian = new Color4f();
						lambertian.x = (float) (light.power * light.color.x * result.material.diffuse.x);
						lambertian.y = (float) (light.power * light.color.y * result.material.diffuse.y);
						lambertian.z = (float) (light.power * light.color.z * result.material.diffuse.z);
						Vector3d l = new Vector3d();
						l.scaleAdd(-1, result.p, lightList.get(i).from);
						l.normalize();
						lambertian.scale((float) Math.max(0, l.dot(result.n)));			
						shading.add(lambertian);

						//Blinn-Phong
						Color4f specular = new Color4f();
						specular.x = (float) (light.power*light.color.x*result.material.specular.x);
						specular.y = (float) (light.power*light.color.y*result.material.specular.y);
						specular.z = (float) (light.power*light.color.z*result.material.specular.z);
						specular.w = 1;
						Vector3d v = new Vector3d();
						v.scaleAdd(-1, result.p, ray.eyePoint);
						v.normalize();
						l = new Vector3d();
						l.scaleAdd(-1, result.p, lightList.get(i).from);
						l.normalize();
						Vector3d h = new Vector3d();
						h.add(v, l);
						h.normalize();
						specular.scale((float) Math.pow(Math.max(0, h.dot(result.n)), result.material.shinyness));
						shading.add(specular);
					}
				}
				shading.scale(1f/light.sourceCount);
				color.add(shading);
			}else {
				IntersectResult shadowResult = new IntersectResult();
				Ray shadowRay = new Ray();
				if(inShadow(result, light, surfaceList, shadowResult, shadowRay)) {
					continue;
				}

				//Mirror Reflection
				if("mirror".equals(result.material.type) && reflection>0) {
					Ray reflectionRay = new Ray();
					reflectionRay.viewDirection.scaleAdd(-2*ray.viewDirection.dot(result.n), result.n, ray.viewDirection);
					reflectionRay.viewDirection.normalize();
					reflectionRay.eyePoint.scaleAdd(1e-10, reflectionRay.viewDirection, result.p);
					IntersectResult reflectionResult = new IntersectResult();
					for(Intersectable surface:surfaceList) {
						surface.intersect(reflectionRay, reflectionResult);
					}
					if(reflectionResult.t<Double.POSITIVE_INFINITY) {
						Color4f reflectionColor = computeLighting(reflectionResult,reflectionRay,reflection-1);
						reflectionColor = new Color4f(reflectionColor.x*result.material.mirror.x, reflectionColor.y*result.material.mirror.y,
								reflectionColor.z*result.material.mirror.z, 1);
						color.add(reflectionColor);
					}else {
						color.set(render.bgcolor.x, render.bgcolor.y, render.bgcolor.z, 1);
					}
				}else if("refraction".equals(result.material.type) && reflection>0) {
					Ray refractionRay = new Ray();
					double cosi = ray.viewDirection.dot(result.n);
					cosi = cosi<1?cosi:1;
					cosi = cosi>-1?cosi:-1;
					double etai = 1, etat = refractivity;
					Vector3d n = new Vector3d();
					if(cosi>0) {
						etai = 1.0;
						etat = refractivity;
						cosi = -cosi;
						n.scale(-1, result.n);
					}else {
						etai = refractivity;
						etat = 1.0;
						n.set(result.n);
					}
					double eta = etai/etat;
					double k = 1-eta*eta*(1-cosi*cosi);
					if(k<=0) {
//						refractionRay.viewDirection.set(result.n);
						refractionRay.viewDirection.set(new Vector3d(0,0,0));
					}else {
						n.scale((eta * cosi) - Math.sqrt(k));
						refractionRay.viewDirection.scaleAdd(eta, ray.viewDirection, n);
					}
					refractionRay.viewDirection.normalize();
					refractionRay.eyePoint.scaleAdd(1e-10, refractionRay.viewDirection, result.p);
					IntersectResult refractionResult = new IntersectResult();
					for(Intersectable surface:surfaceList) {
						surface.intersect(refractionRay, refractionResult);
					}
					if(refractionResult.t<Double.POSITIVE_INFINITY) {
						Color4f reflectionColor = computeLighting(refractionResult,refractionRay,reflection-1);
						reflectionColor = new Color4f(reflectionColor.x, reflectionColor.y,
								reflectionColor.z, 1);
						color.add(reflectionColor);
					}else {
						color.set(render.bgcolor.x, render.bgcolor.y, render.bgcolor.z, 1);
					}
				}else {
					//Diffuse-Lambertian
					Color4f lambertian = new Color4f();
					lambertian.x = (float) (light.power * light.color.x * result.material.diffuse.x);
					lambertian.y = (float) (light.power * light.color.y * result.material.diffuse.y);
					lambertian.z = (float) (light.power * light.color.z * result.material.diffuse.z);
					Vector3d l = new Vector3d();
					l.scaleAdd(-1, result.p, light.from);
					l.normalize();
					lambertian.scale((float) Math.max(0, l.dot(result.n)));			
					color.add(lambertian);

					//Blinn-Phong
					Color4f specular = new Color4f();
					specular.x = (float) (light.power*light.color.x*result.material.specular.x);
					specular.y = (float) (light.power*light.color.y*result.material.specular.y);
					specular.z = (float) (light.power*light.color.z*result.material.specular.z);
					specular.w = 1;
					Vector3d v = new Vector3d();
					v.scaleAdd(-1, result.p, ray.eyePoint);
					v.normalize();
					l = new Vector3d();
					l.scaleAdd(-1, result.p, light.from);
					l.normalize();
					Vector3d h = new Vector3d();
					h.add(v, l);
					h.normalize();
					specular.scale((float) Math.pow(Math.max(0, h.dot(result.n)), result.material.shinyness));
					color.add(specular);
				}
			}
			
		}
		return color;
	}
	
	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param surfaceList The scene objects (including root nodes)
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final List<Intersectable> surfaceList, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shadows and use it in your lighting computation
		//Create a shadowray and see if it intersect with objects in certain range.
		shadowRay.viewDirection.scaleAdd(-1, result.p, light.from);
		double t = shadowRay.viewDirection.length();
		shadowRay.viewDirection.normalize();
		shadowRay.eyePoint=new Point3d();
		shadowRay.eyePoint.scaleAdd(1e-10,shadowRay.viewDirection,result.p);
		for(Intersectable surface:surfaceList) {
			surface.intersect(shadowRay, shadowResult);
		}

		return shadowResult.t<t&&shadowResult.t>0;
	}    


	class RenderThread extends Thread {
		int h;
		int w;
		Scene scene;
		Camera cam;
		int index;
		
		RenderThread(Scene scene, Camera cam, int w, int h, int index){
			this.scene = scene;
			this.cam = cam;
			this.w = w;
			this.h = h;
			this.index = index;
		}
		
		public void run() {
			//Initiate params with sampling
	        int upperHeight = index*h/scene.threadCount,
	        		lowerHeight = (index+1)*h/scene.threadCount;
	        if(index==scene.threadCount-1)
	        	lowerHeight = h;
	        int sampleCountH = (int) Math.sqrt(render.samples),
	        		sampleCountW = render.samples/sampleCountH;
	        
	        for ( int i = upperHeight; i < lowerHeight && !render.isDone(); i++ ) {
	            for ( int j = 0; j < w && !render.isDone(); j++ ) {
	            	// TODO: Objective 8: do antialiasing by sampling more than one ray per pixel
	            	List<Ray> rayList = new ArrayList<Ray>();
	            	for(int m=0;m<sampleCountH;m++) {
	            		int sampleW = (m!=sampleCountH-1)?sampleCountW:render.samples-sampleCountH*sampleCountW+sampleCountW;
	            		for(int n=0;n<sampleW;n++) {
	            			if(render.jitter) {
	            				offset[0] = (m+Math.random())/(sampleCountH)-0.5;
	                			offset[1] = (n+Math.random())/(sampleW)-0.5;
	            			}else {
	            				offset[0] = (m+0.5f)/(sampleCountH)-0.5;
	                			offset[1] = (n+0.5f)/(sampleW)-0.5;
	            			}
	                        // TODO: Objective 1: generate a ray (use the generateRay method)
	            			if(blur) {
	            				Ray ray = new Ray();
		                    	generateRay(i, j, offset, cam, ray);
		                    	Point3d focalPoint = new Point3d();
		                    	focalPoint.scaleAdd(focalLength, ray.viewDirection, ray.eyePoint);
		            			for(int k=0;k<blurSamples;k++) {
		            				Point2d p = new Point2d();
		            				fastPossonDisk.get(p, k, blurSamples);
		            				Camera temp = new Camera(cam);
		            				temp.setCameraFrom(temp.from.x+p.x/5f, temp.from.y+p.y/5f, temp.from.z);
		            				Ray focalRay = new Ray();
		            				focalRay.viewDirection.scaleAdd(-1, temp.from, focalPoint);
		            				focalRay.viewDirection.normalize();
		            				focalRay.eyePoint.set(temp.from);
			                    	rayList.add(focalRay);
		            			}
	            			}else {
		            			Ray ray = new Ray();
		                    	generateRay(i, j, offset, cam, ray);
		                    	rayList.add(ray);
	            			}
	            		}
	            	}
	            	
	            	Color3f c = new Color3f();
	            	
	                // TODO: Objective 2: test for intersection with scene surfaces
	            	for(Ray ray:rayList) {
	            		IntersectResult result = new IntersectResult();
	                	for(Intersectable surface:surfaceList) {
	                		surface.intersect(ray, result);
	                	}
	                	// TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
	                	if(result.t != Double.POSITIVE_INFINITY) {
	                		Color4f color = computeLighting(result, ray, reflectionLevel);
	                		c.x += color.x;
	                		c.y += color.y;
	                		c.z += color.z;
	                    }else {
	                    	c = render.bgcolor;
	                    }
	            	}

	            	c.scale((float) (1.0f/(render.samples*blurSamples)));
	            	// Here is an example of how to calculate the pixel value.
	            	int r = (int)(255*c.x)<255?(int)(255*c.x):255;
	                int g = (int)(255*c.y)<255?(int)(255*c.y):255;
	                int b = (int)(255*c.z)<255?(int)(255*c.z):255;
	            	int a = 255;
	            
	                int argb = (a<<24 | r<<16 | g<<8 | b);  
	                // update the render image
	                render.setPixel(j, i, argb);
	            }
	        }

		}
	}
	
}

