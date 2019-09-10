package comp557.a3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow
 * for easy display of geometry.
 */
public class HEDS {

    /** List of faces */
    Set<Face> faces = new HashSet<Face>();
    //List of Edges
    List<Edge> edges = new ArrayList<Edge>();
    //Priority Queue of edge based on errors
    Queue<Edge> edgeQueue = new PriorityQueue<Edge>();
    //Variable of regularizationWeight
    double regularizationWeight;
    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    public HEDS( PolygonSoup soup, double regularizationWeight) {
        halfEdges.clear();
        faces.clear();
        
        // TODO: Objective 1: create the half edge data structure from the polygon soup    

        for(int[] face:soup.faceList) {
    		List<HalfEdge> faceEdges = new LinkedList<HalfEdge>();
        	for(int i=0;i<face.length;i++) {
        		HalfEdge he = new HalfEdge();
        		he.head = soup.vertexList.get(face[(i+1)%face.length]);
        		halfEdges.put(face[i]+","+face[(i+1)%face.length], he);
        		faceEdges.add(he);
        	}
        	for(int i=0;i<faceEdges.size();i++) {
        		faceEdges.get(i).next = faceEdges.get((i+1)%face.length);
        	}

        	Face f = new Face(faceEdges.get(0));
        	faces.add(f);
        }
        
        Iterator<Entry<String, HalfEdge>> it = halfEdges.entrySet().iterator();
        while(it.hasNext()) {
        	//Match up HalfEdge pairs
        	String he = it.next().getKey();
        	int comma = he.indexOf(",");
        	String twin = he.substring(comma+1)+","+he.substring(0,comma);
        	if(halfEdges.get(twin)==null)
        		continue;
        	halfEdges.get(he).twin = halfEdges.get(twin);
        	halfEdges.get(twin).twin = halfEdges.get(he);
        	
        	//create new edge
        	Edge edge = new Edge();
        	edge.he = halfEdges.get(he);
        	halfEdges.get(he).e = edge;
        	halfEdges.get(twin).e = edge;
        	edges.add(edge);
        	//Remove current halfedge to avoid duplicate definition when iterating its twin
        	it.remove();
        }
        
        // TODO: Objective 5: fill your priority queue on load
        this.regularizationWeight = regularizationWeight;
        loadPriorityQueue();
    }

    /**
     * You might want to use this to match up half edges... 
     */
    Map<String,HalfEdge> halfEdges = new TreeMap<String,HalfEdge>();
    
    
    // TODO: Objective 2, 3, 4, 5: write methods to help with collapse, and for checking topological problems
    HalfEdge collapse(HalfEdge halfEdge) {
    	while(!redoListHalfEdge.isEmpty()) {
    		redoCollapse();
    	}
    	//Collapse
    	HalfEdge a = halfEdge.next.twin, b = halfEdge.next.next.twin, 
    			c = halfEdge.twin.next.next.twin, d = halfEdge.twin.next.twin;
    	a.twin = b;
    	b.twin = a;
    	c.twin = d;
    	d.twin = c;
    	faces.remove(halfEdge.leftFace);
    	faces.remove(halfEdge.twin.leftFace);
    	//Deal with edges
    	edgeQueue.remove(halfEdge.e);
    	edgeQueue.remove(a.e);
    	edgeQueue.remove(b.e);
    	edgeQueue.remove(c.e);
    	edgeQueue.remove(d.e);
    	Edge newEdge1 = new Edge();
    	newEdge1.he = a;
    	a.e = newEdge1;
    	b.e = newEdge1;
    	Edge newEdge2 = new Edge();
    	newEdge2.he = c;
    	c.e = newEdge2;
    	d.e = newEdge2;
    	//Add to undoList
    	undoList.add(halfEdge);
    	//Set new vertex and recompute each face normal
    	Vertex optimalV = new Vertex();
    	optimalV.p = new Point3d(halfEdge.e.v.x,halfEdge.e.v.y,halfEdge.e.v.z);
    	HalfEdge temp = a;
    	do {
    		temp.head = optimalV;
    		temp.leftFace.recomputeNormal();	//recompute adjacent face normal
    		temp = temp.next.twin;
    	}while(temp!=a);
    	//compute new vertex Q
    	computeVertexQ(a);
    	//Update adjacent edge Q and error
    	temp = a;
    	do {
    		computeEdgeError(temp.e);
    		temp = temp.next.twin;
    	}while(temp!=a);
    	//Add new edges to the queue so as to update queue order
    	edgeQueue.add(newEdge1);
    	edgeQueue.add(newEdge2);
    	return edgeQueue.poll().he;
    }
    
    //Check topological problems
    boolean noMoreCollapse(HalfEdge he){
    	if(faces.size()<=4)
    		return true;
    	Set<Vertex> set1 = findAdjacentVertex(he);
    	Set<Vertex> set2 = findAdjacentVertex(he.twin);
    	
    	set1.retainAll(set2);
    	if(set1.size()>2)
    		return true;
    	return false;
    }
    
    //Find 1-rings of Vertex with a HalfEdge
    private Set<Vertex> findAdjacentVertex(HalfEdge he){
    	Set<Vertex> set = new HashSet<Vertex>();
    	HalfEdge loop = he;
    	do {
    		set.add(loop.next.head);
    		loop = loop.next.twin;
    	}while(loop!=he && loop!=null);
    	if(loop==null) {
    		loop = he;
    		while(loop.twin!=null) {
    			set.add(loop.twin.head);
    			loop = loop.twin.next.next;
    		}
    		set.add(loop.next.next.head);
    	}
    	return set;
    }
    
    //Compute the error metric of Vertex with a HalfEdge
    private void computeVertexQ(HalfEdge he) {
    	HalfEdge loop = he;
    	Matrix4d q = new Matrix4d();
    	do {
    		q.add(loop.leftFace.K);
    		loop = loop.next.twin;
    	}while(loop!=he && loop!=null);
    	//Deal with boundary
    	if(loop==null) {
    		loop = he;
    		while(loop.twin!=null) {
    			q.add(loop.twin.leftFace.K);
    			loop = loop.twin.next.next;
    		}
    	}
    	he.head.Q = q;
    }
    
    //Compute Q, optimal v and error for edge
    private void computeEdgeError(Edge edge) {
    	//Compute edge Q
		HalfEdge halfEdge = edge.he;
    	edge.Q.add(halfEdge.head.Q, halfEdge.twin.head.Q);
    	//Regularization
    	Matrix4d reg = new Matrix4d();
    	reg.setIdentity();
    	Vector3d mid = new Vector3d();
    	mid.add(halfEdge.head.p, halfEdge.twin.head.p);
    	mid.scale(0.5);
    	Vector4d regVector = new Vector4d(mid.x,mid.y,mid.z,mid.dot(mid));
    	reg.setColumn(3, regVector);
    	reg.setRow(3, regVector);	
    	reg.mul(regularizationWeight);
    	edge.Q.add(reg);

    	//Compute current optimal vector using -A(-1)b
    	Matrix4d quadricError = edge.Q;
    	Matrix3d A = new Matrix3d(quadricError.m00,quadricError.m01,quadricError.m02,quadricError.m10,quadricError.m11,
    			quadricError.m12,quadricError.m20,quadricError.m21,quadricError.m22);
    	Matrix3d B = new Matrix3d();
    	B.setColumn(0, new Vector3d(quadricError.m03,quadricError.m13,quadricError.m23));
    	A.invert();
    	A.mul(-1);
    	A.mul(B);
    	Vector3d optimalVector = new Vector3d();
    	A.getColumn(0, optimalVector);
    	Vector4d v = new Vector4d(optimalVector.x,optimalVector.y,optimalVector.z,1);
    	edge.v = v;
    	//edge.v = new Vector4d(0.5*(halfEdge.head.p.x+halfEdge.twin.head.p.x),0.5*(halfEdge.head.p.y+halfEdge.twin.head.p.y),0.5*(halfEdge.head.p.z+halfEdge.twin.head.p.z),1);
    	
		//compute error using vT Q v
		Matrix4d temp1 = new Matrix4d();
		temp1.setColumn(0, v);
		temp1.mul(halfEdge.e.Q,temp1);
		Vector4d temp2 = new Vector4d();
		temp1.getColumn(0, temp2);
		edge.error = v.dot(temp2);
    }
    
    public HalfEdge nextCollapseEdge() {
    	if(edgeQueue.isEmpty())
    		return null;
    	return edgeQueue.poll().he;
    }
    
    public HalfEdge getCollapseEdge() {
    	if(edgeQueue.isEmpty())
    		return null;
    	return edgeQueue.peek().he;
    }
    
    //Initiate the priority queue of edges
    private void loadPriorityQueue() {
    	for(Edge edge:edges) {
        	computeVertexQ(edge.he);
        	computeVertexQ(edge.he.twin);
    		computeEdgeError(edge);
    		edgeQueue.add(edge);
    	}
    }
    
    public void setRegularizationWeight(double regularizationWeight) {
    	this.regularizationWeight = regularizationWeight;
    }

    /**
	 * Need to know both verts before the collapse, but this information is actually 
	 * already stored within the excized portion of the half edge data structure.
	 * Thus, we only need to have a half edge (the collapsed half edge) to undo
	 */
	LinkedList<HalfEdge> undoList = new LinkedList<>();
	/**
	 * To redo an undone collapse, we must know which edge to collapse.  We should
	 * likewise reuse the Vertex that was created for the collapse.
	 */
	LinkedList<HalfEdge> redoListHalfEdge = new LinkedList<>();
	LinkedList<Vertex> redoListVertex = new LinkedList<>();

    void undoCollapse() {
    	if ( undoList.isEmpty() ) return; // ignore the request
    	HalfEdge he = undoList.removeLast();
    	// TODO: Objective 6: undo the last collapse
    	// be sure to put the information on the redo list so you can redo the collapse too!
    	redoListHalfEdge.add(he);
    	redoListVertex.add(he.next.twin.head);
    	
    	//Start undoCollapse
    	//Match up twins
    	he.next.twin.twin = he.next;
    	he.next.next.twin.twin = he.next.next;
    	he.twin.next.twin.twin = he.twin.next;
    	he.twin.next.next.twin.twin = he.twin.next.next;
    	//Reset halfEdge heads and recompute face normals
    	HalfEdge temp = he.next.twin;
    	do {
    		temp.head= he.head;
    		temp.leftFace.recomputeNormal();
    		temp = temp.next.twin;
    	}while(temp!=he);
    	temp = he.twin.next.twin;
    	do {
    		temp.head = he.twin.head;
    		temp.leftFace.recomputeNormal();
    		temp = temp.next.twin;
    	}while(temp!=he.twin);
    	faces.add(he.leftFace);
    	faces.add(he.twin.leftFace);
    }
    
    void redoCollapse() {
    	if ( redoListHalfEdge.isEmpty() ) return; // ignore the request
    	
    	HalfEdge he = redoListHalfEdge.removeLast();
    	Vertex v = redoListVertex.removeLast();
    	
    	// put this on the undo list so we can undo this collapse again
    	undoList.add( he );  

    	// TODO: Objective 7: undo the edge collapse!
    	HalfEdge a = he.next.twin, b = he.next.next.twin, 
    			c = he.twin.next.next.twin, d = he.twin.next.twin;
    	a.twin = b;
    	b.twin = a;
    	c.twin = d;
    	d.twin = c;
    	faces.remove(he.leftFace);
    	faces.remove(he.twin.leftFace);
    	HalfEdge temp = a;
    	do {
    		temp.head = v;
    		temp.leftFace.recomputeNormal();	//recompute adjacent face normal
    		temp = temp.next.twin;
    	}while(temp!=a);
    }
      
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // we do not assume triangular faces here        
        Point3d p;
        Vector3d n;        
        for ( Face face : faces ) {
            HalfEdge he = face.he;
            gl.glBegin( GL2.GL_POLYGON );
            n = he.leftFace.n;
            gl.glNormal3d( n.x, n.y, n.z );
            HalfEdge e = he;
            do {
                p = e.head.p;
                gl.glVertex3d( p.x, p.y, p.z );
                e = e.next;
            } while ( e != he );
            gl.glEnd();
        }
        
    }

}