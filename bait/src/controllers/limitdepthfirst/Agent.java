package controllers.limitdepthfirst;

import java.awt.Graphics2D;
import java.lang.ProcessBuilder.Redirect.Type;
import java.util.ArrayList;
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

    protected ArrayList<StateObservation> reached_state = new ArrayList<StateObservation>();
    protected ArrayList<StateObservation> true_state = new ArrayList<StateObservation>();
    protected Stack<Types.ACTIONS> idea_action = new Stack<Types.ACTIONS>();
    protected Stack<Types.ACTIONS> reversed_idea_action = new Stack<Types.ACTIONS>();
    //protected Stack<StateObservation> ready_to_reach = new Stack<StateObservation>();

    protected int Max_depth = 6;
    protected Boolean get_key = false;
    protected int Search_Depth;
    protected Types.ACTIONS need_action;
    protected Types.ACTIONS now_action;
    protected double least_value;
    protected Boolean win_break = false;

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

    public Boolean check_positon(StateObservation now_state) {
        for (StateObservation pre_state : reached_state) {
            if (pre_state.equalPosition(now_state)) {
                return true;
            }
        }
        return false;
    }

    public double heuristic(StateObservation stateObs) {
        Vector2d now = stateObs.getAvatarPosition();
        if (get_key) {
            if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS){  //状态在执行完action后游戏胜利时，所处的位置与目标的位置不同，故需要做判断
                return 0;
            }
            else{
                return now.dist(goalpos);
            }
        } else {
            return now.dist(keypos);
        }
    }

    public Boolean get_target(StateObservation state, Types.ACTIONS action) {
        StateObservation state_check = state.copy();
        state_check.advance(action);
        if (heuristic(state_check) < least_value){
            least_value = heuristic(state_check);
            if ( now_action != need_action){
                need_action = now_action;  //改变第一步的action
            }
        }
        if (state_check.isGameOver()) {
            if (state_check.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                win_break = true;
                return true;
            } 
            else {
                return false;
            }
        } 
        else {
            if (check_positon(state_check)) {
                return false;
            } 
            else {
                ArrayList<Types.ACTIONS> actions = state_check.getAvailableActions();
                Boolean mark = false;
                if (Search_Depth > Max_depth) {
                    return false;
                }
                else{
                    reached_state.add(state_check.copy());
                    for (Types.ACTIONS future_action : actions) {
                        idea_action.push(future_action);
                        Search_Depth += 1;
                        mark = get_target(state_check, future_action);
                        if (!mark) {
                            Search_Depth -= 1;
                            idea_action.pop();
                        } 
                        else {
                            break;
                        }
                    }
                }
                return mark;
            }
        }
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (!reversed_idea_action.empty()) {
            return reversed_idea_action.pop();
        }
        true_state.add(stateObs.copy());
        reached_state.add(stateObs.copy());
        StateObservation stCopy = stateObs.copy();
        ArrayList<Types.ACTIONS> actions = stCopy.getAvailableActions();
        need_action = actions.get(0);
        Boolean success = false;
        least_value = 10000;
        for (Types.ACTIONS my_action : actions) {
            now_action = my_action;
            Search_Depth = 0;
            StateObservation stready = stCopy.copy();
            idea_action.push(my_action);
            Search_Depth += 1;
            success = get_target(stready, my_action);
            if (success) {
                break;
            }
            else {
                idea_action.pop();
                Search_Depth -= 1;
            }
        }
        while (!idea_action.empty()) {
            reversed_idea_action.push(idea_action.pop());
        }
        if (win_break){
            return reversed_idea_action.pop();
        }
        else {  //重新初始化以进行下一次搜索
            idea_action = new Stack<Types.ACTIONS>();
            reversed_idea_action = new Stack<Types.ACTIONS>();
            reached_state = new ArrayList<StateObservation>();
            for (StateObservation state : true_state){
                reached_state.add(state);
            }
            stateObs.advance(need_action);
            if (stateObs.getAvatarPosition().equals(keypos)) {
                get_key = true;
            }
            //System.out.println(need_action);
            return need_action;
        }
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
