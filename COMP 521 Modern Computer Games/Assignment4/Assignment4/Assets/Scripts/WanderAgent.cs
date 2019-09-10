using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class WanderAgent : MonoBehaviour {
    private Vector3 desired_velocity, steering, velocity, target;
    private float max_velocity, max_steer_force = 0.01f, max_avoid_force = 0.03f;
    private float decide_next_target = 2f;
    private float circle_distance = 3f, circle_radius = 1.5f;
    private float wander_angle = 0f;
    private RaycastHit hitInfo;
    private Rigidbody rb;
    private GameObject[] travel_agent_list;

    // Use this for initialization
    void Start()
    {
        rb = gameObject.GetComponent<Rigidbody>();
        //Set a random movement speed, in the range of 0.05-0.1
        max_velocity = (float)new System.Random(System.Guid.NewGuid().GetHashCode()).Next(5, 10) / 100f;
        //Random its face and initial velocity
        int option = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
        switch (option)
        {
            case 1:
                transform.Rotate(new Vector3(0, 1, 0), -90);
                //velocity = new Vector3(-max_velocity/10, 0, 0);
                break;
            case 2:
                transform.Rotate(new Vector3(0, 1, 0), 90);
                //velocity = new Vector3(max_velocity/10, 0, 0);
                break;
            case 3:
                transform.Rotate(new Vector3(0, 1, 0), 180);
                //velocity = new Vector3(0, 0, -max_velocity/10);
                break;
            default:
                //velocity = new Vector3(0, 0, max_velocity/10);
                break;
        }
        //Random its next target position
        NextTarget();
        
    }
    
    void FixedUpdate()
    {
        if (decide_next_target < 0)
        {
            NextTarget();
            decide_next_target = 2f;
        }
        decide_next_target -= Time.deltaTime;

        Seek();
    }

    private void NextTarget()
    {
        float x = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 38) - 19;
        float z = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 18) - 9;
        target = new Vector3(x, 0.5f, z);
    }

    private void Seek()
    {
        Vector3 avoid_force = AvoidObstacle();

        if (avoid_force.magnitude > 0)
        {
            steering = avoid_force;
        }
        else
        {
            int index = NearTravelAgent();
            if (index >= 0)
            {
                //Set interposing target
                InterposingTravelAgent(index);
            }
            else
            {
                //Set wandering target
                Wander();
            }
            //Use the basis seeking performance
            desired_velocity = Vector3.Normalize(target - transform.position);
            desired_velocity *= max_velocity;
            steering = desired_velocity - velocity;
            if (steering.magnitude > max_steer_force)
            {
                steering.Normalize();
                steering *= max_steer_force;
            }
        }
        velocity = velocity + steering;
        if (velocity.magnitude > max_velocity)
        {
            velocity.Normalize();
            velocity *= max_velocity;
        }
        rb.MovePosition(transform.position + velocity);
        //Set the agent face its moving direction        
        transform.rotation = Quaternion.LookRotation(velocity);
    }

    //Craig Reynolds alg
    private void Wander()
    {
        Vector3 circle_position = new Vector3();
        circle_position = transform.position + velocity.normalized * circle_distance;
        Vector3 displacement = new Vector3(circle_radius, 0, 0);
        int angle = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 360);
        displacement.x = Mathf.Sin(angle);
        displacement.z = Mathf.Cos(angle);
        target = circle_position + displacement;
    }

    private int NearTravelAgent()
    {
        travel_agent_list = GameObject.FindGameObjectsWithTag("TravelAgent");
        for (int i = 0; i < travel_agent_list.Length; i++)
        {
            if (Vector3.Distance(travel_agent_list[i].transform.position, transform.position) < 2f)
            {
                return i;
            }
        }
        return -1;
    }

    private void InterposingTravelAgent(int index)
    {
        Vector3 travel_agent_target = travel_agent_list[index].GetComponent<TravelAgent>().target_exit - travel_agent_list[index].transform.position;
        travel_agent_target.Normalize();
        travel_agent_target *= 2;
        target = travel_agent_list[index].transform.position + travel_agent_target;
    }

    private Vector3 AvoidObstacle()
    {
        Vector3 avoidance = new Vector3(0, 0, 0);
        int detector = DetectObstacle();
        if (detector < 0)
            return avoidance;
        if (detector == 1)
        {
            if (transform.position.z > 0)
            {
                avoidance.x = hitInfo.normal.z;
                avoidance.z = -hitInfo.normal.x;
            }
            else
            {
                avoidance.x = -hitInfo.normal.z;
                avoidance.z = hitInfo.normal.x;
            }
        }
        if (detector == 2)
        {
            avoidance.x = -hitInfo.normal.z;
            avoidance.z = hitInfo.normal.x;
        }
        if (detector == 3)
        {
            avoidance.x = hitInfo.normal.z;
            avoidance.z = -hitInfo.normal.x;
        }
        if (!hitInfo.collider.gameObject.CompareTag("TravelAgent") && hitInfo.collider.gameObject.CompareTag("WanderAgent") && detector == 4)
        {
            avoidance = hitInfo.normal;
        }
        if (!hitInfo.collider.gameObject.CompareTag("TravelAgent") && hitInfo.collider.gameObject.CompareTag("WanderAgent") && detector == 5)
        {
            avoidance = hitInfo.normal;
        }
        avoidance.Normalize();
        if (transform.position.x < -19.5f)
        {
            velocity = new Vector3(0, 0, 0);
            avoidance = new Vector3(1, 0, 0);
        }
        avoidance *= max_avoid_force;
        return avoidance;
    }


    private int DetectObstacle()
    {
        Vector3 left_offset = new Vector3(-velocity.z, 0, velocity.x);
        left_offset.Normalize();
        left_offset *= 0.5f;
        Vector3 right_offset = new Vector3(velocity.z, 0, -velocity.x);
        right_offset.Normalize();
        right_offset *= 0.5f;
        RaycastHit info1, info2, info3, info4, info5;
        float detect_distance = 1.2f;
        float d1 = -1, d2 = -1, d3 = -1, d4 = -1, d5 = -1, min_distance = float.PositiveInfinity;
        if (Physics.Raycast(transform.position, velocity, out info1, detect_distance))
        {
            d1 = Vector3.Distance(transform.position, info1.transform.position);
            min_distance = d1 < min_distance ? d1 : min_distance;
            hitInfo = d1 < min_distance ? info1 : hitInfo;
        }
        if (Physics.Raycast(transform.position + left_offset, velocity, out info2, detect_distance))
        {
            d2 = Vector3.Distance(transform.position, info2.transform.position);
            min_distance = d2 < min_distance ? d2 : min_distance;
            hitInfo = d2 < min_distance ? info2 : hitInfo;
        }
        if (Physics.Raycast(transform.position + right_offset, velocity, out info3, detect_distance))
        {
            d3 = Vector3.Distance(transform.position, info3.transform.position);
            min_distance = d3 < min_distance ? d3 : min_distance;
            hitInfo = d3 < min_distance ? info3 : hitInfo;
        }
        if (d1 < 0 && d2 < 0 && d3 < 0 && Physics.Raycast(transform.position, left_offset, out info4, detect_distance - 0.5f))
        {
            d4 = Vector3.Distance(transform.position, info4.transform.position);
            min_distance = d4 < min_distance ? d4 : min_distance;
            hitInfo = d4 < min_distance ? info4 : hitInfo;
        }
        if (d1 < 0 && d2 < 0 && d3 < 0 && Physics.Raycast(transform.position, right_offset, out info5, detect_distance - 0.5f))
        {
            d5 = Vector3.Distance(transform.position, info5.transform.position);
            min_distance = d5 < min_distance ? d5 : min_distance;
            hitInfo = d5 < min_distance ? info5 : hitInfo;
        }
        if (d1 == min_distance)
        {
            Physics.Raycast(transform.position, velocity, out hitInfo, detect_distance);
            return 1;
        }
        else if (d2 == min_distance)
        {
            Physics.Raycast(transform.position + left_offset, velocity, out hitInfo, detect_distance);
            return 2;
        }
        else if (d3 == min_distance)
        {
            Physics.Raycast(transform.position + right_offset, velocity, out hitInfo, detect_distance);
            return 3;
        }
        else if (d4 == min_distance)
        {
            Physics.Raycast(transform.position, left_offset, out hitInfo, detect_distance - 0.5f);
            return 4;
        }
        else if (d5 == min_distance)
        {
            Physics.Raycast(transform.position, right_offset, out hitInfo, detect_distance - 0.5f);
            return 5;
        }
        return -1;

    }
}
