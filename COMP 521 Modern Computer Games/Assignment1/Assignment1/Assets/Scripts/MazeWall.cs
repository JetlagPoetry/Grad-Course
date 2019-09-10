using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MazeWall : MonoBehaviour {

    int health;

    private void Start()
    {
        //Each wall has 3 health
        health = 3;
    }

    private void Update()
    {
        //If this wall has no health, set non-active
        if(health <= 0)
        {
            gameObject.SetActive(false);
        }
    }

    private void OnCollisionEnter(Collision other)
    {
        //Decrease health whenever got hit by bullet
        if (other.gameObject.tag == "Bullet")
        {
            health--;
            Destroy(other.gameObject);
        }

    }
}
