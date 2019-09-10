using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityStandardAssets.Characters.FirstPerson;

public class BulletEmitter : MonoBehaviour {

    public GameObject bullet;
    public FirstPersonController firstPersonController;
    private float initialHeight;

	// Use this for initialization
	void Start () {
        initialHeight = firstPersonController.transform.position.y;
    }
	
	// Update is called once per frame
	void Update () {
        if (Input.GetButtonDown("Fire1") && firstPersonController.m_ProjectileCount > 0)
        {
            if (initialHeight + 1f > firstPersonController.transform.position.y)
            {
                firstPersonController.m_ProjectileCount--;
                Shoot();
            }
        }
	} 

    public void Shoot()
    {
        GameObject _gameObject = Instantiate(bullet, transform.position, transform.rotation);
        Destroy(_gameObject, 1f);
    }
}
