using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Wind : MonoBehaviour {

    public float altitude = 5f;
    public float wind_force = 0f;
    float force_range = 0.00025f;
    float timer = 0.35f;

    // Update is called once per frame
    void Update() {

        //Call ChangeWindForce() every 0.5s
        timer -= Time.deltaTime;
        if (timer <= 0)
        {
            ChangeWindForce();
            timer = 0.35f;
        }
           
    }

    void ChangeWindForce()
    {
        //random a wind_force in (-force_range, force_range)
        wind_force = force_range * (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 200) - 100) / 100f;
    }
}