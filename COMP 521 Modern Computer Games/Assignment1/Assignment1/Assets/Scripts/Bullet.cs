using System.Collections;
using System.Collections.Generic;
using UnityStandardAssets.Characters.FirstPerson;
using UnityEngine;

public class Bullet : MonoBehaviour {

    Rigidbody rb;
    public float bulletSpeed;

	// Use this for initialization
	void Start () {
        rb = GetComponent<Rigidbody>();
        rb.AddRelativeForce(0, 0, bulletSpeed, ForceMode.Impulse);
	}
	
}
