using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TravelAgent : MonoBehaviour
{
    public GameObject prefab_travel_agent;

    [System.NonSerialized]
    public Vector3 target_exit;
    private Vector3 desired_velocity, steering, velocity, entrance;
    private float max_velocity, max_steer_force = 0.01f, max_avoid_force = 0.03f;
    private float decide_exit_timer = 10f;
    private RaycastHit hitInfo;
    private Rigidbody rb;
    private List<Vector3> obstacleLocation = new List<Vector3>(){ new Vector3(-12, 0.5f, 5), new Vector3(-4, 0.5f, 5),
        new Vector3(4, 0.5f, 5), new Vector3(12, 0.5f, 5),new Vector3(-12, 0.5f, -5), new Vector3(-4, 0.5f, -5),
        new Vector3(4, 0.5f, -5),new Vector3(12, 0.5f, -5), new Vector3(-8, 0.5f, 0), new Vector3(8, 0.5f, 0)};

    // Use this for initialization
    void Start()
    {
        rb = GetComponent<Rigidbody>();
        //Set face to left
        transform.Rotate(new Vector3(0, 1, 0), -90);
        //Random its choice of exit
        DecideExit();
        //Set a random movement speed, in the range of 0.1-0.2
        max_velocity = (float)new System.Random(System.Guid.NewGuid().GetHashCode()).Next(5, 10) / 100f;
        velocity = new Vector3(-max_velocity, 0, 0);
        entrance = transform.position;
    }

    // Update is called once per frame
    void FixedUpdate()
    {
        //If hasn't reach the exit for a long time, reconsider its target
        if (decide_exit_timer < 0)
        {
            DecideExit();
            decide_exit_timer = 10f;
        }
        decide_exit_timer -= Time.deltaTime;

        Seek();
    }

    private void DecideExit()
    {
        float z = (new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 2) == 0) ? 5 : -5;
        target_exit = new Vector3(-20, 0.5f, z);
    }

    private void Seek()
    {
        Vector3 avoid_force = AvoidObstacle();

        if (avoid_force.magnitude > 0 && Vector3.Distance(entrance,transform.position) > 1f && 
            Vector3.Distance(hitInfo.transform.position, target_exit) > 0.5f)
        {
            steering = avoid_force;
        }
        else
        {
            desired_velocity = Vector3.Normalize(target_exit - transform.position);
            desired_velocity *= max_velocity;
            steering = desired_velocity - velocity + AvoidObstacle();
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
        if (!hitInfo.collider.gameObject.CompareTag("TravelAgent") && !hitInfo.collider.gameObject.CompareTag("WanderAgent") 
            &&!hitInfo.collider.gameObject.CompareTag("SocialAgent") && detector == 4)
        {
            avoidance.x = -hitInfo.normal.z;
            avoidance.z = hitInfo.normal.x;
        }
        if (!hitInfo.collider.gameObject.CompareTag("TravelAgent") && !hitInfo.collider.gameObject.CompareTag("WanderAgent")
            && !hitInfo.collider.gameObject.CompareTag("SocialAgent") && detector == 5)
        {
            avoidance.x = hitInfo.normal.z;
            avoidance.z = -hitInfo.normal.x;
        }
        avoidance.Normalize();
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
        if (d1 < 0 && d2 < 0 && d3 < 0 && Physics.Raycast(transform.position, left_offset, out info4, detect_distance-0.5f))
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

    private void OnCollisionEnter(Collision collision)
    {
        if (collision.transform.tag == "Exit")
        {
            Instantiate(prefab_travel_agent, new Vector3(19.5f, 0.5f, 0), transform.rotation);
            Destroy(gameObject);
        }
    }
}
