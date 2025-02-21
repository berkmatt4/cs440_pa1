package src.labs.stealth.agents;

import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Path;
import edu.bu.labs.stealth.graph.Vertex;
import edu.cwru.sepia.environment.model.state.State.StateView;

import java.util.HashSet;
import java.util.Stack;
import java.util.Set;

public class DFSMazeAgent extends MazeAgent {

    private Vertex goal; //store the goal vertex for global reference

    public DFSMazeAgent(int playerNum) {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src, 
                        Vertex goal, 
                        StateView state)
    {
        //store the goal vertex
        this.goal = goal;

        //stack for DFS traversal
        Stack<Path> stack = new Stack<>();

        //set to keep track of visited vertices
        Set<Vertex> visited = new HashSet<>();

        stack.push(new Path(src, 1.0f, null));
        visited.add(src);

        while(!stack.isEmpty()){
            Path currentPath = stack.pop();
            Vertex currentVertex = currentPath.getDestination();

            //see what the current vertex is
            System.out.println("Current vertex: " + currentVertex.getXCoordinate() + ", " + currentVertex.getYCoordinate());

            //if curret is goal, we are done
            if(currentVertex.equals(goal)){
                return currentPath;
            }

            //for each loop adds all adjascent vertices to the neighbor list
            //also pushes each neighbor onto the stack
            for(Vertex neighbor : getAdjacentVertices(currentVertex, state)){
                if(neighbor != null && !visited.contains(neighbor)){
                    visited.add(neighbor);
                    //add the neighbor to the stack with a new path
                    stack.push(new Path(neighbor, 1.0f, currentPath));
                }
            }
        }

        return null;
    }

    /*same helper method for BFS */
    private Set<Vertex> getAdjacentVertices(Vertex current, StateView state) {
        Set<Vertex> neighbors = new HashSet<>();
        int x = current.getXCoordinate();
        int y = current.getYCoordinate();

        System.out.println("Checking adjacent vertices for: " + x + ", " + y);

        for(int dx = -1; dx <= 1; dx++){
            for(int dy = -1; dy <= 1; dy++){
                if(dx == 0 && dy == 0){
                     continue;
                }

                int newX = x + dx;
                int newY = y + dy;

                //check if the new position is valid
                if (isPositionValid(newX, newY, state)){
                    neighbors.add(new Vertex(newX, newY));
                } 
                else{
                    //is it invalid
                    System.err.println("Invalid position: " + newX + ", " + newY );
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
    private boolean isPositionValid(int x, int y, StateView state) {
        //case: goal
        if (goal != null && x == goal.getXCoordinate() && y == goal.getYCoordinate()) {
            return true;
        }

        //check if the position is within the map bounds
        if (x < 0 || y < 0 || x >= state.getXExtent() || y >= state.getYExtent()) {
            System.err.println("Position out of bounds: " + x + ", " + y);
            return false;
        }

        //check if position is blocked by unit or resource
        boolean isBlocked = state.isResourceAt(x, y) || state.isUnitAt(x, y);
        if (isBlocked) {
            System.err.println("Position blocked: " + x + ", " + y);
        }
        return !isBlocked;
    }
}