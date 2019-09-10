using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SocialAgentHead : MonoBehaviour {

    private Material wander_material, social_material;
    private Renderer renderer;

    // Use this for initialization
    void Start () {
        wander_material = Resources.Load("WanderAgent") as Material;
        social_material = Resources.Load("SocialAgent") as Material;
        renderer = gameObject.GetComponent<Renderer>();
    }
	
	// Update is called once per frame
	void Update () {
		
	}

    public void ChangeToSocialMaterial()
    {
        renderer.material = social_material;
    }

    public void ChangeToWanderMaterial()
    {
        renderer.material = wander_material;
    }
}
