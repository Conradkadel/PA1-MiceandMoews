package csc460;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Stores the color that should be stored at each spot in the board.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class Board {
    public static final Color DEFAULT_COLOR = Color.WHITE;
    public ArrayList<ArrayList<Color>> board;
    public ArrayList<ArrayList<String>> directions; // The direction the agent is facing at each spot.
    private int numRows;
    private int numCols;

    /**
     * Initializes the board to the given size. Uses the DEFAULT_COLOR for 
     * all spots.
     * 
     * @param numRows The number of rows the board should be.
     * @param numCols The number of columns the board should be.
     */
    public Board(int numRows, int numCols){
        board = new ArrayList<ArrayList<Color>>();
        directions = new ArrayList<ArrayList<String>>();
        this.numRows = numRows;
        this.numCols = numCols;

        for(int i = 0; i < numRows; i++){
            ArrayList<Color> row = new ArrayList<Color>();
            ArrayList<String> directionRow = new ArrayList<String>();
            for(int j = 0; j < numCols; j++){
                row.add(DEFAULT_COLOR);
                directionRow.add(""); 
            }
            board.add(row);
            directions.add(directionRow);
        }
    }

    /**
     * Sets the color of the given spot on the board.
     * 
     * @param coord The spot of the board whose color should be set.
     * @param color The color to set that spot.
     */
    public void setColor(BoardCoordinate coord, Color color){
        board.get(coord.y).set(coord.x, color);
    }

    /**
     * Sets the direction of the given spot on the board.
     * 
     * @param coord The spot of the board whose direction should be set.
     * @param direction The direction to set that spot to.
     */
    public void setDirection(BoardCoordinate coord, String direction){
        directions.get(coord.y).set(coord.x, direction);
    }

    /**
     * @return The number of rows of the board.
     */
    public int numRows(){
        return numRows;
    }

    /**
     * @return The number of columns of the board.
     */
    public int numCols(){
        return numCols;
    }
}
