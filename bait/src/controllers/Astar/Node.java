package controllers.Astar;

import java.util.ArrayList;
import java.util.Stack;

import core.game.StateObservation;
import ontology.Types;

public class Node implements Cloneable {
    public StateObservation state;
    public ArrayList<Types.ACTIONS> astaraction = new ArrayList<Types.ACTIONS>(); // 存储初始状态到该状态的路径
    public double priority;
    public int path;
    @Override
    public Node clone(){  //重写clone方法
        Node node = null;
        try{
            node = (Node)super.clone();
        }
        catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return node;
    }
}
