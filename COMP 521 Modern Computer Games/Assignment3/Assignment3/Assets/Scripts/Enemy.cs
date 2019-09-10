using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Enemy : MonoBehaviour
{
    public GameObject enemy;
    GameObject aiAgent, playerAgent;
    private const float speed = 0.02f;
    private float timer = 0f;

    // Use this for initialization
    void Start()
    {
        aiAgent = GameObject.FindGameObjectWithTag("AIAgent");
        playerAgent = GameObject.FindGameObjectWithTag("PlayerAgent");

        //random starting doorway
        if (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 2) == 0)
        {
            //starting right
            transform.position = new Vector3(12.5f, transform.position.y, transform.position.z);
            transform.Rotate(new Vector3(0, 1, 0), -90);
        }
        else
        {
            //starting left
            transform.position = new Vector3(-12.5f, transform.position.y, transform.position.z);
            transform.Rotate(new Vector3(0, 1, 0), 90);
        }
    }

    // Update is called once per frame
    void Update()
    {
        //detect doorway
        CollideWithDoorway();

        //moving horizontal
        transform.Translate(Vector3.forward * speed);
        
    }

    //Collsion with side obstacles, called by EnemyCollider
    private void CollideWithSideObstacle()
    {
        timer -= Time.deltaTime;
        if (timer <= 0)
        {
            int option = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 3);
            if (option == 0)
            {
                //unhindered
                //do nothing
            }
            else if (option == 1)
            {
                //disappear and respawn
                Instantiate(enemy, new Vector3(0f, 0f, transform.position.z), new Quaternion(0, 0, 0, 0));
                Destroy(gameObject);
            }
            else
            {
                //reverse direction
                transform.Rotate(new Vector3(0, 1, 0), 180);
            }
            timer = 2f;
        }
    }

    //Despawn and Respawn if reach doorway
    private void CollideWithDoorway()
    {
        if (transform.position.x < -12.5f || transform.position.x > 12.5f)
        {
            Instantiate(enemy, new Vector3(0f, 0f, transform.position.z), new Quaternion(0,0,0,0));
            Destroy(gameObject);
        }
    }

    //Collide with AI agent
    public void CollideWithAI()
    {
        //AI lose
        aiAgent.SetActive(false);
        
    }

    //Collide with Player
    public void CollideWithPlayer()
    {
        //Player lose
        playerAgent.SetActive(false);
    }

    private void OnTriggerEnter(Collider other)
    {
        if (other.tag == "SideObstacle")
        {
            CollideWithSideObstacle();
        }
    }

    //Expose to AIAgent, return -1 when facing left, 1 when facing right
    public int GetDirecion()
    {
        return transform.rotation.y > 0 ? 1 : -1;
    }
}
