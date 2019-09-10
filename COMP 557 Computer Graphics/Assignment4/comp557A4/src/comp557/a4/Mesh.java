package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 9: ray triangle intersection for meshes
		for(int[] face:soup.faceList) {
			Vector3d n = new Vector3d();
			Point3d a = new Point3d(soup.vertexList.get(face[0]).p),
					b = new Point3d(soup.vertexList.get(face[1]).p),
					c = new Point3d(soup.vertexList.get(face[2]).p);
			Vector3d aTob = new Vector3d();
			aTob.scaleAdd(-1, a, b);
			Vector3d aToc = new Vector3d();
			aToc.scaleAdd(-1, a, c);
			Vector3d bToc = new Vector3d();
			bToc.scaleAdd(-1, b, c);
			Vector3d cToa = new Vector3d();
			cToa.scaleAdd(-1, c, a);
			
			n.cross(aTob, aToc);
			n.normalize();
			
			Vector3d eyeToa = new Vector3d();
			eyeToa.scaleAdd(-1, ray.eyePoint, a);

			double t = eyeToa.dot(n)/ray.viewDirection.dot(n);
			Point3d p = new Point3d();
			p.scaleAdd(t, ray.viewDirection, ray.eyePoint);
			
			Vector3d aTop = new Vector3d();
			aTop.scaleAdd(-1, a, p);
			Vector3d bTop = new Vector3d();
			bTop.scaleAdd(-1, b, p);
			Vector3d cTop = new Vector3d();
			cTop.scaleAdd(-1, c, p);
			
			
			aTop.cross(aTob, aTop);
			bTop.cross(bToc, bTop);
			cTop.cross(cToa, cTop);
			if(n.dot(aTop)>0 && n.dot(bTop)>0 && n.dot(cTop)>0) {
				if(t<result.t && t>1e-8) {
					result.t = t;
					result.n.set(n);;
					result.p = p;
					result.material = this.material;
				}
			}
		}
	
	}
	
	
}
