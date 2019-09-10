using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Controller : MonoBehaviour
{

    public int obstacle_count;
    public int travel_agent_count;
    public int wander_agent_count;
    public int social_agent_count;
    public GameObject prefab_obstacle;
    public GameObject prefab_travel_agent;
    public GameObject prefab_wander_agent;
    public GameObject prefab_social_agent;

    private int travel_agent_to_init, wander_agent_to_init, social_agent_to_init;
    private float travel_agent_init_timer = 1f;
    private List<Vector3> obstacleLocation = new List<Vector3>(){ new Vector3(-12, 0.5f, 5), new Vector3(-4, 0.5f, 5),
        new Vector3(4, 0.5f, 5), new Vector3(12, 0.5f, 5),new Vector3(-12, 0.5f, -5), new Vector3(-4, 0.5f, -5),
        new Vector3(4, 0.5f, -5),new Vector3(12, 0.5f, -5), new Vector3(-8, 0.5f, 0), new Vector3(8, 0.5f, 0)};

    // Use this for initialization
    void Start()
    {
        travel_agent_to_init = travel_agent_count;
        wander_agent_to_init = wander_agent_count;
        social_agent_to_init = social_agent_count;

        for (int i = 0; i < obstacle_count; i++)
        {
            generateObstacle();
        }


    }

    // Update is called once per frame
    void Update()
    {
        //Generate certain number of travel agents with time span
        if (travel_agent_to_init > 0 && travel_agent_init_timer < 0)
        {
            generateTravelAgents();
            travel_agent_to_init--;
            travel_agent_init_timer = 1f;
        }
        travel_agent_init_timer -= Time.deltaTime;

        if (wander_agent_to_init > 0)
        {
            wander_agent_to_init--;
            Instantiate(prefab_wander_agent, new Vector3(0, 0.5f, 0), transform.rotation);
        }

        if (social_agent_to_init > 0)
        {
            social_agent_to_init--;
            Instantiate(prefab_social_agent, new Vector3(0, 0.5f, 0), transform.rotation);
        }
    }

    //Generate obstacles
    private void generateObstacle()
    {
        int polygonGroup = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4) + 1;
        int location = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, obstacleLocation.Count);
        if (polygonGroup == 1)
        {
            //Single cuboid
            GameObject obstacle = Instantiate(prefab_obstacle, obstacleLocation[location], transform.rotation);
            float scale_z = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(200, 400) / 100.0f - 1;
            float scale_x = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(500, 800) / 100.0f - 1;
            int rotation = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(-45, 45);
            obstacle.transform.Rotate(new Vector3(0, 1, 0), rotation);
            obstacle.transform.localScale += new Vector3(scale_x, 0, scale_z);
            obstacleLocation.RemoveAt(location);
        }
        else if (polygonGroup == 2)
        {
            //Two cuboids
            GameObject obstacle = Instantiate(prefab_obstacle, obstacleLocation[location], transform.rotation);
            float scale_z = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(200, 400) / 100.0f - 1;
            float scale_x = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(250, 400) / 100.0f - 1;
            obstacle.transform.localScale += new Vector3(scale_x, 0, scale_z);

            float scale_z_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(200, 400) / 100.0f - 1;
            float scale_x_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(250, 400) / 100.0f - 1;
            int direction = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            int direction_x = 0, direction_z = 0;
            switch (direction)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_2 + 2) / 2,
                obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_2 + 2) / 2),
                transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_2, 0, scale_z_2);

            obstacleLocation.RemoveAt(location);
        }
        else if (polygonGroup == 3)
        {
            //Three cuboids
            GameObject obstacle = Instantiate(prefab_obstacle, obstacleLocation[location], transform.rotation);
            float scale_z = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(150, 300) / 100.0f - 1;
            obstacle.transform.localScale += new Vector3(scale_x, 0, scale_z);

            float scale_z_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(150, 300) / 100.0f - 1;
            int direction_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            int direction_x = 0, direction_z = 0;
            switch (direction_2)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_2 + 2) / 2,
                obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_2 + 2) / 2),
                transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_2, 0, scale_z_2);

            float scale_z_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(150, 300) / 100.0f - 1;
            int direction_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            while (direction_3 == direction_2)
            {
                direction_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            }
            direction_x = 0;
            direction_z = 0;
            switch (direction_3)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_3 + 2) / 2,
               obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_3 + 2) / 2),
               transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_3, 0, scale_z_3);

            obstacleLocation.RemoveAt(location);
        }
        else if (polygonGroup == 4)
        {
            //Four cuboids
            GameObject obstacle = Instantiate(prefab_obstacle, obstacleLocation[location], transform.rotation);
            float scale_z = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            obstacle.transform.localScale += new Vector3(scale_x, 0, scale_z);

            float scale_z_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            int direction_2 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            int direction_x = 0, direction_z = 0;
            switch (direction_2)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_2 + 2) / 2,
                obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_2 + 2) / 2),
                transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_2, 0, scale_z_2);

            float scale_z_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            int direction_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            while (direction_3 == direction_2)
            {
                direction_3 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            }
            direction_x = 0;
            direction_z = 0;
            switch (direction_3)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_3 + 2) / 2,
               obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_3 + 2) / 2),
               transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_3, 0, scale_z_3);

            float scale_z_4 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            float scale_x_4 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(100, 300) / 100.0f - 1;
            int direction_4 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            while (direction_4 == direction_2 || direction_4 == direction_3)
            {
                direction_4 = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
            }
            direction_x = 0;
            direction_z = 0;
            switch (direction_4)
            {
                case 0:
                    direction_x = 1;
                    break;
                case 1:
                    direction_x = -1;
                    break;
                case 2:
                    direction_z = 1;
                    break;
                case 3:
                    direction_z = -1;
                    break;
            }
            obstacle = Instantiate(prefab_obstacle, new Vector3(obstacleLocation[location].x + direction_x * (scale_x + scale_x_4 + 2) / 2,
               obstacleLocation[location].y, obstacleLocation[location].z + direction_z * (scale_z + scale_z_4 + 2) / 2),
               transform.rotation);
            obstacle.transform.localScale += new Vector3(scale_x_4, 0, scale_z_4);

            obstacleLocation.RemoveAt(location);
        }
    }

    //Generate travel agents
    private void generateTravelAgents()
    {
        Instantiate(prefab_travel_agent, new Vector3(19f, 0.5f, 0), transform.rotation);
    }
   
}
