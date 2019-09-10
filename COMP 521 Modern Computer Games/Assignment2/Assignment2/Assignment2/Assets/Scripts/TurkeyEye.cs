using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TurkeyEye : MonoBehaviour {

    LineRenderer lineRenderer;

    public List<Vector3> points;

    // Use this for initialization
    void Start()
    {
        points = new List<Vector3>();
        //Get LineRenderer Component
        lineRenderer = GetComponent<LineRenderer>();

        CreateEye();
    }

    // Update is called once per frame
    void Update()
    {
        DrawEye();
    }

    void CreateEye()
    {
        points.Add(new Vector3(-4.4f, 1, 0));
        points.Add(new Vector3(-4.6f, 1, 0));
    }

    void DrawEye()
    {
        //Set LineRenderer params and draw all points in a for loop
        lineRenderer.SetColors(Color.black, Color.black);
        lineRenderer.SetWidth(0.1f, 0.1f);
        lineRenderer.useWorldSpace = false;
        lineRenderer.SetVertexCount(points.Count);
        for (int i = 0; i < points.Count; i++)
        {
            lineRenderer.SetPosition(i, points[i]);
        }
    }

    public void TurkeyMove(float ax, float ay)
    {
        //Make the eye move
        for (int i = 0; i < points.Count; i++)
        {
            points[i] = new Vector3(points[i].x + ax, points[i].y + ay, 0);
        }
    }

    public void DestroyEye()
    {
        lineRenderer = new LineRenderer();
        Destroy(gameObject);
    }

}
