using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityStandardAssets.Characters.FirstPerson;

public class Pistol : MonoBehaviour {

    public FirstPersonController firstPersonController;
    private void Update()
    {
        transform.position = new Vector3(firstPersonController.transform.position.x, firstPersonController.transform.position.y + 0.4f, firstPersonController.transform.position.z);
        transform.rotation = firstPersonController.transform.rotation;
    }
}
