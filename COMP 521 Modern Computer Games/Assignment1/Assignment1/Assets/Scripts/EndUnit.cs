using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class EndUnit : MonoBehaviour {
    
    void Start () {

        //Delete mazetriggers of the last unit
        for (int i = 0; i < MazeUnit.mazeTriggerList.Count; i++)
        {
            Destroy(MazeUnit.mazeTriggerList[i]);
        }
        MazeUnit.mazeTriggerList.Clear();

        //Enable forwardwall in the last unit to be hitten
        for (int i = 0; i < MazeUnit.forwardMazeWallList.Count; i++)
        {
            MazeUnit.forwardMazeWallList[i].AddComponent<MazeWall>();
        }
    }

}
