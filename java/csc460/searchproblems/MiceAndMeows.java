package csc460.searchproblems;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Scanner;

import csc460.Board;
import csc460.BoardCoordinate;
import csc460.SearchState;
import java.awt.Color;

/**
 * Solves a Mice and Meows problem, where the agent (a cat) starts at a given spot then has to
 * eat up all the mice on a grid board and finally make it to the door. Water spots (puddles)
 * cost more for the cat to go into due to the cat not liking water.
 * See the loadBoardFile method for the board representation.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class MiceAndMeows implements SearchProblem {
    private SearchState startState;
    private BoardCoordinate exit;
    private BoardCoordinate start;
    // Used for determining next moves.
    private ArrayList<ArrayList<Character>> internalBoard;
    private HashMap<Character,Color> colorMap;
    private String heuristic;
    private TreeSet<BoardCoordinate> water;

    /**
     * Represents a maze state, where we only need to know the location of
     * the agent as well as details about that location (what it costs,
     * estimated distance to the exit, how the agent moved into the spot).
     */
    public class MiceAndMeowsState extends SearchState {
        private BoardCoordinate agentCoord;
        private TreeSet<BoardCoordinate> mice;
        private String action;
        private double cost, distance;

        /**
         * Initializes the maze state.
         * 
         * @param agentCoord Agent's location on the board. 
         * @param action The action that led to this state.
         * @param cost The cost of moving to this state.
         * @param distance The estimated distance to the exit.
         */
        public MiceAndMeowsState(BoardCoordinate agentCoord, TreeSet<BoardCoordinate> mice,
                String action, double cost,
                double distance){
            this.agentCoord = agentCoord;
            this.action = action;
            this.cost = cost;
            this.distance = distance;
            this.mice = mice;
        }

        /**
         * @return The location of the agent.
         */
        @Override
        public BoardCoordinate getAgentCoordinates() {
            return agentCoord;
        }

        /**
         * @return The mice locations in a set.
         */
        public TreeSet<BoardCoordinate> getMiceCoordinates() {
            return mice;
        }

        /**
         * @return The cost to reach this state.
         */
        @Override
        public double getCost() {
            return cost;
        }

        /**
         * @return The estimated distance to the goal.
         */
        @Override
        public double getDistance() {
            return distance;
        }

        /**
         * @return The action that led to this state.
         */
        @Override
        public String getAction() {
            return action;
        }

        /**
         * @return A string representation of the state.
         */
        public String toString(){
            StringBuffer description = new StringBuffer(agentCoord.toString());
            for(BoardCoordinate coord : mice){
                description.append(coord.toString());
            }
            return description.toString();
        }

    }

    /**
     * Creates a map of character codes (from the board file) to colors.
     */
    public MiceAndMeows() {
        colorMap = new HashMap<Character,Color>();
        colorMap.put(' ', Color.WHITE);
        colorMap.put('s', Color.GREEN);
        colorMap.put('e', Color.RED);
        colorMap.put('w', Color.BLACK);
    }

    /**
     * Sets the heuristic to use to estimate the distance to a goal state. 
     * The following are currently supported:
     * 
     *   - manhattan
     *   - euclidean
     *   - mice-remaining
     */
    @Override
    public void setHeuristic(String heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * @return The starting state.
     */
    @Override
    public SearchState getStartState() {
        return startState;
    }

    /**
     * Determines if the given state is a goal state.
     * 
     * @param state The state to test.
     * @return True if the agent has reached the exit.
     */
    @Override
    public boolean isGoal(SearchState state) {
        return ((MiceAndMeowsState) state).getMiceCoordinates().isEmpty() && state.getAgentCoordinates().equals(exit);
    }

    /**
     * Estimates the distance from the given coordinate to a goal state using the
     * heuristic set for the instance.
     * 
     * @param coord A spot on the board.
     * 
     * @return An estimate of the distance from the given coordinate to the 
     *         exit.
     */
    public double getDistance(BoardCoordinate coord, TreeSet<BoardCoordinate> mice){
        if(heuristic.equals("manhattan")){
            return Math.abs(coord.x - exit.x) + Math.abs(coord.y - exit.y);
        } else if(heuristic.equals("euclidean")){
            return Math.sqrt(Math.pow(coord.x-exit.x, 2) + 
                             Math.pow(coord.y-exit.y, 2));

        } else if(heuristic.equals("mice-remaining")){
            if(mice.size() > 0){
                return mice.size();
            } else {
                return Math.abs(coord.x - exit.x) + Math.abs(coord.y - exit.y);
            }
        } else {
            return 0;
        }
    }

    /**
     * Finds all of the states that can be reached from the given state.
     * 
     * @param state The state to find successors of.
     * @return A collection of the states to the left, up, right, and down of 
     *         the given state.
     */
    @Override
    public Iterable<SearchState> getSuccessors(SearchState state) {
        ArrayList<SearchState> successors = new ArrayList<SearchState>();

        BoardCoordinate curCoords = state.getAgentCoordinates();

        // This speeds up checking the four directions (so we can use a loop).
        String[] actions = {"left", "up", "right", "down"};
        BoardCoordinate[] successorCoords = {
            new BoardCoordinate(curCoords.x-1, curCoords.y), // left
            new BoardCoordinate(curCoords.x, curCoords.y-1), // up
            new BoardCoordinate(curCoords.x+1, curCoords.y), // right
            new BoardCoordinate(curCoords.x, curCoords.y+1) // down
        };

        // Look in each direction around the agent. If it's in bounds, add it to the list
        // of successors.
        for(int i = 0; i < actions.length; i++){
            if(successorCoords[i].y >= 0 && 
                    successorCoords[i].y < internalBoard.size() &&
                successorCoords[i].x >= 0 && 
                    successorCoords[i].x < internalBoard.get(0).size()){

                // Set the cost: 6 for water, 1 for every other spot.
                int cost = 1;
                if (internalBoard.get(successorCoords[i].y).get(successorCoords[i].x) == 'w' ){
                    cost = 6;
                }

                // Update the list of mice if one is colocated with this spot.
                TreeSet<BoardCoordinate> mice = new TreeSet<BoardCoordinate>(((MiceAndMeowsState) state).getMiceCoordinates());
                if(mice.contains(successorCoords[i])){
                    mice.remove(successorCoords[i]);
                }

                successors.add(new MiceAndMeowsState(successorCoords[i], mice, actions[i], 
                    cost, getDistance(successorCoords[i], mice)));
            }
        }

        return successors;
    }

    /**
     * Parses a Mice and Meows file. Should contain one character per spot.
     * Here are the character codes:
     *   s -- the agent's starting position
     *   e -- exit (if reached and all mice have been collected, the goal state has been reached)
     *   w -- water (cost: 6)
     *   m -- mouse (all of these must be collected, plus the exit reached in order for the goal test to pass)
     *   (blank) -- a spot the agent may enter (cost: 1)
     * 
     * @param filename The name of the maze file to load.
     * @return A color mapped version of the maze.
     */
    @Override
    public Board loadBoardFile(String filename) throws FileNotFoundException {
        Scanner reader = new Scanner(new File(filename));
        String line;
        char spot;
        int x = 0, y = 0;
        Board board;
        TreeSet<BoardCoordinate> mice = new TreeSet<BoardCoordinate>();
        internalBoard = new ArrayList<ArrayList<Character>>();

        while(reader.hasNextLine()){
            line = reader.nextLine();
            ArrayList<Character> row = new ArrayList<Character>();

            for(x = 0; x < line.length(); x++){
                spot = line.charAt(x);

                // Add the spot to the internal representation.
                row.add(spot);
                if(spot == 's'){
                    start = new BoardCoordinate(x, y);
                } else if(spot == 'e') {
                    exit = new BoardCoordinate(x, y);
                } else if(spot == 'm') {
                    mice.add(new BoardCoordinate(x,y));
                }
            }
            internalBoard.add(row);
            y++;
        }
        reader.close();

        // Initializes the start state.
        startState = new MiceAndMeowsState(start, mice, "", 0.0, getDistance(start, mice));

        board = new Board(internalBoard.size(), internalBoard.get(0).size());
        // Map the spot to a color and add that to the external
        // board representation.

        y = 0;
        for(ArrayList<Character> row : internalBoard){
            x = 0;
            for(Character c : row){
                board.setColor(new BoardCoordinate(x,y), colorMap.get(c.charValue()));
                if(c == ' '){
                    System.out.print('_');
                } else {
                    System.out.print(c);
                }
                x++;
            }
            System.out.println();
            y++;
        }

        return board;
    }
    
}
