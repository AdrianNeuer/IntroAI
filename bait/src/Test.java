import java.lang.annotation.Repeatable;
import java.util.Random;

import core.ArcadeMachine;
import core.competition.CompetitionParameters;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{

    public static void main(String[] args)
    {   
        int seed = new Random().nextInt(); // seed for random


        String AstarController = "controllers.Astar.Agent";
        String sampleMCTSController = "controllers.sampleMCTS.Agent";

        CompetitionParameters.ACTION_TIME = 100; // no time for finding the whole path
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl2.txt", true, sampleMCTSController, null, seed, false);


        //CompetitionParameters.ACTION_TIME = 1000; // no time for finding the whole path
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl0.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl1.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl2.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl3.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl4.txt", true, AstarController, null, seed, false);
        
        /*
        String limitdepthfirstController = "controllers.limitdepthfirst.Agent";

        CompetitionParameters.ACTION_TIME = 100; // set to the time that allow you to do the depth first search
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl0.txt", true, limitdepthfirstController, null, seed, false);
        */
        /*
        String depthfirstController = "controllers.depthfirst.Agent"; 

        CompetitionParameters.ACTION_TIME = 10000; // set to the time that allow you to do the depth first search
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl0.txt", true, depthfirstController, null, seed, false);
        */
        //ArcadeMachine.playOneGame( "examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl1.txt", null, new Random().nextInt());
    }
}
