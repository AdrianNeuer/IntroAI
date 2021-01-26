package controllers.depthfirst;

import core.player.AbstractPlayer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;
    protected ArrayList<Observation> grid[][];
    protected int block_size;

    protected ArrayList<StateObservation> reached_state = new ArrayList<StateObservation>();
    protected Stack<Types.ACTIONS> idea_action = new Stack<Types.ACTIONS>();
    protected Stack<Types.ACTIONS> reversed_idea_action = new Stack<Types.ACTIONS>();
    // protected Stack<StateObservation> ready_to_reach = new
    // Stack<StateObservation>();

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
    }

    public Boolean check_positon(StateObservation now_state) {
        for (StateObservation pre_state : reached_state) {
            if (pre_state.equalPosition(now_state)) {
                return true;
            }
        }
        return false;
    }

    public Boolean get_target(StateObservation state, Types.ACTIONS action) {
        StateObservation state_check = state.copy();
        state_check.advance(action);
        if (state_check.isGameOver()) {
            if (state_check.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                return true;
            } else {
                return false;
            }
        } else {
            if (check_positon(state_check)) {
                return false;
            } 
            else {
                reached_state.add(state_check);
                ArrayList<Types.ACTIONS> actions = state_check.getAvailableActions();
                Boolean mark = false;
                for (Types.ACTIONS future_action : actions) {
                    idea_action.push(future_action);
                    mark = get_target(state_check, future_action);
                    if (!mark) {
                        idea_action.pop();
                    } else {
                        break;
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
        reached_state.add(stateObs.copy());
        StateObservation stCopy = stateObs.copy();
        ArrayList<Types.ACTIONS> actions = stCopy.getAvailableActions();
        Boolean success = false;
        for (Types.ACTIONS my_action : actions) {    //为了防止reached_state里面出现empty的状态
            StateObservation stready = stCopy.copy();
            idea_action.push(my_action);
            success = get_target(stready, my_action);
            if (success) {
                break;
            } else {
                idea_action.pop();
            }
        }
        while (!idea_action.empty()) {
            reversed_idea_action.push(idea_action.pop());
        }
        return reversed_idea_action.pop();
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
