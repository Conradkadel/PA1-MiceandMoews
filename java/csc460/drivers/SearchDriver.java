package csc460.drivers;

import csc460.Drawer;
import csc460.Board;
import csc460.BoardCoordinate;
import csc460.SearchNode;
import csc460.searchproblems.*;
import csc460.searchstrategies.*;
import csc460.SearchState;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

/**
 * A driver for generic search problems.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class SearchDriver implements Driver {
    public static final int SPOT_SIZE = 60;
    public static final int MARGIN_SIZE = 5;
    public static final int FPS = 3;
    private final Color SELECTED_PLAN_COLOR = Color.ORANGE;
    private Board board;
    private SearchProblem problem;
    private SearchStrategy strategy;
    private boolean solutionFound;
    private final int ALPHA_STEP = 2;
    private final int ALPHA_START = 15;
    private int alpha;
    private Color searchColor;
    private String boardFile;
    private int steps = 0;

    /**
     * Initializes the driver. This calls the init() method of the given 
     * strategy, so that doesn't need to be done before hand. It also takes 
     * care of loading the starting board.
     * 
     * @param problem The search problem to solve.
     * @param strategy The search strategy to use to find a solution to the
     *                  problem.
     * @param boardFile The filename of the starting state. This is given to the
     *                  search problem's loadBoardFile() method.
     */
    public SearchDriver(SearchProblem problem, SearchStrategy strategy,
            String boardFile) throws FileNotFoundException {
        solutionFound = false;
        this.problem = problem;
        this.strategy = strategy;
        alpha = ALPHA_START;
        this.boardFile = boardFile;
        loadBoardFile();
        strategy.init(problem);

    }

    /**
     * Gets the next search node to expand from the search strategy and updates
     * the board. When a solution is found, it highlights the solution. Stats
     * about the solution and search strategy are displayed to stdout.
     *
     * @return True if there's more work to be done. False if the solution was
     *         previously found or an error occurred (e.g., there are no more
     *         states to expand). Essentially indicate if there are board
     *         changes that require repainting the screen.
     */
    public boolean step(){
        if(solutionFound){
            return false;
        }



        // Get the next state to explore.
        SearchNode searchNode = strategy.nextNode();

        String lastAction = "";

        // This should happen if tere is no solution (the entire state space 
        // has been explored).
        if(searchNode == null){
            return false;
        }

        // We reached a goal state.
        if(problem.isGoal(searchNode.state)){

            // Highlight the solution path on the board.
            for(int i = 0; i < searchNode.pathCoords.size()-1; i++) {
                BoardCoordinate prevCoord, curCoord, nextCoord;
                String actionPath;
                prevCoord = null;
                if (i > 0) {
                    prevCoord = searchNode.pathCoords.get(i - 1);
                } else {
                    prevCoord = problem.getStartState().getAgentCoordinates();
                }
                curCoord = searchNode.pathCoords.get(i);
                nextCoord = searchNode.pathCoords.get(i + 1);

                actionPath = getActionPathCode(prevCoord, curCoord, nextCoord);
        
                board.setColor(curCoord, SELECTED_PLAN_COLOR);
                board.setDirection(curCoord, actionPath);
            }

            solutionFound = true;

            System.out.print(
                "\nSearch strategy: "+ strategy.getClass().getCanonicalName() +
                "\nStates expanded: "+ strategy.getNumStatesExpanded() +
                "\nMax fringe size: "+ strategy.getMaxFringeSize() +
                "\nSolution cost: "+ searchNode.cost +
                "\nSolution path length: "+ searchNode.pathActions.size() +
                "\nSolution path (actions): ");
            for(String action : searchNode.pathActions){
                System.out.print(action.charAt(0));
            }
            System.out.println();

        // Just another move; paint the spot on the board with the next shade
        // of blue.
        } else if(!searchNode.state.getAgentCoordinates().equals(
                problem.getStartState().getAgentCoordinates())){
            searchColor = new Color(0, 0, 255, alpha);
            board.setColor(searchNode.state.getAgentCoordinates(), searchColor);
            alpha = Math.min(alpha+ALPHA_STEP, 255);
        }

        return true;
    }

    /** 
     * Determines the path code (e.g., "u" for "up" or "dr" for "down right")
     * based on the previous, current, and next coordinates. This is used to 
     * determine which path images to load in each spot on the agent's path.
     * 
     * @param prevCoord The coordinate before the current coordinate.
     * @param curCoord The current coordinate (where the path is being drawn).
     * @param nextCoord The coordinate after the current coordinate.
     * 
     * @return The action path code string.
     */
    private String getActionPathCode(BoardCoordinate prevCoord, BoardCoordinate curCoord, 
            BoardCoordinate nextCoord) {
        String actionPath;
        if (prevCoord.x == nextCoord.x) {
            // Down
            if (prevCoord.y < nextCoord.y)
                actionPath = "d";
            // Up
            else
                actionPath = "u";
        } else if (prevCoord.y == nextCoord.y) {
            // Right
            if(prevCoord.x < nextCoord.x)
                actionPath = "r";
            // Left
            else
                actionPath = "l";

        // Down right
        } else if(nextCoord.x > curCoord.x && prevCoord.y < curCoord.y){
            actionPath = "dr";

        // Left up
        } else if(prevCoord.x > curCoord.x && nextCoord.y < curCoord.y){
            actionPath = "lu";

        // Right up
        } else if(prevCoord.x < curCoord.x && nextCoord.y < curCoord.y ){
            actionPath = "ru";
        
        // Down left
        } else if(nextCoord.x < curCoord.x && prevCoord.y < curCoord.y){
            actionPath = "dl";

        // Up left
        } else if(nextCoord.x < curCoord.x && prevCoord.y > curCoord.y){
            actionPath = "ul";

        // Right down
        }  else if(prevCoord.x < curCoord.x && nextCoord.y > curCoord.y){
            actionPath = "rd";

        // Left down
        } else if(prevCoord.x > curCoord.x && nextCoord.y > curCoord.y){
            actionPath = "ld";

        // Up right
        } else { // if(nextCoord.x > curCoord.x && prevCoord.y > curCoord.y){
            actionPath = "ur";
        }
        return actionPath;
    }

    /**
     * Loads the initial board (i.e., the starting state). The parsing is 
     * offloaded to the search problem's loadBoardFile method.
     */
    public void loadBoardFile() throws FileNotFoundException {
        board = problem.loadBoardFile(boardFile);
    }

    /**
     * @return The current board colors.
     */
    public Board getBoard(){
        return board;
    }

    /**
     * Parses the command line arguments to find a solution to the specified problem.
     * 
     * @param args See "usage" below.
     */
    public static void main(String[] args){
        String heuristic = "";
        String boardFile = null;
        SearchStrategy searchStrategy = null;
        SearchProblem searchProblem = null;
        SearchDriver searchDriver = null;
        int spotSize = SearchDriver.SPOT_SIZE;
        int marginSize = SearchDriver.MARGIN_SIZE;
        int fps = SearchDriver.FPS;
        String title = "";
        boolean graphicsOn = true;

        String usage = "Usage: SearchDriver [options]\n\n"+
            "REQUIRED Arguments:\n"+
            "   -p=P -- P is the search problem; current options:\n"+
            "             * maze (find path from entrance to exit)\n"+
            "   -s=S -- S is the search strategy:\n"+
            "             * bfs -- Breadth First Search\n"+
            "             * dfs -- Depth First Search\n"+
            "             * id  -- Iterative Deepening\n"+
            "             * ucs -- Uniform Cost Search\n"+
            "             * greedy -- Greedy Search\n"+
            "   -f=F -- F is the filename of the board to read in; settings:\n"+
            "           maze:\n"+
            "             s -- the agent's starting position\n"+
            "             e -- the exit from the maze (only one)\n"+
            "             w -- a wall (agent cannot enter)\n"+
            "             [space] -- an open spot where the agent is allowed to enter\n"+
            "OPTIONAL arguments\n"+
            "   -h=H -- H is the distance heuristic:\n"+
            "           for p=maze:\n"+
            "             [blank] -- no heuristic\n"+
            "             manhattan -- Manhattan distance from a state to the exit\n"+
            "             euclidean -- Euclidean distance from a state to the exit\n"+
            "   -fps=FPS -- FPS is the frames per second; default is 3\n"+
            "   -spotSize=S -- S is the size of squares; default is 60\n"+
            "   -marginSize=S -- S is he size of the gap between spots;\n"+ 
            "             default is 5\n"+
            "   -graphics=on|off -- if on (default), a window will appear showing\n"+
            "             the progress of the search\n"+
            "\n\n";

        if(args.length < 3){
            System.err.print(usage);
            return;
        }

        // Parse options.
        for(String arg : args){
            // Search problem (-p=).
            if(arg.startsWith("-p=")){
                String problemCode = arg.substring(3);
                title += problemCode +"|";

                // Maze.
                if(problemCode.equals("maze")){
                    searchProblem = new Maze();


                // Invalid option.
                } else {
                    System.err.print("Unrecognized search problem: "+ 
                        problemCode +"\n\n"+ usage);
                    return;
                }

            // Search strategy (-s=).
            } else if(arg.startsWith("-s=")) {
                String strategyCode = arg.substring(3);
                title += strategyCode +"|";

                // Breadth first search
                if(strategyCode.equals("bfs")){
                    searchStrategy = new BFS();

                // Depth first search
                } else if(strategyCode.equals("dfs")){
                    searchStrategy = new DFS();

                // Iterative deepening
                } else if(strategyCode.equals("id")){
                    searchStrategy = new IterativeDeepening();

                // Uniform cost search
                } else if(strategyCode.equals("ucs")){
                    searchStrategy = new UCS();

                // Greedy search
                } else if(strategyCode.equals("greedy")){
                    searchStrategy = new Greedy();
            

                    // Invalid option.
                } else {
                    System.err.print("Unrecognized search strategy: "+ 
                        strategyCode +"\n\n"+ usage);
                    return;
                }

            // Board file (-f=).
            } else if(arg.startsWith("-f=")){
                boardFile = arg.substring(3);
                title += boardFile +"|";

            // Heuristic (-h=).
            } else if(arg.startsWith("-h=")){
                heuristic = arg.substring(3);
                title += heuristic +"|";

            // Frames per second (-fps=).
            } else if(arg.startsWith("-fps=")){
                fps = Integer.parseInt(arg.substring(5));

            // Spot size (-spotSize=).
            } else if(arg.startsWith("-spotSize=")){
                spotSize = Integer.parseInt(arg.substring(10));
            
            // Margin size (-marginSize=).
            } else if(arg.startsWith("-marginSize=")){
                spotSize = Integer.parseInt(arg.substring(12));

            } else if(arg.startsWith("-graphics=")){
                graphicsOn = arg.substring(10).equals("on");


            // Invalid option.
            } else {
                System.err.print("Unrecognized option: "+ arg +"\n\n"+ usage);
                return;
            }
        }

        // Make sure we have each of the required components.
        if(searchProblem == null || searchStrategy == null || boardFile == null){
                System.err.print("The following arguments are missing: ");
                if(searchProblem == null)
                    System.err.print("-p ");
                if(searchStrategy == null)
                    System.err.print("-s ");
                if(boardFile == null)
                    System.err.print("-f ");
                System.err.print("\n\n"+ usage);
                return;
        }

        searchProblem.setHeuristic(heuristic);

        try{
            searchDriver = new SearchDriver(
                searchProblem, searchStrategy, boardFile);

            if(graphicsOn){
                final JFrame ex = new Drawer(searchDriver, title, spotSize, 
                    marginSize, fps);

                EventQueue.invokeLater(() -> {
                    ex.setVisible(true);
                });
            } else {
                while(searchDriver.step()) ;
            }
        } catch(FileNotFoundException ex){
            System.err.println("Couldn't open board file '"+ boardFile +"': "+ 
                ex.getLocalizedMessage());
        }
    }
}
