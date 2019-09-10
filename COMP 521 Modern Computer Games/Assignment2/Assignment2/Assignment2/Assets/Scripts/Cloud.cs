using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Cloud : MonoBehaviour {

    float vx, ax;
    Wind wind;
    SpriteRenderer wall;

    // Use this for initialization
    void Start () {
        //Find GameObject
        wind = GameObject.FindGameObjectWithTag("Wind").GetComponent<Wind>();
        wall = GameObject.FindGameObjectWithTag("Wall").GetComponent<SpriteRenderer>();
    }

    // Update is called once per frame
    void Update () {
        //Get current wind_force_acceleration
        ax = wind.wind_force;
        //Compute current velocity and position of cannonball
        vx = vx + ax * 1f;
        //If reached the wall, stop moving
        float wall_bound = wall.transform.position.x + wall.bounds.size.x / 2;
        if (transform.position.x- gameObject.GetComponent<SpriteRenderer>().bounds.size.x / 2<= wall_bound)
        {
            vx = 0;
        }
        gameObject.transform.position = new Vector3(transform.position.x + vx * 1f, transform.position.y, 0);
    }
}
