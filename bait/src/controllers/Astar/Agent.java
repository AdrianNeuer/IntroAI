package controllers.Astar;

import java.awt.Graphics2D;
import java.lang.ProcessBuilder.Redirect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;
    protected ArrayList<Observation> grid[][];
    protected int block_size;
    protected Vector2d goalpos;
    protected Vector2d keypos;


    public static Comparator<Node> priorityComparator = new Comparator<Node>() { // 匿名重写compare
            @Override
            public int compare(Node node1, Node node2) {
                if (node1.priority < node2.priority) {
                    return -1;
                } else if (node1.priority > node2.priority) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

    protected ArrayList<StateObservation> reached_state = new ArrayList<StateObservation>();
    protected ArrayList<Types.ACTIONS> idea_action = new ArrayList<Types.ACTIONS>();
    protected PriorityQueue<Node> ready_to_reach = new PriorityQueue<Node>(1,priorityComparator);

    protected Boolean get_key = false;
    protected Boolean better_action = false;

    /**
     * Public constructor with state observation and time due.
     * 
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        goalpos = fixedPositions[1].get(0).position; // 目标的坐标
        keypos = movingPositions[0].get(0).position; // 钥匙的坐标
    }

    public Boolean check_positon_reached(Node now_state) {
        for (StateObservation pre_state : reached_state) {
            if (pre_state.equalPosition(now_state.state)) {
                return true;
            }
        }
        return false;
    }

    public Boolean check_positon_unreached(Node now_state) {
        for (Node pre_state : ready_to_reach) {
            if (pre_state.state.equalPosition(now_state.state)) {
                if (now_state.priority > pre_state.priority){
                    better_action =true;
                }
                return true;
            }
        }
        return false;
    }

    public double heuristic(Node stateObs) {
        Vector2d now = stateObs.state.getAvatarPosition();
        if (get_key) {
            if (stateObs.state.getGameWinner() == Types.WINNER.PLAYER_WINS) { // 状态在执行完action后游戏胜利时，所处的位置与目标的位置不同，故需要做判断
                return 0;
            } else {
                return now.dist(goalpos) + stateObs.path;
            }
        } else {
            return now.dist(keypos) + stateObs.path;
        }
    }

    public void get_target(Node main_node) { // 由于数组存在，复制结点较为复杂
        ready_to_reach.offer(main_node);
        while (!ready_to_reach.isEmpty()) {
            Node checknode = ready_to_reach.poll().clone();
            reached_state.add(checknode.state.copy());
            idea_action.clear(); // 每次清空获取新的路径序列
            for (Types.ACTIONS one_action : checknode.astaraction) {
                idea_action.add(one_action);
            }   
            ArrayList<Types.ACTIONS> actions = checknode.state.getAvailableActions();
            for(Types.ACTIONS action : actions){
                StateObservation stCopy = checknode.state.copy();
                stCopy.advance(action);
                idea_action.add(action);
                Node newnode = new Node();
                newnode.state = stCopy.copy();
                for (Types.ACTIONS all_action : idea_action){
                    newnode.astaraction.add(all_action);
                }
                newnode.path = checknode.path + 50;
                newnode.priority = heuristic(newnode);
                if (get_key==false && newnode.state.getAvatarPosition().equals(keypos)){ // 拿到钥匙后要转换目标，所以清空原数组
                    get_key =true;
                    ready_to_reach.clear();
                    Node fir_node = new Node();
                    fir_node.state = newnode.state.copy();
                    fir_node.path = newnode.path;
                    fir_node.priority = heuristic(newnode);
                    for (Types.ACTIONS one_action : newnode.astaraction){
                        fir_node.astaraction.add(one_action);
                    }
                    ready_to_reach.add(fir_node);
                    idea_action.remove(idea_action.size()-1);
                    break;      
                }
                if (newnode.state.isGameOver()) {
                    if (newnode.state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                        return;
                    } 
                    else {
                        idea_action.remove(idea_action.size()-1);
                        continue;
                    }
                } 
                else {
                    if (check_positon_reached(newnode)) {
                        idea_action.remove(idea_action.size()-1);
                    } 
                    else if (check_positon_unreached(newnode)) {
                        if (better_action){  //对同一状态有更好的路径，加入进去
                            Node third_node = new Node();
                            third_node.state = newnode.state.copy();
                            third_node.path = newnode.path;
                            third_node.priority = newnode.priority;
                            for (Types.ACTIONS second_action : newnode.astaraction){
                                third_node.astaraction.add(second_action);
                            }
                            ready_to_reach.add(third_node);
                        }
                        idea_action.remove(idea_action.size()-1);
                    }
                    else {
                        Node second_node = new Node();
                        second_node.state = newnode.state.copy();
                        second_node.path = newnode.path;
                        second_node.priority = newnode.priority;
                        for (Types.ACTIONS second_action : newnode.astaraction){
                            second_node.astaraction.add(second_action);
                        }
                        ready_to_reach.add(second_node);
                        idea_action.remove(idea_action.size()-1);
                    }
                }
            }
        }
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (!idea_action.isEmpty()){
            return idea_action.remove(0);
        }
        Node firstnode = new Node();
        firstnode.path = 0;
        firstnode.state = stateObs.copy();
        firstnode.priority = firstnode.state.getAvatarPosition().dist(keypos);
        get_target(firstnode);
        return idea_action.remove(0);
    }

    private void printDebug(ArrayList<Observation>[] positions, String str) {
        if (positions != null) {
            System.out.print(str + ":" + positions.length + "(");
            for (int i = 0; i < positions.length; i++) {
                System.out.print(positions[i].size() + ",");
            }
            System.out.print("); ");
        } else
            System.out.print(str + ": 0; ");
    }

    /**
     * Gets the player the control to draw something on the screen. It can be used
     * for debug purposes.
     * 
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g) {
        int half_block = (int) (block_size * 0.5);
        for (int j = 0; j < grid[0].length; ++j) {
            for (int i = 0; i < grid.length; ++i) {
                if (grid[i][j].size() > 0) {
                    Observation firstObs = grid[i][j].get(0); // grid[i][j].size()-1
                    // Three interesting options:
                    int print = firstObs.category; // firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i * block_size + half_block, j * block_size + half_block);
                }
            }
        }
    }
}
