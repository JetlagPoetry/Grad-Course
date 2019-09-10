using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SocialGroup : MonoBehaviour {

    public Vector3 center = new Vector3(0, 0, 0);
    public int agent_number = 0;

	// Use this for initialization
	void Start () {
        agent_number = 0;
    }
	
	// Update is called once per frame
	void Update () {
		
	}

    public void SetCenter(float x,float y,float z)
    {
        center.Set(x, y, z);
    }

    public void SetCenter(Vector3 position)
    {
        center.Set(position.x, position.y, position.z);
    }

    public void SetCenter(Vector3 position1, Vector3 position2)
    {
        center = (position1 + position2) / 2f;
    }
}
