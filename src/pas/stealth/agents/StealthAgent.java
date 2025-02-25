package src.pas.stealth.agents;


// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


// JAVA PROJECT IMPORTS
import edu.bu.pas.stealth.agents.AStarAgent;                // the base class of your class
import edu.bu.pas.stealth.agents.AStarAgent.AgentPhase;     // INFILTRATE/EXFILTRATE enums for your state machine
import edu.bu.pas.stealth.agents.AStarAgent.ExtraParams;    // base class for creating your own params objects
import edu.bu.pas.stealth.graph.Vertex;                     // Vertex = coordinate
import edu.bu.pas.stealth.graph.Path;                       // see the documentation...a Path is a linked list



public class StealthAgent
    extends AStarAgent
{

    // Fields of this class
    // TODO: add your fields here! For instance, it might be a good idea to
    // know when you've killed the enemy townhall so you know when to escape!
    // TODO: implement the state machine for following a path once we calculate it
    //       this will for sure adding your own fields.
    private int enemyChebyshevSightLimit;
    private AgentPhase currentPhase;
    private Vertex startVertex;
    private Vertex townHallVertex;
    private Path currentPath;
    private boolean infilDone = false;
    private ArrayList<Vertex> enemyPos = new ArrayList<Vertex>();
    private ArrayList<Integer> enemyIDs = new ArrayList<Integer>();
    private int myUnitID;




    public StealthAgent(int playerNum)
    {
        super(playerNum);

        this.enemyChebyshevSightLimit = -1; // invalid value....we won't know this until initialStep()
    }

    // TODO: add some getter methods for your fields! Thats the java way to do things!
    public final int getEnemyChebyshevSightLimit() { return this.enemyChebyshevSightLimit; }

    public void setEnemyChebyshevSightLimit(int i) { this.enemyChebyshevSightLimit = i; }

    public Vertex getStartVertex() {return this.startVertex;}
    public Vertex getTHVertex() {return this.townHallVertex;}
    public Path getCurrPath() {return this.currentPath;}
    public boolean getInfilStatus() {return this.infilDone;}


    ///////////////////////////////////////// Sepia methods to override ///////////////////////////////////

    /**
        TODO: if you add any fields to this class it might be a good idea to initialize them here
              if they need sepia information!
     */
    @Override
    public Map<Integer, Action> initialStep(StateView state,
                                            HistoryView history)
    {
        super.initialStep(state, history); // call AStarAgent's initialStep() to set helpful fields and stuff

        // now some fields are set for us b/c we called AStarAgent's initialStep()
        // let's calculate how far away enemy units can see us...this will be the same for all units (except the base)
        // which doesn't have a sight limit (nor does it care about seeing you)
        // iterate over the "other" (i.e. not the base) enemy units until we get a UnitView that is not null
        List<Integer> myUnitIDSet = new ArrayList<Integer>();
        for(Integer unitID : state.getUnitIds(this.getPlayerNumber())){
            myUnitIDSet.add(unitID);
        }

        if(myUnitIDSet.size() > 1){
            System.err.println("[ERROR] StealthAgent.initialStep: too many frienly units counted");
            System.exit(-1);
        }
        int myUnitID = myUnitIDSet.get(0);
        this.myUnitID = myUnitID;
        UnitView footman = state.getUnit(myUnitID);
        this.startVertex = new Vertex(footman.getXPosition(), footman.getYPosition());


        UnitView otherEnemyUnitView = null;
        Iterator<Integer> otherEnemyUnitIDsIt = this.getOtherEnemyUnitIDs().iterator();
        while(otherEnemyUnitIDsIt.hasNext() && otherEnemyUnitView == null)
        {
            otherEnemyUnitView = state.getUnit(otherEnemyUnitIDsIt.next());
        }

        


        int enemyBaseID = this.getEnemyBaseUnitID();
        UnitView enemyBase = state.getUnit(enemyBaseID);

        this.townHallVertex = new Vertex(enemyBase.getXPosition(), enemyBase.getYPosition());

        System.out.println("Enemy townhall at "+ this.townHallVertex.getXCoordinate() +", "+this.townHallVertex.getYCoordinate());

        Set<Integer> enemyUnitIds = new HashSet<Integer>();
        for(Integer unitID : state.getUnitIds(0)){
            if(unitID != enemyBaseID){
                System.out.println("Found enemy!");
                enemyUnitIds.add(unitID);
                this.enemyIDs.add(unitID);
                UnitView enemyUnit = state.getUnit(unitID);
                Vertex enemyVert = new Vertex(enemyUnit.getXPosition(), enemyUnit.getYPosition());
                this.enemyPos.add(enemyVert);
                System.out.println("Enemy at: "+enemyUnit.getXPosition()+", "+enemyUnit.getYPosition());
            }
        }


        this.currentPhase = AgentPhase.INFILTRATE;

        if(otherEnemyUnitView == null)
        {
            System.err.println("[ERROR] StealthAgent.initialStep: could not find a non-null 'other' enemy UnitView??");
            System.exit(-1);
        }

        // lookup an attribute from the unit's "template" (which you can find in the map .xml files)
        // When I specify the unit's (i.e. "footman"'s) xml template, I will use the "range" attribute
        // as the enemy sight limit
        this.setEnemyChebyshevSightLimit(otherEnemyUnitView.getTemplateView().getRange());

        return null;
    }

    /**
        TODO: implement me! This is the method that will be called every turn of the game.
              This method is responsible for assigning actions to all units that you control
              (which should only be a single footman in this game)
     */
    @Override
    public Map<Integer, Action> middleStep(StateView state,
                                           HistoryView history)
    {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        /**
            I would suggest implementing a state machine here to calculate a path when neccessary.
            For instance beginning with something like:

            if(this.shouldReplacePlan(state))
            {
                // recalculate the plan
            }

            then after this, worry about how you will follow this path by submitting sepia actions
            the trouble is that we don't want to move on from a point on the path until we reach it
            so be sure to take that into account in your design

            once you have this working I would worry about trying to detect when you kill the townhall
            so that you implement escaping
         */

         public float computeEdgeWeight(Vertex start, Vertex end, Stateview gameState, ExtraParams params) {
    
            int targetX = end.getXCoordinate();
            int targetY = end.getYCoordinate();
        
            int destinationX = this.getDestination().getXCoordinate();
            int destinationY = this.getDestination().getYCoordinate();
        
            float riskFactor = 0;
            int enemyVisionRange = this.getEnemyChebyshevSightLimit();
        
            for (Integer enemyId : this.getOtherEnemyUnitIDs()) {
                UnitView enemyUnit = gameState.getUnit(enemyId);
                if (enemyUnit == null) {
                    continue;
                }
        
                int enemyPosX = enemyUnit.getXPosition();
                int enemyPosY = enemyUnit.getYPosition();
                float proximityToEnemy = (float) Math.max(Math.abs(enemyPosX - targetX), Math.abs(enemyPosY - targetY));
        
                if (proximityToEnemy <= enemyVisionRange + 1) {
                    logger.finest("Node " + end + " is within enemy detection range.");
                    return Float.POSITIVE_INFINITY;
                } else {
                    riskFactor += enemyVisionRange / (proximityToEnemy - enemyVisionRange - 1);
                }
            }
        
            float heuristicToGoal = (float) Math.max(Math.abs(destinationX - targetX), Math.abs(destinationY - targetY));
            logger.finest("Path from " + start + " to " + end + " has a computed weight of " + (heuristicToGoal + riskFactor));
        
            return heuristicToGoal + riskFactor;
        }

        return actions;
    }

    ////////////////////////////////// End of Sepia methods to override //////////////////////////////////

    /////////////////////////////////// AStarAgent methods to override ///////////////////////////////////

    public Collection<Vertex> getNeighbors(Vertex v,
                                           StateView state,
                                           ExtraParams extraParams)
    {
        return null;
    }

    public Path aStarSearch(Vertex src,
                            Vertex dst,
                            StateView state,
                            ExtraParams extraParams)
    {
        return null;
    }

    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state,
                               ExtraParams extraParams)
    {
        return 1f;
    }

    public boolean shouldReplacePlan(StateView state,
                                     ExtraParams extraParams)
    {
        return false;
    }

    //////////////////////////////// End of AStarAgent methods to override ///////////////////////////////

}

