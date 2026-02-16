// EXCLUDE FILE
package csc460.searchstrategies;

import csc460.searchproblems.SearchProblem;
import csc460.BoardCoordinate;
import csc460.SearchNode;
import csc460.SearchState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * A*: states are explored based on the cost to get to the state (backwards cost) +
 * their estimated distance to a goal state.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class AStar implements SearchStrategy {
    SearchProblem problem;
    PriorityQueue<SearchNode> fringe;
    int numStatesExpanded;
    int maxFringeSize;
    // SOLUTION FOR ADVANCED
    HashSet<SearchState> seen;
    boolean useGraphSearch;
    int counter = 0;

    /**
     * Initializes the fringe so it only holds the starting state. Initializes the 
     * seen set and all the stats.
     * 
     * @param problem The problem to solve.
     * @param useGraphSearch Whether or not to use graph search. If false, tree search will be used.
     */
    public void init(SearchProblem problem, boolean useGraphSearch){
        SearchNode root = new SearchNode(
            problem.getStartState(), 
            new ArrayList<String>(),
            new HashSet<SearchState>(), 
            new ArrayList<BoardCoordinate>(), 
            0.0); // Priority: h(x) -- we call it the "distance" to a goal state in
                  // this implementation.
        root.pathStates.add(problem.getStartState());
        fringe= new PriorityQueue<SearchNode>();
        fringe.add(root);
        this.problem = problem;
        numStatesExpanded = 0;
        maxFringeSize = 1;
        // SOLUTION FOR ADVANCED        
        this.useGraphSearch = useGraphSearch;
        if(useGraphSearch){
            seen = new HashSet<SearchState>();
            seen.add(problem.getStartState());
        }
    }

    /**
     * Finds the next node to expand. Adds successor states that are unexpanded 
     * on the current path to the fringe.
     * 
     * @return The next node to expand. Null if there are no more nodes left 
     *         to explore.
     */
    public SearchNode nextNode(){
        if(fringe.isEmpty()){
            return null;
        }

        SearchNode node = fringe.poll();

        expandNode(node);

        // Update stats.
        numStatesExpanded++;
        maxFringeSize = Math.max(maxFringeSize, fringe.size());

        return node;
    }

    /**
     * Adds the unexplored successor states of the given node's state to the 
     * fringe.
     * 
     * @param node The state whose successors should be added to the fringe.
     */
    public void expandNode(SearchNode node){
        for(SearchState successor : problem.getSuccessors(node.state)){
            // SOLUTION FOR ADVANCED
            if(useGraphSearch && seen.contains(successor) ||
               !useGraphSearch && node.pathStates.contains(successor)){
                continue;
            }
            // SOLUTION FOR ADVANCED
            if(useGraphSearch){
                seen.add(successor);
            }

            // Make a deep copy of the search states.
            HashSet<SearchState> pathStates = 
                new HashSet<SearchState>(node.pathStates);
            pathStates.add(successor);            

            ArrayList<String> pathActions = 
                new ArrayList<String>(node.pathActions);
            pathActions.add(successor.getAction());

            ArrayList<BoardCoordinate> pathCoords = 
                new ArrayList<BoardCoordinate>(node.pathCoords);
            pathCoords.add(successor.getAgentCoordinates());

            fringe.add(new SearchNode(
                successor,
                pathActions,
                pathStates,
                pathCoords,
                node.cost + successor.getCost(), // Backward cost: g(x)
                node.cost + successor.getCost() + successor.getDistance() // Priority: f(x) = g(x) + h(x)
                ));

            counter++;
        }
    }

    /**
     * @return The current number of states expanded.
     */
    @Override
    public int getNumStatesExpanded() {
        return numStatesExpanded;
    }

    /**
     * @return The maximum size of the fringe so far.
     */
    @Override
    public int getMaxFringeSize() {
        return maxFringeSize;
    }
}
