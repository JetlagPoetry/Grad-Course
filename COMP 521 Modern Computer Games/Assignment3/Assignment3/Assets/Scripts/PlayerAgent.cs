using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

public class PlayerAgent : MonoBehaviour {

    [System.NonSerialized]
    public int score = 0;
    private int ultimate_spell = 2;
    private Vector3[] alcoves = { new Vector3(-10f, 0.3f, 7f), new Vector3(-5f, 0.3f, 7f), new Vector3(0f, 0.3f, 7f),
        new Vector3(5f, 0.3f, 7f), new Vector3(10f, 0.3f, 7f),new Vector3(-10f, 0.3f, -7f), new Vector3(-5f, 0.3f, -7f),
        new Vector3(0f, 0.3f, -7f),new Vector3(5f, 0.3f, -7f), new Vector3(10f, 0.3f, -7f) };

    private float speed = 3f;
    private Rigidbody rb;
    private AIAgent ai;
    public Enemy enemy;

    // Use this for initialization
    void Start () {
        rb = gameObject.GetComponent<Rigidbody>();
        ai = GameObject.FindGameObjectWithTag("AIAgent").GetComponent<AIAgent>();
    }
	
	// Update is called once per frame
	void Update () {
        //Player Movement control
        if (Input.GetKey(KeyCode.W))
        {
            rb.transform.Translate(Vector3.forward * Time.deltaTime * speed);
        }

        if (Input.GetKey(KeyCode.S))
        {
            rb.transform.Translate(Vector3.back * Time.deltaTime * speed);
        }

        if (Input.GetKey(KeyCode.A))
        {
            rb.transform.Translate(Vector3.left * Time.deltaTime * speed);
        }

        if (Input.GetKey(KeyCode.D))
        {
            rb.transform.Translate(Vector3.right * Time.deltaTime * speed);
        }

        if (Input.GetKeyDown(KeyCode.Space) && ultimate_spell > 0)
        {
            //Find closest enemy
            GameObject[] enemies = GameObject.FindGameObjectsWithTag("Enemy");
            float enemy_distance = Vector3.Distance(transform.position, enemies[0].GetComponent<Enemy>().transform.position);
            float ai_distance = Vector3.Distance(transform.position, ai.transform.position);
            int closest_enemy = 0;
            for (int i = 0; i < enemies.Length; i++)
            {
                float distance = Vector3.Distance(transform.position, enemies[i].GetComponent<Enemy>().transform.position);
                if (distance <= enemy_distance)
                {
                    enemy_distance = distance;
                    closest_enemy = i;
                }
            }
            if (ai_distance > enemy_distance)
            {
                //despawn and respawn this enemy
                Instantiate(enemy, new Vector3(0f, 0f, enemies[closest_enemy].transform.position.z), new Quaternion(0, 0, 0, 0));
                Destroy(enemies[closest_enemy]);
            }
            else
            {
                //teleport AI to a random alcove
                int ai_location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 10);
                ai.GetComponent<CapsuleCollider>().enabled = false;
                ai.transform.position = new Vector3(alcoves[ai_location].x, alcoves[ai_location].y, alcoves[ai_location].z > 0 ? 8f : -8f);
                ai.GetComponent<CapsuleCollider>().enabled = true;
            }
            ultimate_spell--;
        }
    }
}
