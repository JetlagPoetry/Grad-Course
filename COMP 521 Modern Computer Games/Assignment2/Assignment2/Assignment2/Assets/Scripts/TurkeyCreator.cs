using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TurkeyCreator : MonoBehaviour {
    
    public Turkey turkey;

    // Use this for initialization
    void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

    public void CreateNewTurkey()
    {
        Instantiate(turkey, transform.position, transform.rotation);
    }

}
