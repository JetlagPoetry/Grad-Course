using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.UI;

public class Controller : MonoBehaviour {

    public GameObject enemy;
    public PlayerAgent player;
    public AIAgent ai;
    private PlayerAgent player_agent;
    private AIAgent ai_agent;
    public GameObject item;
    private Text text1, text2;
    [System.NonSerialized]
    public int item_count;

    private Vector3[] alcoves = { new Vector3(-10f, 0.3f, 7.2f), new Vector3(-5f, 0.3f, 7.2f), new Vector3(0f, 0.3f, 7.2f),
        new Vector3(5f, 0.3f, 7.2f), new Vector3(10f, 0.3f, 7.2f),new Vector3(-10f, 0.3f, -7.2f), new Vector3(-5f, 0.3f, -7.2f),
        new Vector3(0f, 0.3f, -7.2f),new Vector3(5f, 0.3f, -7.2f), new Vector3(10f, 0.3f, -7.2f) };

    // Use this for initialization
    void Start()
    {
        //Get text
        text1 = GameObject.FindGameObjectWithTag("Text1").GetComponent<Text>();
        text2 = GameObject.FindGameObjectWithTag("Text2").GetComponent<Text>();
        //Generate items
        item_count = alcoves.Length;
        for (int i = 0; i < alcoves.Length; i++)
        {
            Instantiate(item, alcoves[i], transform.rotation);
        }
        //Generate two enemies
        Instantiate(enemy, new Vector3(0f, 0f, 3.75f), transform.rotation);
        Instantiate(enemy, new Vector3(0f, 0f, -3.75f), transform.rotation);
        //Generate player
        int player_location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 10);
        player_agent = Instantiate(player, new Vector3(alcoves[player_location].x, alcoves[player_location].y,
            alcoves[player_location].z > 0 ? 8f : -8f), transform.rotation);
        //Generate AI
        int ai_location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 10);
        while (ai_location == player_location)
        {
            ai_location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 10);
        }
        ai_agent = Instantiate(ai, new Vector3(alcoves[ai_location].x, alcoves[ai_location].y,
            alcoves[ai_location].z > 0 ? 8f : -8f), transform.rotation);
    }

    // Update is called once per frame
    void Update()
    {
        text1.text = "Player score:" + player.score;
        text2.text = "AI score:" + ai.score;
        if (item_count <= 0 || (!player_agent.gameObject.activeSelf && !ai_agent.gameObject.activeSelf))
        {
            //Winning agent declared
            if (player.score > ai.score)
            {
                Application.LoadLevel("PlayerWins");
            }
            else if (player.score < ai.score)
            {
                Application.LoadLevel("AIWins");
            }
            else
            {
                Application.LoadLevel("Tie");
            }
        }
    }

}

