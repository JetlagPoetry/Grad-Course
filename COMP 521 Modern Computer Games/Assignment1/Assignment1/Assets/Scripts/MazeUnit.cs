using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MazeUnit : MonoBehaviour
{

    private bool[] destroyForwardWall;
    private bool[] destroyInnerWall;
    private static int[] unitLabel = new int[8];
    private static int[] nextUnitLabel = new int[8];
    private static int label = 1;
    public float Mergerate;
    public GameObject innerMazeWall;
    public GameObject forwardMazeWall;
    public GameObject mazeTrigger;
    public GameObject projectiles;
    public static List<GameObject> mazeTriggerList = new List<GameObject>();
    public static List<GameObject> forwardMazeWallList = new List<GameObject>();
    private Dictionary<int, List<int>> labelSet;

    void Start()
    {
        //delete mazetriggers of the last unit
        for (int i = 0; i < mazeTriggerList.Count; i++)
        {
            Destroy(mazeTriggerList[i]);
        }
        mazeTriggerList.Clear();

        //enable forwardwall in the last unit to be hitten
        for (int i = 0; i < forwardMazeWallList.Count; i++)
        {
            forwardMazeWallList[i].AddComponent<MazeWall>();
        }

        //get unitLabel from former nextUnitLabel
        unitLabel = nextUnitLabel;

        //renew the param objects
        nextUnitLabel = new int[8];
        destroyForwardWall = new bool[8];
        destroyInnerWall = new bool[7];
        mazeTriggerList = new List<GameObject>();
        forwardMazeWallList = new List<GameObject>();
        labelSet = new Dictionary<int, List<int>>();

        //Assign label to undefined grids
        for (int i = 0; i < unitLabel.Length; i++)
        {
            if (unitLabel[i] == 0)
            {
                unitLabel[i] = label++;
            }
        }

        //Randomly merge adjacent cells from different sets of this row
        for (int i = 0; i < destroyInnerWall.Length; i++)
        {
            if (unitLabel[i] != unitLabel[i + 1] && random(100) <= Mergerate * 100)
            {
                destroyInnerWall[i] = true;
                int temp = unitLabel[i + 1];
                for(int j = 0; j < unitLabel.Length; j++)
                {
                    if (unitLabel[j] == temp)
                    {
                        unitLabel[j] = unitLabel[i];
                    }
                }
            }
        }

        for (int i = 0; i < destroyInnerWall.Length; i++)
        {
            if (!destroyInnerWall[i])
            {
                Instantiate(innerMazeWall, new Vector3(transform.position.x + 5 * i - 15, transform.position.y + 2, transform.position.z), transform.rotation);

            }
        }

        for (int i = 0; i < unitLabel.Length; i++)
        {
            List<int> list = new List<int>();
            //store label data in labelset
            if (labelSet.ContainsKey(unitLabel[i]))
            {
                list.AddRange(labelSet[unitLabel[i]]);
                labelSet.Remove(unitLabel[i]);
            }
            list.Add(i);
            labelSet.Add(unitLabel[i], list);

            //Randomly create vertical connections
            if (random(100) <= Mergerate * 100)
            {
                destroyForwardWall[i] = true;
                nextUnitLabel[i] = unitLabel[i];
            }
        }

        //Ensure every set has a path to the next row
        foreach (KeyValuePair<int, List<int>> keyValuePair in labelSet)
        {
            bool _hasAPath = false;
            for (int i = 0; i < keyValuePair.Value.Count; i++)
            {
                if (destroyForwardWall[keyValuePair.Value[i]])
                {
                    _hasAPath = true;
                    break;
                }
            }

            if (!_hasAPath)
            {
                int temp = keyValuePair.Value[random(keyValuePair.Value.Count)];
                destroyForwardWall[temp] = true;
                nextUnitLabel[temp] = unitLabel[temp];
            }
        }

        //Instantiate forwardwalls and mazetriggers
        for (int i = 0; i < destroyForwardWall.Length; i++)
        {
            if (!destroyForwardWall[i])
            {
                GameObject _gameObject = Instantiate(forwardMazeWall, new Vector3(transform.position.x + 5 * i - 17.5f, transform.position.y + 2, transform.position.z + 2.5f), transform.rotation);
                forwardMazeWallList.Add(_gameObject);
            }
            else
            {
                GameObject _gameObject = Instantiate(mazeTrigger, new Vector3(transform.position.x + 5 * i - 17.5f, transform.position.y + 2, transform.position.z + 2.5f), transform.rotation);
                mazeTriggerList.Add(_gameObject);
            }
        }

        //Instantiate all the projectiles
        for (int i = 0; i < unitLabel.Length; i++)
        {
            Instantiate(projectiles, new Vector3(transform.position.x + 5 * i - 17.5f, transform.position.y + 1.5f, transform.position.z), transform.rotation);
        }
    }

    //Random a int number from 0 to param max
    private int random(int max)
    {
        return new System.Random(System.Guid.NewGuid().GetHashCode()).Next(0, max);
    }

}
