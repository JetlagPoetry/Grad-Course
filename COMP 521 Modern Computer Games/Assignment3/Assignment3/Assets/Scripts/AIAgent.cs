using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

public class AIAgent : MonoBehaviour {

    private PlayerAgent player;
    private float speed = 2f;
    private int ultimate_spell = 2;
    private NavMeshAgent navMeshAgent;
    private Vector3[] alcoves = { new Vector3(-10f, 0.3f, 7f), new Vector3(-5f, 0.3f, 7f), new Vector3(0f, 0.3f, 7f),
        new Vector3(5f, 0.3f, 7f), new Vector3(10f, 0.3f, 7f), new Vector3(-10f, 0.3f, -7f), new Vector3(-5f, 0.3f, -7f),
        new Vector3(0f, 0.3f, -7f),new Vector3(5f, 0.3f, -7f), new Vector3(10f, 0.3f, -7f), new Vector3(-12f, 0.3f, 0f),
        new Vector3(12f, 0.3f, 0f) };
    private Vector3 closest_enemy_position;
    public Enemy enemy;

    [System.NonSerialized]
    public int score = 0;

    // Use this for initialization
    void Start () {
        navMeshAgent = GetComponent<NavMeshAgent>();
        player = GameObject.FindGameObjectWithTag("PlayerAgent").GetComponent<PlayerAgent>();
        GoToClosestItem();
    }

    // Update is called once per frame
    void Update()
    {
        UltimateSpellToPlayer();
        int state = CloseToEnemy();
        if (state == 0)
        {
            GoToClosestItem();
        }
        else if (state == 1)
        {
            GoToClosestAlcove();
        }
        else if (state == 2)
        {
            navMeshAgent.SetDestination(transform.position);
        }
    }

    //Determine whether to use teleport trap to player
    private void UltimateSpellToPlayer()
    {
        //Has only 2 teleport traps
        if (ultimate_spell <= 0)
            return;
        //If there are less than 4 items, and Player is closer to most of it, then teleport player
        GameObject[] items = GameObject.FindGameObjectsWithTag("Item");
        if (items.Length <= 4)
        {
            int temp = 0;
            for(int i = 0; i < items.Length; i++)
            {
                if (Vector3.Distance(player.transform.position, items[i].GetComponent<Item>().transform.position) <
                    Vector3.Distance(transform.position, items[i].GetComponent<Item>().transform.position))
                    temp++;
            }
            if (temp >= items.Length / 2 + 1)
            {
                //teleport Player to a random alcove
                int player_location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 10);
                player.GetComponent<BoxCollider>().enabled = false;
                player.transform.position = new Vector3(alcoves[player_location].x, alcoves[player_location].y, alcoves[player_location].z > 0 ? 8f : -8f);
                player.GetComponent<BoxCollider>().enabled = true;
                ultimate_spell--;

                Debug.Log("1");
            }
        }
    }

    //Set the closest item to be the dest
    private void GoToClosestItem()
    {
        GameObject[] items = GameObject.FindGameObjectsWithTag("Item");
        if (items.Length==0) return;
        int closest_item = 0;
        float item_distance = Vector3.Distance(transform.position, items[0].GetComponent<Item>().transform.position);
        //Find closest item
        for (int i = 0; i < items.Length; i++)
        {
            if (items[i] == null) continue;
            float distance = Vector3.Distance(transform.position, items[i].GetComponent<Item>().transform.position);
            if (distance <= item_distance)
            {
                //If player is closer to this item, then continue
                if (Vector3.Distance(player.transform.position, items[i].GetComponent<Item>().transform.position) < distance)
                    continue;
                item_distance = distance;
                closest_item = i;
            }
        }
        navMeshAgent.SetDestination(items[closest_item].transform.position);
    }

    //Set the closest alcove to be the dest
    private void GoToClosestAlcove()
    {
        int closest_alcove = 0;
        float alcove_distance = Vector3.Distance(transform.position, alcoves[0]);
        for (int i = 0; i < alcoves.Length; i++)
        {
            float distance = Vector3.Distance(transform.position, alcoves[i]);
            if (distance <= alcove_distance)
            {
                //If this is blocked by enemy, then continue
                if (distance > 1 && (closest_enemy_position.x > transform.position.x && closest_enemy_position.x - 3.8f < alcoves[i].x ||
                    closest_enemy_position.x < transform.position.x && closest_enemy_position.x + 3.8f > alcoves[i].x))
                    continue;
                alcove_distance = distance;
                closest_alcove = i;
            }
        }
        navMeshAgent.SetDestination(alcoves[closest_alcove]);
    }
    
    //Detect if close to enemy, return 0 when not
    //return 1 when enemy is facing toward ai, 2 when enemy is back to ai
    //return -1 when has dealt with behaviour
    private int CloseToEnemy()
    {
        GameObject[] enemies = GameObject.FindGameObjectsWithTag("Enemy");
        GameObject[] items = GameObject.FindGameObjectsWithTag("Item");
        for(int i = 0; i < enemies.Length; i++)
        {
            Vector3 enemy = enemies[i].transform.position;
            int direction = enemies[i].GetComponent<Enemy>().GetDirecion();
            if (transform.position.z * enemy.z > 0)
            {
                if (direction == 1)
                {
                    //Enemy facing right
                    if (transform.position.x > enemy.x && transform.position.x - enemy.x < 4.5f)
                    {
                        //If enemy far from doorway
                        if (enemy.x > 6f)
                        {
                            int left = 0, right = 0;
                            for (int j = 0; j < items.Length; j++)
                            {
                                if (items[j].transform.position.z * transform.position.z > 0)
                                {
                                    if (items[j].transform.position.x < transform.position.x)
                                        left++;
                                    else
                                        right++;
                                }
                            }
                            if (left < right) 
                            {
                                //Else go the other way
                                navMeshAgent.SetDestination(new Vector3(10f, transform.position.y, transform.position.z));
                                return -1;
                            }
                        }
                        closest_enemy_position = enemy;
                        return 1;
                    }
                    if (transform.position.x < enemy.x && enemy.x - transform.position.x < 1.1f)
                    {
                        //If enemy far from doorway
                        if (enemy.x < -6f)
                        {
                            int left = 0, right = 0;
                            for (int j = 0; j < items.Length; j++)
                            {
                                if (items[j].transform.position.z * transform.position.z > 0)
                                {
                                    if (items[j].transform.position.x < transform.position.x)
                                        left++;
                                    else
                                        right++;
                                }
                            }
                            if (left < right && ultimate_spell > 0)
                            {
                                //If more items on its way, teleport the enemy
                                Instantiate(this.enemy, new Vector3(0f, 0f, enemies[i].transform.position.z), new Quaternion(0, 0, 0, 0));
                                Destroy(enemies[i]);
                                ultimate_spell--;
                            }
                            else
                            {
                                //Else go the other way
                                navMeshAgent.SetDestination(new Vector3(-10f, transform.position.y, transform.position.z));
                            }
                            return -1;
                        }
                        return 2;
                    }
                }
                else
                {
                    //Enemy facing left
                    if (transform.position.x < enemy.x && enemy.x - transform.position.x < 4.5f)
                    {
                        //If enemy far from doorway
                        if (enemy.x < -6f)
                        {
                            int left = 0, right = 0;
                            for (int j = 0; j < items.Length; j++)
                            {
                                if (items[j].transform.position.z * transform.position.z > 0)
                                {
                                    if (items[j].transform.position.x < transform.position.x)
                                        left++;
                                    else
                                        right++;
                                }
                            }
                            if (left > right)
                            {
                                //Else go the other way
                                navMeshAgent.SetDestination(new Vector3(-10f, transform.position.y, transform.position.z));
                                return -1;
                            }
                        }
                        //Enemy not far away from doorway, then return 1 and hide
                        closest_enemy_position = enemy;
                        return 1;
                    }
                    if (transform.position.x > enemy.x && transform.position.x - enemy.x < 1.1f)
                    {
                        //If enemy far from doorway
                        if (enemy.x > 6f)
                        {
                            int left = 0, right = 0;
                            for (int j = 0; j < items.Length; j++)
                            {
                                if (items[j].transform.position.z * transform.position.z > 0)
                                {
                                    if (items[j].transform.position.x < transform.position.x)
                                        left++;
                                    else
                                        right++;
                                }
                            }
                            if (left > right && ultimate_spell > 0)
                            {
                                //If more items on its way, teleport the enemy
                                Instantiate(this.enemy, new Vector3(0f, 0f, enemies[i].transform.position.z), new Quaternion(0, 0, 0, 0));
                                Destroy(enemies[i]);
                                ultimate_spell--;
                            }
                            else
                            {
                                //Else go the other way
                                navMeshAgent.SetDestination(new Vector3(10f, transform.position.y, transform.position.z));
                            }
                            return -1;
                        }
                        //Enemy not far away from doorway, then return 2 and wait
                        return 2;
                    }
                }
            }
        }
        return 0;
    }

    IEnumerator Wait(float waitTime)
    {
        yield return new WaitForSeconds(waitTime);
    }  
}
