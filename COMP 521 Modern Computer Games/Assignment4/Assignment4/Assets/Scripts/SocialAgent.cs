using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SocialAgent : MonoBehaviour {
    [System.NonSerialized]
    public int state = 0;//0-Wandering; 1-Ready to form group; 2-Form/enter group; 3-In group; 4-Cooldown;

    private Vector3 desired_velocity, steering, velocity, target;
    private float max_velocity, max_steer_force = 0.01f, max_avoid_force = 0.03f;
    private float decide_next_target = 2f;
    private float circle_distance = 3f, circle_radius = 1.5f;
    private float wander_angle = 0f;
    private float conversation_timer = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(50, 200) / 100f;
    private float cool_down_timer = 2f;
    private RaycastHit hitInfo;
    private Rigidbody rb;
    private GameObject[] social_agent_list;
    private GameObject nearest_social_agent;
    private Renderer renderer;
    private Material wander_material, social_material;
    private SocialAgentHead head;
    public SocialGroup socialGroup;
    private SocialAgent other;

    // Use this for initialization
    void Start()
    {
        rb = gameObject.GetComponent<Rigidbody>();
        renderer = gameObject.GetComponent<Renderer>();
        head = gameObject.GetComponentInChildren<SocialAgentHead>();
        wander_material = Resources.Load("WanderAgent") as Material;
        social_material = Resources.Load("SocialAgent") as Material;

        //Set a random movement speed, in the range of 0.05-0.1
        max_velocity = (float)new System.Random(System.Guid.NewGuid().GetHashCode()).Next(5, 10) / 100f;
        //Random its face and initial velocity
        int option = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 4);
        switch (option)
        {
            case 1:
                transform.Rotate(new Vector3(0, 1, 0), -90);
                break;
            case 2:
                transform.Rotate(new Vector3(0, 1, 0), 90);
                break;
            case 3:
                transform.Rotate(new Vector3(0, 1, 0), 180);
                break;
            default:
                break;
        }
        //Random its next target position
        NextTarget();

    }

    void FixedUpdate()
    {
        Debug.Log(state);
        //If wandering, update its state
        if (state == 0)
        {
            if (decide_next_target < 0)
            {
                NextTarget();
                decide_next_target = 2f;
            }
            decide_next_target -= Time.deltaTime;

            Seek();

            int index = NearSocialAgent();
            if (index >= 0)
            {
                //Other social agents around
                if(new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, 100) < 5)
                {
                    nearest_social_agent = social_agent_list[index];
                    state = 1;
                }
            }
        }
        else if (state == 1)
        {
            //Self is ready to form a group
            SocialAgent other = nearest_social_agent.GetComponent<SocialAgent>();
            //If far away from the other agent, then back to wandering
            if (Vector3.Distance(transform.position, other.transform.position) > 2f)
            {
                state = 0;
            }
            else
            {
                if (other.state == 1)
                {
                    //Other is ready to form a group, then create group
                    socialGroup.SetCenter(transform.position, other.transform.position);
                    target = (socialGroup.center+transform.position)/2;
                    socialGroup.agent_number++;
                    state = 2;
                }
                else if (other.state == 2 || other.state == 3)
                {
                    //Other is forming or already in group, then join
                    other.socialGroup.agent_number++;
                    socialGroup = other.socialGroup;
                    target = (socialGroup.center + transform.position) / 2;
                    state = 2;
                }
                this.other = other;
            }
        }else if (state == 2)
        {
            velocity = new Vector3(0, 0, 0);
            Seek();
            transform.rotation = Quaternion.LookRotation(other.transform.position - transform.position);
            //Apply steer force to from a group
            if (Vector3.Distance(transform.position,target)<1.2f)
            {
                //Complete forming process, transfer to state 3
                state = 3;
                conversation_timer = new System.Random(System.Guid.NewGuid().GetHashCode()).Next(50, 200) / 100f;
            }
        }
        else if(state == 3) 
        {
            //In conversation, stay still
            //Do nothing
            transform.rotation = Quaternion.LookRotation(other.transform.position - transform.position);
            velocity = new Vector3(0, 0, 0);
            //In a random period of time, leave group
            if (conversation_timer < 0) 
            {
                state = 4;
                renderer.material = wander_material;
                head.ChangeToWanderMaterial();
                cool_down_timer = 2f;
                socialGroup.agent_number--;
            }
            if(socialGroup.agent_number <= 1)
            {
                state = 4;
                renderer.material = wander_material;
                head.ChangeToWanderMaterial();
                cool_down_timer = 2f;
                socialGroup.agent_number = 0;
                socialGroup.SetCenter(0, 0, 0);
            }
            conversation_timer -= Time.deltaTime;
        }
        else
        {
            //Wandering
            if (decide_next_target < 0)
            {
                NextTarget();
                decide_next_target = 2f;
            }
            decide_next_target -= Time.deltaTime;
            Seek();

            //In cooldown
            if (cool_down_timer < 0)
            {
                state = 0;
                renderer.material = social_material;
                head.ChangeToSocialMaterial();
            }
            cool_down_timer -= Time.deltaTime;
        }
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
            Wander();
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

    private int NearSocialAgent()
    {
        social_agent_list = GameObject.FindGameObjectsWithTag("SocialAgent");
        for (int i = 0; i < social_agent_list.Length; i++)
        {
            if (Vector3.Distance(social_agent_list[i].transform.position, transform.position) < 2f)
            {
                if (social_agent_list[i].transform == gameObject.transform)
                    continue;
                return i;
            }
        }
        return -1;
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
        avoidance *= max_avoid_force;
        if (transform.position.x < -19.5f)
        {
            velocity = new Vector3(0, 0, 0);
            avoidance = new Vector3(1, 0, 0);
        }
        if (hitInfo.collider.gameObject.CompareTag("SocialAgent"))
        {
            return new Vector3(0, 0, 0);
        }
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
