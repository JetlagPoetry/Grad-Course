package comp557.a4;

import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A factory class to generate raytracer objects from XML definition. 
 */
public class Parser {

	/**
	 * Create a scene.
	 */
	public static Scene createScene(Node dataNode) {
		Scene scene = new Scene();
		Node ambientAttr = dataNode.getAttributes().getNamedItem("ambient");
        if( ambientAttr != null ) {
        	Scanner s = new Scanner( ambientAttr.getNodeValue());
            float x = s.nextFloat();
            float y = s.nextFloat();
            float z = s.nextFloat();
            scene.ambient.set(x, y, z);   
            s.close();
        }
        Node threadCountAttr = dataNode.getAttributes().getNamedItem("thread");
        if ( threadCountAttr != null ) {
            scene.threadCount = Integer.parseInt( threadCountAttr.getNodeValue() );        	
        }
    	Node blurAttr = dataNode.getAttributes().getNamedItem("blur");
		if ( blurAttr != null ) {
			if("true".equals(blurAttr.getNodeValue())) {
				scene.blur = true;
				Node blurSamplesAttr = dataNode.getAttributes().getNamedItem("blursample");
		        if ( blurSamplesAttr != null ) {
		            scene.blurSamples = Integer.parseInt( blurSamplesAttr.getNodeValue() );        	
		        }
				Node focalLengthAttr = dataNode.getAttributes().getNamedItem("focallength");
		        if ( focalLengthAttr != null ) {
		            scene.focalLength = Integer.parseInt( focalLengthAttr.getNodeValue() );        	
		        }
			}
		}
        NodeList nodeList = dataNode.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
            String nodeName = n.getNodeName();
            if ( nodeName.equalsIgnoreCase( "material" ) ) {                
                Material material = Parser.createMaterial(n);
                Material.materialMap.put( material.name, material );
            } else if ( nodeName.equalsIgnoreCase( "light" ) ) {                
                Light light = Parser.createLight(n);
                scene.lights.put( light.name, light);
            } else if ( nodeName.equalsIgnoreCase( "render" ) ) {                
                scene.render = Parser.createRender(n);
            } else if ( nodeName.equalsIgnoreCase( "node" ) ) {
            	scene.surfaceList.add( Parser.createSceneNode(n) );
            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
        		Plane plane = Parser.createPlane(n);
        		scene.surfaceList.add( plane );
            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
        		Box box = Parser.createBox(n);
        		scene.surfaceList.add( box );
            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
        		Sphere sphere = Parser.createSphere(n);
        		scene.surfaceList.add( sphere );
            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
            	Mesh mesh = Parser.createMesh(n);
            	scene.surfaceList.add( mesh );
            } else if ( nodeName.equalsIgnoreCase( "quadric" ) ) {
            	Quadric quadric = Parser.createQuadric(n);
            	scene.surfaceList.add( quadric );
            }
        }
       
        return scene;
	}
	
	/**
	 * Create a scenegraph node.
	 */
	public static SceneNode createSceneNode(Node dataNode) {
		SceneNode sceneNode = new SceneNode();
        sceneNode.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();		
		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
		if ( refAttr != null ) {
			// add references to all child nodes and geometries
			//
			SceneNode other = SceneNode.nodeMap.get( refAttr.getNodeValue() );
			if ( other != null ) {
				for (Intersectable s : other.children) {
					sceneNode.children.add(s);
				}
			}
		} else {
	        // create geometries for this node.
			//
	        NodeList nodeList = dataNode.getChildNodes();
	        for ( int i = 0; i < nodeList.getLength(); i++ ) {
	            Node n = nodeList.item(i);
	            // skip all text, just process the ELEMENT_NODEs
	            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
	            String nodeName = n.getNodeName();
	        	if ( nodeName.compareToIgnoreCase( "node") == 0 ) {
	                SceneNode childNode = Parser.createSceneNode(n) ;
	                sceneNode.children.add( childNode );
	            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
	        		Plane plane = Parser.createPlane(n);
	        		sceneNode.children.add( plane );
	            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
	        		Box box = Parser.createBox(n);
	        		sceneNode.children.add( box );
	            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
	        		Sphere sphere = Parser.createSphere(n);
	        		sceneNode.children.add( sphere );
	            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
	            	Mesh mesh = Parser.createMesh(n);
	            	sceneNode.children.add( mesh );
	            } else if ( nodeName.equalsIgnoreCase( "quadric" ) ) {
	            	Quadric quadric = Parser.createQuadric(n);
	            	sceneNode.children.add( quadric );
	            }
	        }	        
            if ( !SceneNode.nodeMap.containsKey(sceneNode.name) ) {
            	SceneNode.nodeMap.put( sceneNode.name, sceneNode );
            } else {
            	System.err.println("Parser.createSceneNode(): node with name " + sceneNode.name + " already exists!");
            }	        
		}
		
		// Build the scene node transform.
		sceneNode.M.setIdentity();        
		Node translationAttr = dataNode.getAttributes().getNamedItem("translation");
		if ( translationAttr != null ) {
        	Scanner s = new Scanner( translationAttr.getNodeValue() );
        	double x = s.nextDouble();
        	double y = s.nextDouble();
        	double z = s.nextDouble();
            s.close(); 
        	Vector3d t = new Vector3d(x,y,z);
        	Matrix4d T = new Matrix4d();
        	T.set(t);
        	sceneNode.M.mul(T);
		}		
		Node rotationAttr = dataNode.getAttributes().getNamedItem("rotation");
		if ( rotationAttr != null ) {
        	Scanner s = new Scanner( rotationAttr.getNodeValue() );
        	double degX = s.nextDouble();
        	double degY = s.nextDouble();
        	double degZ = s.nextDouble();
            s.close(); 
        	Matrix4d R = new Matrix4d();
        	R.rotX( Math.toRadians(degX) );
        	sceneNode.M.mul(R);
        	R.rotY( Math.toRadians(degY) );
        	sceneNode.M.mul(R);
        	R.rotZ( Math.toRadians(degZ) );
        	sceneNode.M.mul(R);
		}
		Node scaleAttr = dataNode.getAttributes().getNamedItem("scale");
		if ( scaleAttr != null ) {
            Scanner s = new Scanner( scaleAttr.getNodeValue() );   
            Matrix4d S = new Matrix4d();
            S.setIdentity();
            S.setElement(0,0,s.nextDouble());
            S.setElement(1,1,s.nextDouble());
            S.setElement(2,2,s.nextDouble());
            sceneNode.M.mul( S );
            s.close(); 
		}				
		// cache the inverse matrix, since we only need to compute it once!
		sceneNode.Minv.invert(sceneNode.M);		
		sceneNode.material = parseMaterial(dataNode, "material");					
		return sceneNode;	
	}
	
	/**
	 * Create a light.
	 */
	public static Light createLight(Node dataNode) {
		Light light = new Light();
        light.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();        
        Node colorAttr = dataNode.getAttributes().getNamedItem("color");
        if ( colorAttr != null ) {
        	Scanner s = new Scanner( colorAttr.getNodeValue());
        	float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
            float a = (s.hasNextFloat() ? s.nextFloat() : 0);
            light.color.set(r,g,b,a);   
            s.close();    	
        }
        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
        if ( fromAttr != null ) {
        	Scanner s = new Scanner( fromAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            light.from.set(x,y,z); 
            s.close();
        }
        Node powerAttr = dataNode.getAttributes().getNamedItem("power");
        if ( powerAttr != null ) {
        	light.power = Double.parseDouble( powerAttr.getNodeValue() );
        }
        Node typeAttr = dataNode.getAttributes().getNamedItem("type");
        if ( typeAttr != null ) {
        	light.type = typeAttr.getNodeValue();
        	if("direction".equals(light.type)) {
        		Node sourceCount = dataNode.getAttributes().getNamedItem("sourcecount");
                if ( sourceCount != null ) {
                    light.sourceCount = Integer.parseInt( sourceCount.getNodeValue() );        	
                }
        	}
        }        
		return light;
	}
	
	/**
	 * Create a camera.
	 */
	public static Camera createCamera(Node dataNode) {
		Camera camera = new Camera();			
		Node nameAttr = dataNode.getAttributes().getNamedItem("name");
		if ( nameAttr != null ) {
			camera.name = nameAttr.getNodeValue();
		}
        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
        if ( fromAttr != null ) {
        	Scanner s = new Scanner( fromAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.from.set(x,y,z);     
            s.close();
        }
        Node toAttr = dataNode.getAttributes().getNamedItem("to");
        if ( toAttr != null ) {
        	Scanner s = new Scanner( toAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.to.set(x,y,z);     
            s.close();
        }
        Node upAttr = dataNode.getAttributes().getNamedItem("up");
        if ( upAttr != null ) {
        	Scanner s = new Scanner( upAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.up.set(x,y,z);
            s.close();
        }
        Node fovAttr = dataNode.getAttributes().getNamedItem("fovy");
        if ( fovAttr != null ) {
            camera.fovy = Double.parseDouble( fovAttr.getNodeValue() );       	
        }
        Node widthAttr = dataNode.getAttributes().getNamedItem("width");
        if ( widthAttr != null ) {
            camera.imageSize.width = Integer.parseInt( widthAttr.getNodeValue() );        	
        }
        Node heightAttr = dataNode.getAttributes().getNamedItem("height");
        if ( heightAttr != null ) {
            camera.imageSize.height = Integer.parseInt( heightAttr.getNodeValue() );        	
        }
        
		return camera;
	}
	
	/**
	 * Create a material.
	 */
	public static Material createMaterial(Node dataNode) {
		Material material;
		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
		if( refAttr  != null ) {
			material = Material.materialMap.get( refAttr.getNodeValue() );
		} else {
			material = new Material();			
	    	Node nameAttr = dataNode.getAttributes().getNamedItem("name");
	    	if ( nameAttr != null ) {
	    		material.name = nameAttr.getNodeValue();
	    	}
	    	Node diffuseAttr = dataNode.getAttributes().getNamedItem("diffuse");
	    	if ( diffuseAttr != null ) {
	        	Scanner s = new Scanner( diffuseAttr.getNodeValue() );
	            float r = s.nextFloat();
	            float g = s.nextFloat();
	            float b = s.nextFloat();
	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
	            material.diffuse.set(r,g,b,a);
	            s.close();
	    	}
	    	Node specularAttr = dataNode.getAttributes().getNamedItem("specular");
	    	if ( specularAttr != null ) {
	        	Scanner s = new Scanner( specularAttr.getNodeValue());
	            float r = s.nextFloat();
	            float g = s.nextFloat();
	            float b = s.nextFloat();
	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
	            material.specular.set(r,g,b,a);   
	            s.close();
	    	}
	    	Node hardnessAttr = dataNode.getAttributes().getNamedItem("hardness");
	    	if ( hardnessAttr != null ) {
	    		material.shinyness = Float.parseFloat( hardnessAttr.getNodeValue() );
	    	}
	    	Node typeAttr = dataNode.getAttributes().getNamedItem("type");
	    	if ( typeAttr != null ) {
	    		material.type = typeAttr.getNodeValue();
	    	}
		}
		return material;
	}

	/**
	 * Create a renderer.
	 */
	public static Render createRender(Node dataNode) {
		Render render = new Render();
		Node outputAttr = dataNode.getAttributes().getNamedItem("output");
		if ( outputAttr != null ) {
			render.output = outputAttr.getNodeValue();
		}
		Node bgcolorAttr = dataNode.getAttributes().getNamedItem("bgcolor");
		if ( bgcolorAttr != null ) {
        	Scanner s = new Scanner( bgcolorAttr.getNodeValue());
            float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
			render.bgcolor.set(r,g,b);
			s.close();
		}		
		Node samplesAttr = dataNode.getAttributes().getNamedItem("samples");
		if ( samplesAttr != null ) {
        	Scanner s = new Scanner( samplesAttr.getNodeValue());
            render.samples = s.nextInt(); 
			s.close();
		}
    	NodeList nodeList = dataNode.getChildNodes();
    	for (int i = 0; i < nodeList.getLength(); i++) {
    		Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
    		String name = n.getNodeName();
			if ( name.equalsIgnoreCase("camera") ) {
				render.camera = Parser.createCamera(n);
    		}
    	}	
    	//Jitter Parser
    	Node jitterAttr = dataNode.getAttributes().getNamedItem("jitter");
		if ( jitterAttr != null ) {
			if("true".equals(jitterAttr.getNodeValue())) {
				render.jitter = true;
			}
		}
		return render;
	}
	
	/**
	 * Create a plane.
	 */
	public static Plane createPlane( Node dataNode ) {
		Plane plane = new Plane();	
		plane.material = parseMaterial(dataNode, "material");	
		plane.material2 = parseMaterial(dataNode, "material2");
		return plane;
	}
	
	/**
	 * Create a sphere object.
	 */
	public static Sphere createSphere(Node dataNode) {
		Sphere sphere = new Sphere();
		Node centerAttr = dataNode.getAttributes().getNamedItem("center");
		if ( centerAttr != null ) {
            Scanner s = new Scanner( centerAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            sphere.center = new Point3d(x, y, z);
            s.close();
		}
		Node radiusAttr = dataNode.getAttributes().getNamedItem("radius");
		if ( radiusAttr != null ) {
			sphere.radius = Double.parseDouble( radiusAttr.getNodeValue() );
		}
		sphere.material = parseMaterial(dataNode, "material");	
    	return sphere;
	}

	/**
	 * Create a box object.
	 */
	public static Box createBox(Node dataNode) {
		Box box = new Box();
		Node minAttr = dataNode.getAttributes().getNamedItem("min");
		if ( minAttr != null ) {
            Scanner s = new Scanner( minAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            box.min = new Point3d(x, y, z);
            s.close();
		}
		Node maxAttr = dataNode.getAttributes().getNamedItem("max");
		if ( maxAttr != null ) {
            Scanner s = new Scanner( maxAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            box.max = new Point3d(x, y, z);
            s.close();
		}		
		box.material = parseMaterial(dataNode, "material");
    	return box;
	}	
	
	/**
	 * Create a quadric object.
	 */
	public static Quadric createQuadric(Node dataNode) {
		Quadric quadric = new Quadric();
		Node aaAttr = dataNode.getAttributes().getNamedItem("a2");
		if ( aaAttr != null ) {
            Scanner s = new Scanner( aaAttr.getNodeValue() );
            quadric.a2 = s.nextDouble();
            s.close();
		}
		Node bbAttr = dataNode.getAttributes().getNamedItem("b2");
		if ( bbAttr != null ) {
            Scanner s = new Scanner( bbAttr.getNodeValue() );
            quadric.b2 = s.nextDouble();
            s.close();
		}	
		quadric.material = parseMaterial(dataNode, "material");
    	return quadric;
	}	
	
	
	/**
	 * Create a mesh object.
	 */
	public static Mesh createMesh(Node dataNode) {
        Mesh mesh = new Mesh();
        mesh.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
        Node filenameAttr = dataNode.getAttributes().getNamedItem("filename");
        if ( filenameAttr != null ) {
        	mesh.soup = new PolygonSoup( filenameAttr.getNodeValue() );
        	if ( !Mesh.meshMap.containsKey(mesh.name) )
        		Mesh.meshMap.put(mesh.name, mesh);
        } else {
			String instance = dataNode.getAttributes().getNamedItem("ref").getNodeValue();
			Mesh other = Mesh.meshMap.get(instance);
			if ( other != null ) {
				mesh.soup = other.soup;
			}
        }
        mesh.material = parseMaterial(dataNode, "material");
    	return mesh;    	
	}

	/**
	 * Utility method to parse a material tag.
	 */
	private static Material parseMaterial(Node dataNode, String tagName) {
		Material material = null;
    	NodeList nodeList = dataNode.getChildNodes();
    	for (int i = 0; i < nodeList.getLength(); i++) {
    		Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
    		String name = n.getNodeName();
			if ( name.equalsIgnoreCase(tagName) ) {
    			Node refNode = n.getAttributes().getNamedItem("ref");
    			if( refNode != null ) { 
					material = Material.materialMap.get( refNode.getNodeValue() );
    			} else {
    				material = Parser.createMaterial(n);
    			}
    		}
    	}
    	return material;
	}		
}