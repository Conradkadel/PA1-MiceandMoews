package csc460;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;


import csc460.drivers.Driver;

/**
 * Handles drawing the board on the screen and calling the step() method of the
 * given driver. The screen is updated after each call to step().
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class Drawer extends JFrame {

    /**
     * Initializes the board.
     * 
     * @param driver The object responsible for making updates to the (logical) board.
     * @param title The title to display at the top of the window.
     * @param spotSize The height and width of each spot in pixels.
     * @param marginSize The space to either side of each spot in pixels.
     * @param framesPerSecond The frequency to call the step() method of the
     *                        driver.
     */
    public Drawer(Driver driver, String title, int spotSize, int marginSize, 
            int framesPerSecond) {
        add(new GraphicalBoard(driver, spotSize, marginSize, framesPerSecond));
        setResizable(false);
        pack();
        setTitle(title);    
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }

    // Draws the board and also calls the necessary updates along the way.
    class GraphicalBoard extends JPanel
            implements Runnable {

        Driver driver;
        int delay;
        int width;
        int height;
        int marginSize;
        int spotSize;
        BufferedImage leftRightPath, downUpPath, downRightLeftUpPath, 
            rightDownUpLeftPath, leftDownUpRight, downLeftRightUpPath; 
        HashMap<String, BufferedImage> directionImages;

        private Thread animator;

        /**
         * Initializes the window. The window size is based on the size of the 
         * board retrieved from the driver. The board consists of a grid of 
         * spots and maps directly from the board retrieved from the driver.
         * 
         * @param driver The object responsible for making updates to the board.
         * @param spotSize The height and width of each spot.
         * @param marginSize The space to either side of each spot.
         * @param framesPerSecond The frequency to call the step() method of the
         *                        driver.
         */
        public GraphicalBoard(Driver driver, int spotSize, int marginSize, 
                int framesPerSecond) {
            delay = 1000/framesPerSecond;
            this.driver = driver;
            this.spotSize = spotSize;
            this.marginSize = marginSize;

            // Compute the window dimensions based on the board size.
            height = driver.getBoard().numRows()*(spotSize+marginSize) + 
                     marginSize;
            width = driver.getBoard().numCols()*(spotSize+marginSize) + 
                     marginSize;

            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(width, height));

            // Load images for path directions in the solution path.
            try {
                downUpPath = ImageIO.read(getClass().getResource("images/down-up.png"));
                leftRightPath = ImageIO.read(getClass().getResource("images/left-right.png"));
                downRightLeftUpPath = ImageIO.read(getClass().getResource("images/downright-leftup.png"));
                rightDownUpLeftPath = ImageIO.read(getClass().getResource("images/rightdown-upleft.png"));
                leftDownUpRight = ImageIO.read(getClass().getResource("images/leftdown-upright.png"));
                downLeftRightUpPath = ImageIO.read(getClass().getResource("images/downleft-rightup.png"));

                directionImages = new HashMap<String, BufferedImage>();
                directionImages.put("u", downUpPath);
                directionImages.put("d", downUpPath);
                directionImages.put("l", leftRightPath);
                directionImages.put("r", leftRightPath);
                directionImages.put("dr", downRightLeftUpPath);
                directionImages.put("lu", downRightLeftUpPath);
                directionImages.put("rd", rightDownUpLeftPath);
                directionImages.put("ul", rightDownUpLeftPath);
                directionImages.put("ld", leftDownUpRight);
                directionImages.put("ur", leftDownUpRight);
                directionImages.put("dl", downLeftRightUpPath);
                directionImages.put("ru", downLeftRightUpPath);
                directionImages.put("", null);
            } catch (IOException e) {
                String msg = String.format("Error loading images: %s", 
                    e.getMessage());
                
                JOptionPane.showMessageDialog(this, msg, "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        /** 
         * Called when the panel is added to the JFrame. Starts the animation 
         * thread.
         */
        @Override
        public void addNotify() {
            super.addNotify();
            animator = new Thread(this);
            animator.start();
        }

        /**
         * Called when the panel needs to be redrawn.
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
        }

        /**
         * Draws the board to the screen.
         * 
         * @param g The drawing interface.
         */
        private void drawBoard(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;
            int x = 0, y = 0;

            for(y = 0; y < driver.getBoard().numRows(); y++){
                for(x = 0; x < driver.getBoard().numCols(); x++){
                    Color color = driver.getBoard().board.get(y).get(x);
                    String direction = driver.getBoard().directions.get(y).get(x);
                    ArrayList<BufferedImage> pathImages = new ArrayList<BufferedImage>();
                    if(directionImages.get(direction) != null){
                        pathImages.add(directionImages.get(direction));
                    } 
                    drawSpot(g2D, x, y, color, pathImages);
                }
            }

            Toolkit.getDefaultToolkit().sync();
        }

        /**
         * Draws a square on the board at the given coordinate with the given
         * color.
         * 
         * @param g2D The drawing interface.
         * @param x The x value of the internal spot to draw (from board).
         * @param y The y value of the internal spot to draw (from board).
         * @param color The color to paint the spot.
         * @param images An optional list of images to draw on top of the spot (can be null).
         */
        private void drawSpot(Graphics2D g2D, int x, int y, Color color, ArrayList<BufferedImage> images) {
            int externalX = internalToExternalIndex(x);
            int externalY = internalToExternalIndex(y);

            Rectangle spot = new Rectangle(externalX, externalY, spotSize, spotSize);
            g2D.setPaint(color);

            
            g2D.draw(spot);
            g2D.fill(spot);

            if(images != null){
                for(BufferedImage image : images){
                    g2D.drawImage(image, externalX, externalY, spotSize, spotSize, this);
                }
            }
        }

        /**
         * Converts a logical (internal) coordinate value from the board to a 
         * physical (external) coordinate value on the screen.
         * @param n The coordinate value (x or y) from the internal representation.
         * @return The external mapping of n.
         */
        private int internalToExternalIndex(int n){
            return marginSize + n*(spotSize+marginSize);
        }

        /**
         * Converts a physical (external) coordinate on the screen to a logical
         * (internal) coordinate value from the board.
         * 
         * @param coord The external coordinate on the screen.
         * @return The internal mapping of the coordinate.
         */
        private BoardCoordinate externalToInternalIndex(BoardCoordinate coord){
            return new BoardCoordinate(
                (coord.x - marginSize)/(spotSize+marginSize),
                (coord.y - marginSize)/(spotSize+marginSize));
        }

        /**
         * Continues to run the next driver step and update the display until
         * step returns false. 
         */
        @Override
        public void run() {
            boolean keepGoing = true;
            long beforeTime, timeDiff, sleep;
            beforeTime = System.currentTimeMillis();

            while (keepGoing) {
                keepGoing = driver.step();
                repaint();

                timeDiff = System.currentTimeMillis()-beforeTime;
                sleep = delay - timeDiff;

                if (sleep < 0) {
                    sleep = 2;
                }

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    
                    String msg = String.format("Thread interrupted: %s", e.getMessage());
                    
                    JOptionPane.showMessageDialog(this, msg, "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }

                beforeTime = System.currentTimeMillis();
            }
        }
    }

}
