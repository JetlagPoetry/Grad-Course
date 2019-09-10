using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Turkey : MonoBehaviour {

    LineRenderer lineRenderer;
    SpriteRenderer wall;
    Wind wind;
    Mountain mountain;
    TurkeyCreator turkeyCreator;

    public List<Vector3> points = new List<Vector3>();
    private List<Vector3> original = new List<Vector3>();
    private float ax, ay;
    public Vector3 origin = new Vector3(0, 0, 0);
    float slide_timer = 2f, leap_timer = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 300) / 100f + 4f,
        gravity = -0.3f, slide_speed = 0.2f;
    public float scale = 0.2f;
    public Vector3 initiate_point = new Vector3(-10f, 1.1f, 0);
    float mountain_span;

    // Use this for initialization
    void Start() {
        //Get Components and Objects
        lineRenderer = GetComponent<LineRenderer>();
        wall = GameObject.FindGameObjectWithTag("Wall").GetComponent<SpriteRenderer>();
        wind = GameObject.FindGameObjectWithTag("Wind").GetComponent<Wind>();
        mountain = GameObject.FindGameObjectWithTag("Mountain").GetComponent<Mountain>();
        turkeyCreator = GameObject.FindGameObjectWithTag("TurkeyCreator").GetComponent<TurkeyCreator>();

        //Set and draw all initial points
        CreateTurkey();

        //Give turkey random initiate point.x in (-11f,-9f)
        initiate_point = new Vector3(new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 200) / 100f - 11f, 1f, 0);
        transform.position = initiate_point;

        //Give turkey initial accelarations in random directions
        ax = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 2) == 1 ? slide_speed : -slide_speed;
        ay = 0;
    }

    // Update is called once per frame
    void Update() {
        //Check if collide with wall, ground or mountain
        if (CollideWithGround()) 
        {
            TurkeySlide();
        }else if (CollideWithMountain())
        {
            TurkeyDownHill();
        }
        else
        {
            ay = gravity;
        }
        CollideWithWall();
        //Leap straight up every few seconds
        TurkeyLeap();
        //Calculate current turkey position
        TurkeyMove();
        //Check all constraints, if not maintained, add correction
        MaintainConstraints();
        //Draw Turkey
        DrawTurkey();
    }

    void CreateTurkey()
    {
        points.Add(new Vector3(-6, 0, 0));
        points.Add(new Vector3(-5, 1, 0));
        points.Add(new Vector3(-5, 2, 0));
        points.Add(new Vector3(-4, 2, 0));
        points.Add(new Vector3(-3, 0, 0));
        points.Add(new Vector3(-3, 1, 0));
        points.Add(new Vector3(-1, 3, 0));
        points.Add(new Vector3(2, 3, 0));
        points.Add(new Vector3(4, 2, 0));
        points.Add(new Vector3(5, 0, 0));
        points.Add(new Vector3(4, -2, 0));
        points.Add(new Vector3(2, -3, 0));
        points.Add(new Vector3(1, -3, 0));
        points.Add(new Vector3(1, -4, 0));
        points.Add(new Vector3(1, -5, 0));
        points.Add(new Vector3(1, -4, 0));
        points.Add(new Vector3(1, -3, 0));
        points.Add(new Vector3(-1, -3, 0));
        points.Add(new Vector3(-1, -4, 0));
        points.Add(new Vector3(-1, -5, 0));
        points.Add(new Vector3(-1, -4, 0));
        points.Add(new Vector3(-1, -3, 0));
        points.Add(new Vector3(-3, -3, 0));
        points.Add(new Vector3(-4, 0, 0));
        points.Add(new Vector3(-5, -1, 0));
        points.Add(new Vector3(-5, 0, 0));
        points.Add(new Vector3(-6, 0, 0));

        for(int i = 0; i < points.Count; i++)
        {
            original.Add(new Vector3(points[i].x, points[i].y, 0));
        }
    }

    void DrawTurkey()
    {
        //Set LineRenderer params and draw all points in a for loop
        lineRenderer.SetWidth(0.05f, 0.05f);
        lineRenderer.useWorldSpace = false;
        lineRenderer.SetVertexCount(points.Count);
        for (int i = 0; i < points.Count; i++)
        {
            lineRenderer.SetPosition(i, points[i]);
        }
    }

    float jump_timer = 0;
    void TurkeyLeap()
    {
        if (ax == 0)
            ax = slide_speed;
        //Every few seconds, take a leap
        leap_timer -= Time.deltaTime;
        jump_timer -= Time.deltaTime;
        if (leap_timer <= 0)
        {
            //Stop movement in x axis
            ax = 0;
            leap_timer = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 300) / 100f + 4f;
            jump_timer = 0.3f;
        }
        if (jump_timer > 0)
        {
            ay = 2f;
        }
    }

    void CollideWithWall()
    {
        float wall_bound = wall.transform.position.x + wall.bounds.size.x / 2;
        for (int i = 0; i < points.Count; i++)
        {
            if (wall_bound >= points[i].x * scale + initiate_point.x) 
            {
                //If hit the wall, stop all movements in x axis
                ax = 0;
                for (int j = 0; j < points.Count; j++)
                {
                    points[j] = new Vector3(points[j].x + 0.1f, points[j].y, 0);
                }
            }
        }
    }

    bool CollideWithGround()
    {
        for (int i = 0; i < points.Count; i++)
        {
            if (points[i].y * scale + initiate_point.y < 0.2f)
            {
                return true;
            }
        }
        return false;
    }

    void TurkeySlide()
    {
        ay = 0;
        float x_coordinate = origin.x * scale + initiate_point.x;
        //Restrict the sliding range
        if (x_coordinate > -mountain.half_width - 1f)
            ax = -slide_speed;
        else if (x_coordinate < wall.transform.position.x + 1f)
            ax = slide_speed;
    }

    void TurkeyDownHill()
    {
        ay = 0;
        ax = -slide_speed;
    }

    bool CollideWithMountain()
    {
        for (int i = 0; i < points.Count; i++)
        {
            mountain_span = 2 * mountain.half_width / (mountain.points.Count - 1);
            float x_coordinate = points[i].x * scale + initiate_point.x;
            float x_index = (x_coordinate + mountain.half_width) / mountain_span;
            int index = (int)Mathf.Round(x_index);
            
            if (index <= 0 || index >= mountain.points.Count)
                continue;
            if (points[i].y * scale + initiate_point.y  <= mountain.points[index].y)
            {
                //If hit right part of the mountain, destroy current turkey and instantiate a new one
                if (x_coordinate >= -1f)
                {
                    turkeyCreator.CreateNewTurkey();
                    gameObject.transform.GetComponentInChildren<TurkeyEye>().DestroyEye();
                    lineRenderer = new LineRenderer();
                    Destroy(gameObject);
                    return false;
                }
                else
                {
                    //If left part of the mountain, start moving left
                    ax = -slide_speed;
                    return true;
                }
            }
        }
        return false;
    }

    void TurkeyMove()
    {
        //Add windforce if turkey is in the sky
        if (origin.y * scale + initiate_point.y > wind.altitude)
        {
            ax += wind.wind_force * 50f;
        }

        //Apply accelaration on the origin point
        origin = new Vector3(origin.x + ax, origin.y + ay, 0);
        //Call eye to move
        gameObject.transform.GetComponentInChildren<TurkeyEye>().TurkeyMove(ax, ay);

    }

    void MaintainConstraints()
    {
        for (int i = 0; i < points.Count; i++)
        {
            Vector3 original_v = original[i];
            Vector3 current_v = points[i] - origin;

            //Check distance and angle between the origin and all other points, make correction to other points location
            points[i] = points[i] + (original_v - current_v) * (Vector3.Angle(original_v, current_v) * 0.025f 
			+ Vector3.Distance(original_v,current_v)*0.2f);
        }
    }

    public void HitByCannonball(Vector3 direction)
    {
        ax = ax - 0.15f * (direction.x > 0 ? direction.x : -direction.x) / Mathf.Abs(direction.x);
        ay = ay - 0.15f * (direction.y > 0 ? direction.y : -direction.y) / Mathf.Abs(direction.y);
    }
}
