using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Goal : MonoBehaviour {  

    private void OnTriggerEnter(Collider other)
    {
        //after the player arrives at the house, show end game screen
        if (other.gameObject.tag == "Player")
        {
            Application.LoadLevel("EndGameScene");
        }
        
    }
}
