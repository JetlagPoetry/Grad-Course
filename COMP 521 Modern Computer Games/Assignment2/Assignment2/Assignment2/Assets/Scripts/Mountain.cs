using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Mountain : MonoBehaviour
{

    LineRenderer lineRenderer;

    //Make variables public to let other scripts to do collision detection
    public List<Vector3> points = new List<Vector3>();
    public int iteration = 1;
    public float half_width = 6f;
    public float height = 5f;

    void Start()
    {
        //Get LineRenderer Component
        lineRenderer = GetComponent<LineRenderer>();

        //Set three original points
        points.Add(new Vector3(-half_width, 0, 0));
        points.Add(new Vector3(0, height, 0));
        points.Add(new Vector3(half_width, 0, 0));

        //Generate terrian using midpoint displacement
        for (int i = 0; i < iteration; i++)
        {
            //with each iteration, go through every existing point
            //from the rear of the list to the head
            for (int j = points.Count - 1; j > 0; j--)
            {
                //insert another point and add a displacement on the y axis
                Vector3 previous_point = points[j - 1], next_point = points[j];

                //reduce displacement on each iteration
                //proportion to the difference between previous height and next height does no look as good
                //float displacement = (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 80) - 40) / 100f * height * Math.Abs(previous_point.y - next_point.y);
                double displacement = ((double)(new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 200) - 100) / 100d) * height * Math.Pow(0.5, i + 1);
                points.Insert(j, new Vector3((previous_point.x + next_point.x) / 2, (previous_point.y + next_point.y) / 2 + (float)displacement, 0));
            }
           
        }
        //Add two points to draw the ground
        points.Insert(0, new Vector3(-100f, 0, 0));
        points.Add(new Vector3(100f, 0, 0));
        //draw ground and mountain points
        drawMountain();
        //Delete the two outlier points afterwards
        points.RemoveAt(0);
        points.RemoveAt(points.Count - 1);
    }

    void drawMountain()
    {
        //Set LineRenderer params and draw all points in a for loop
        lineRenderer.SetColors(Color.black, Color.black);
        lineRenderer.SetWidth(0.1f, 0.1f);
        lineRenderer.useWorldSpace = true;
        lineRenderer.SetVertexCount(points.Count);
        for (int i = 0; i < points.Count; i++)
        {
            lineRenderer.SetPosition(i, points[i]);
        }
    }
    
}