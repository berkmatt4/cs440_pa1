package src.labs.stealth.agents;

import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Path;
import edu.bu.labs.stealth.graph.Vertex;
import edu.cwru.sepia.environment.model.state.State.StateView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BFSMazeAgent extends MazeAgent {

    private Vertex goal; //store the goal vertex for global reference

    public BFSMazeAgent(int playerNum) {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src, 
                        Vertex goal, 
                        StateView state)
    {
        this.goal = goal;


        //queue of nodes to be traversed
        Queue<Path> queue = new LinkedList<>();

        //keep track of what we've already visited
        Set<Vertex> visited = new HashSet<>();

        queue.add(new Path(src, 0.0f, null));
        visited.add(src);

        
        while(!queue.isEmpty()){
            Path currentPath = queue.poll();   
            Vertex currentVertex = currentPath.getDestination();

            //see where we are right now
            System.out.println("Current vertex: " + currentVertex.getXCoordinate() + ", " + currentVertex.getYCoordinate());

            //if we reached the goal, we are done
            if(currentVertex.equals(goal)){
                return currentPath;
            }

            /*main logic for adding vertices to the queue
             * for each loop parses over all neighbors returned by the helper
             * marks each one as visited and adds the path to the neighbor
             *to the queue*/
            for(Vertex neighbor : getAdjacentVertices(currentVertex, state)){
                if(neighbor != null && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    // Add the neighbor to the queue with a new path
                    queue.add(new Path(neighbor, 1.0f, currentPath));
                }
            }
        }

        //no path, return null
        return null;
    }

    /*helper function to get all vertices adjacent to a given vertex
     * searches x,y coordinates that are to the left, right, up, down,
     * and diagonals from current x, y coordinates
     */
    private Set<Vertex> getAdjacentVertices(Vertex current, StateView state){
        Set<Vertex> neighbors = new HashSet<>();
        int x = current.getXCoordinate();
        int y = current.getYCoordinate();

        //i had to make sure I was checking the correct vertices adjacent to this one lol
        System.out.println("Checking adjacent vertices for: " + x + ", " + y);

        /* use all possible differences in x and y values to get all neighbors
         * x+1, x+0, x-1, y+1, y+0, y-1
         */
        for(int dx = -1; dx <= 1; dx++){
            for(int dy = -1; dy <= 1; dy++){
                if(dx == 0 && dy == 0){ 
                    continue;               //this is the current vertex
                }

                int newX = x + dx;
                int newY = y + dy;

                //print pos being checked
                System.out.println("checking position: " + newX + ", " + newY );

                //check if the new position is valid
                if(isPositionValid(newX, newY, state)){
                    neighbors.add(new Vertex(newX, newY));
                } 
                else{
                    //print if invalid
                    System.err.println("Invalid position: " + newX + ", " + newY);
                }
            }
        }

        return neighbors;
    }

    /*helper to ensure that when adding positions into the neighbors list
     * that they are within the map, and that there isn't a resource or unit
     * where that position is
     * special check: goal position should always be valid
     */
    private boolean isPositionValid(int x, int y, StateView state){
        //case: goal position
        if (goal != null && x == goal.getXCoordinate() && y == goal.getYCoordinate()){
            return true; //goal is always valid
        }

        //check if the position is within the map bounds
        if (x < 0 || y < 0 || x >= state.getXExtent() || y >= state.getYExtent()){
            System.err.println("Position out of bounds: " + x + ", " + y);
            return false;
        }

        //check if there is a resource or unit
        boolean isBlocked = state.isResourceAt(x, y) || state.isUnitAt(x, y);
        if (isBlocked) {
            System.err.println("Position blocked: " + x + ", " + y );
        }
        return !isBlocked;
    }
}