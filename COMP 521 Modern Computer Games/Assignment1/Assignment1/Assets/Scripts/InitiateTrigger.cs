using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class InitiateTrigger : MonoBehaviour {

    public GameObject mazeUnit;
    public GameObject mazeWall;
    private bool firstUnitCreated = false;

    private void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.tag == "Player" && firstUnitCreated == false)
        {
            firstUnitCreated = true;
            Instantiate(mazeUnit, new Vector3(0, -5, 20), transform.rotation);
            Instantiate(mazeWall, new Vector3(0, -3, 17.5f), transform.rotation);
        }
    }

}
