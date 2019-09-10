using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Cannon : MonoBehaviour {

    public GameObject cannonball;
    public float angle = 40f;

    // Use this for initialization
    void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
        //If UpArrow pressed, rotate the cannon clockwise
        if (Input.GetKey(KeyCode.UpArrow))
        {
            if (angle >= 90f) return;
            gameObject.transform.Rotate(0, 0, -1f);
            angle += 1f;
        }

        //If DownArrow pressed, rotate the cannon counter-clockwise
        if (Input.GetKey(KeyCode.DownArrow))
        {
            if (angle <= 0f) return;
            gameObject.transform.Rotate(0, 0, 1f);
            angle -= 1f;
        }

        //If Spacebar pressed, fire cannonballs
        if (Input.GetKeyDown(KeyCode.Space))
        {
            Shoot();
        }
    }

    //Fire cannonballs
    public void Shoot()
    {
        //Create a cannonball
        Instantiate(cannonball, transform.position, transform.rotation);
    }
}
