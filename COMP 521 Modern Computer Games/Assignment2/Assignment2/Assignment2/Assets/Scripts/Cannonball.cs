using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Cannonball : MonoBehaviour {

    Cannon cannon;
    Wind wind;
    Mountain mountain;
    SpriteRenderer wall;
    GameObject[] turkey_list;
    float initialv = 0.18f, vx, vy, ax, ay;
    float radius, mountain_span;
    float timer = 0.2f;

    // Use this for initialization
    void Start () {
        //Find GameObject
        cannon = GameObject.FindGameObjectWithTag("Cannon").GetComponent<Cannon>();
        wind = GameObject.FindGameObjectWithTag("Wind").GetComponent<Wind>();
        mountain = GameObject.FindGameObjectWithTag("Mountain").GetComponent<Mountain>();
        wall = GameObject.FindGameObjectWithTag("Wall").GetComponent<SpriteRenderer>();
        
        radius = gameObject.GetComponent<SpriteRenderer>().bounds.size.x / 2;
        

        //Initiate cannonball velocity and accelerations
        float angle = cannon.angle;
        vx = -initialv * Mathf.Cos(angle * Mathf.Deg2Rad);
        vy = initialv * Mathf.Sin(angle * Mathf.Deg2Rad);
        ay = -0.00098f;
	}

    // Update is called once per frame
    void Update() {
        //If the cannonball is higher than some altitude,get current wind_force_acceleration
        if (gameObject.transform.position.y >= wind.altitude) ax = wind.wind_force;
        else ax = 0f;
        //Compute current velocity and position of cannonball
        vx = vx + ax * 1f;
        vy = vy + ay * 1f;

        //Do collision detection with wall and mountain
        timer -= Time.deltaTime;
        if (CollideWithWall() && timer <= 0) 
        {
            CollisionResolutionWithWall();
            timer = 0.2f;
        }

        int i = CollideWithMountain();
        if (i != 0 && timer <= 0)
        {
            CollisionResolutionWithMountain(i);
            timer = 0.2f;
        }

        //Make the cannonball disappear
        //If collide with the ground, make the cannonball disappear
        //If exceed screen bounds, make the cannonball disappear
        //If stop moving, make the cannonball disappear
        if (CollideWithGround() || ExceedScreenBounds() || StopMoving())
        {
            Destroy(gameObject);
        }

        gameObject.transform.position = new Vector3(transform.position.x + vx * 1f, transform.position.y + vy * 1f, 0);

        CollisionWithTurkey();
    }

    bool CollideWithWall()
    {
        //If the left bound of the cannonball is smaller than the right bound of the wall, consider it as collision
        float cannonball_bound = transform.position.x - gameObject.GetComponent<SpriteRenderer>().bounds.size.x / 2;
        float wall_bound = wall.transform.position.x + wall.bounds.size.x / 2;
        //If collide, make the cannonball bounce with a restitution
        if (wall_bound >= cannonball_bound)
        {
            return true;
        }
        return false;
    }

    int CollideWithMountain()
    {
        //If cannonball does not reach the outer mountain region, return directly
        if (transform.position.x - radius > mountain.half_width || 
            transform.position.x + radius < -mountain.half_width || 
            transform.position.y - radius > mountain.height)
            return 0;
        mountain_span = 2 * mountain.half_width / (mountain.points.Count - 1);
        //Convert cannonball position of x into index range of the mountain points
        float left_index = Mathf.Floor((transform.position.x - radius + mountain.half_width) / mountain_span);
        float right_index = Mathf.Ceil((transform.position.x + radius + mountain.half_width) / mountain_span);
        if (left_index <= 0) left_index = 1;
        if (right_index >= mountain.points.Count - 1) right_index = mountain.points.Count - 2;

        //Search for each line segment if the cannonball intersects with it
        for(int i = (int)left_index; i <= right_index; i++)
        {
            //If collide, make the cannonball bounce
            //Angles alpha refers to the intersection angle between the line segment and horizontal line
            //Beta refers to the intersection angle between the current velocity and horizontal line
            if (Mathf.Pow(mountain.points[i].x - transform.position.x, 2) + Mathf.Pow(mountain.points[i].y - transform.position.y, 2) <= Mathf.Pow(radius, 2))
                return i;
        }
        return 0;
    }

    void CollisionResolutionWithWall()
    {
        float rand = (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 46) + 50) / 100f;
        vx = -vx * rand;
        vy = vy * rand;
    }

    void CollisionResolutionWithMountain(int i)
    {
        float alpha = Mathf.Atan2(mountain.points[i + 1].y - mountain.points[i - 1].y,
            mountain.points[i + 1].x - mountain.points[i - 1].x == 0 ? 0.0001f : mountain.points[i + 1].x - mountain.points[i - 1].x);
        float v0 = Mathf.Sqrt(vx * vx + vy * vy);
        float v1 = v0 * (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 46) + 50) / 100f;

        float beta = Mathf.Atan2(vy, vx == 0 ? 0.0001f : vx);
        vx = v1 * Mathf.Cos(alpha * 2 + beta);
        vy = v1 * Mathf.Sin(alpha * 2 + beta);
      
    }

    bool CollideWithGround()
    {
        //ground Y is 0f
        if (transform.position.y <= 0f)
            return true;
        return false;
    }

    bool ExceedScreenBounds()
    {
        if (transform.position.x >= 14f || transform.position.y >= 12f)
            return true;
        return false;
    }

    bool StopMoving()
    {
        //return true if velocity and acceleration from all directions are 0
        if (vx == 0 && vy == 0 && ax == 0 && ay == 0)
            return true;
        return false;
    }

    void CollisionWithTurkey()
    {
        turkey_list = GameObject.FindGameObjectsWithTag("Turkey");
        for (int i = 0; i < turkey_list.Length; i++)
        {
            Turkey turkey = turkey_list[i].GetComponent<Turkey>();
            for (int j = 0; j < turkey.points.Count; j++)
            {
                float distance = Mathf.Sqrt(Mathf.Pow(turkey.points[j].x * turkey.scale + turkey.initiate_point.x - transform.position.x, 2) +
                    Mathf.Pow(turkey.points[j].y * turkey.scale + turkey.initiate_point.y - transform.position.y, 2));
                if (distance <= radius)
                {
                    turkey.HitByCannonball(turkey.origin - transform.position);
                    Destroy(gameObject);
                }
            }
        }
    }
}
