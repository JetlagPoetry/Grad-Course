using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Item : MonoBehaviour {

    public PlayerAgent player;
    public AIAgent ai;
    private Controller controller;

    // Use this for initialization
    void Start () {
        controller = GameObject.FindGameObjectWithTag("Controller").GetComponent<Controller>();

    }
	
	// Update is called once per frame
	void Update () {
		
	}

    private void OnTriggerEnter(Collider other)
    {
        //Collected by agents
        if (other.tag == "AIAgent")
        {
            ai.score++;
            controller.item_count--;
            Destroy(gameObject);
        }
        if(other.tag == "PlayerAgent")
        {
            player.score++;
            controller.item_count--;
            Destroy(gameObject);
        }
    }
}
