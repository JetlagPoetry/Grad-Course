using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MazeTrigger : MonoBehaviour {
    
    public GameObject mazeUnit;
    public GameObject endUnit;
    private bool nextUnitCreated = false;

    private void OnTriggerEnter(Collider other)
    {
        //Trigger with player, generate the next mazeunit
        if (other.gameObject.tag == "Player" && nextUnitCreated == false)
        {
            nextUnitCreated = true;
            Instantiate(mazeUnit, new Vector3(0, -5, transform.position.z + 2.5f), transform.rotation);
        }
        //Trigger with bullet, generate the last mazeunit
        if (other.gameObject.tag == "Bullet")
        {
            Instantiate(endUnit, new Vector3(0, -5, transform.position.z + 2.5f), transform.rotation);
        }

    }

}
