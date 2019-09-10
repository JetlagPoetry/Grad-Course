using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class EnemyCollider : MonoBehaviour {

    Enemy enemy;

	// Use this for initialization
	void Start () {
        enemy = gameObject.GetComponentInParent<Enemy>();
	}
	
	// Update is called once per frame
	void Update () {
		
	}
    
    void OnTriggerEnter(Collider other)
    { 
        //Representing fov to detect agents
        if(other.tag == "AIAgent")
        {
            enemy.CollideWithAI();
        }
        if(other.tag == "PlayerAgent")
        {
            Debug.Log("Player die");
            enemy.CollideWithPlayer();
        }
    }
}
