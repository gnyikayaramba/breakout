/*
* File: Breakout.java
* -------------------
* This file will eventually implement the game of Breakout.
*/
import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.WindowConstants;

public class Breakout extends GraphicsProgram {
/** Width and height of application window in pixels */
public static final int APPLICATION_WIDTH = 600;
public static final int APPLICATION_HEIGHT = 600;
/** Dimensions of game board (usually the same) */
private static final int WIDTH = APPLICATION_WIDTH;
private static final int HEIGHT = APPLICATION_HEIGHT;
/** Dimensions of the paddle */
private static final int PADDLE_WIDTH = 60;
private static final int PADDLE_HEIGHT = 10;
/** Offset of the paddle up from the bottom */
private static final int PADDLE_Y_OFFSET = 30;
/** Number of bricks per row */
private static final int NBRICKS_PER_ROW = 10;
/** Number of rows of bricks */
private static final int NBRICK_ROWS = 10;
/** Separation between bricks */
private static final int BRICK_SEP = 4;
/** Width of a brick */
private static final int BRICK_WIDTH =
(WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;
/** Height of a brick */
private static final int BRICK_HEIGHT = 10;
/** Radius of the ball in pixels */
private static final int BALL_RADIUS = 10;
/** Offset of the top brick row from the top */
private static final int BRICK_Y_OFFSET = 70;
/** Number of turns */
private static final int NTURNS = 3;
private static final int DELAY = 50;

// Private Instance variables
private GRect paddle; //keeps track of the paddle
private GOval ball;   // keeps track of the ball
private GLabel score; // keeps track of user's score
private RandomGenerator rgen = RandomGenerator.getInstance(); //random number generator for ball initialization
// clip to play a bouncing sound when ball bounces, used a conversion sites output link, LINK MIGHT EXPIRE!!
private AudioClip bounceClip = MediaTools.loadAudioClip("https://www.sounddogs.com/previews/101/mp3/131579_SOUNDDOGS__ba.mp3");

private double vx, vy; // velocity in the x and y directions respectively
private int points; // User's points
private int speed_counter; // Used to update the speed of game play
private int lives = NTURNS; // Keeps track of the number of lives the user is left with
private boolean win; // True if winner has won, i.e. demolished all bricks


public void run() {
	// Initialize the game
	gameInit();
	
	// Mouse event listeners for mouse tracking
	addMouseListeners();
	
	// Game play
	while(lives>0&&!win){
	play();
	}
}

private void gameInit(){
	setSize(WIDTH, HEIGHT); // set window size
	buildBricks();
	drawPaddle();
	initBall();
	score = new GLabel("Score: " + 0 + " Lives: " + lives , WIDTH/2, 15); //initialize scoreboard
	score.setVisible(true);
	add(score);
	points = 0;
	speed_counter =8;
}

//Maintains game play by sequentially calling to the game's various components
private void play(){
	updateSpeed();
	moveBall();
	keepBallBounded();
	updateLives();
	GObject collider = getCollidingObject();
	processCollision(collider);
	checkWin();
	pause(DELAY);
}

// Builds the bricks that are targets in the game
private void buildBricks(){
	
	int x_start = (WIDTH-(NBRICKS_PER_ROW*(BRICK_WIDTH + BRICK_SEP)-BRICK_SEP))/2;
	GRect brick;
	Color myColor = Color.WHITE;
	
	int x;
	int y;
	
	for(int i = 0; i< NBRICK_ROWS; i++){
		y = BRICK_Y_OFFSET + i*(BRICK_HEIGHT+BRICK_SEP);
		
		//Change brick colors by line
		if(i<2) myColor = Color.RED;
		if(1<i&&i<4) myColor = Color.ORANGE;
		if(3<i&&i<6) myColor =Color.YELLOW;
		if(5<i&&i<8) myColor = Color.GREEN;
		if(7<i&&i<10) myColor = Color.CYAN;
		
		for(int j = 0; j< NBRICKS_PER_ROW; j++){
			
			x = x_start + j*(BRICK_WIDTH+BRICK_SEP);
			
			brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
			brick.setColor(myColor);
			brick.setFilled(true);
			add(brick);	
		}
	}	
}

// Draws the paddle used in the game in its initial centralized position
private void drawPaddle(){
	
		int x_paddle = (WIDTH-PADDLE_WIDTH)/2; //paddle initialization x-coordinate
		int y_paddle = (HEIGHT-PADDLE_Y_OFFSET); //paddle initialization y-coordinate
		paddle = new GRect(x_paddle,y_paddle , PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		add(paddle);	
}

 public void mouseDragged(MouseEvent e){} //Abstract method necessary for implementation of mouse listener interface

 public void mouseMoved(MouseEvent e){
	// Moves the paddle and ensures that it stays within the bounds of the GCanvas
	 if(paddle!=null){
		 if(e.getX()<WIDTH-PADDLE_WIDTH){
		 paddle.move(e.getX()-paddle.getX(), 0);
		 }
	}
}
 
// Create the ball and start moving it
private void initBall(){
	double x_coord = WIDTH/2-BALL_RADIUS;
	double y_coord = HEIGHT/2-BALL_RADIUS; // ball's initial coordinates
	
	ball = new GOval(x_coord, y_coord, 2*BALL_RADIUS, 2*BALL_RADIUS); // Create ball
	ball.setFilled(true);
	ball.setColor(Color.BLUE);
	add(ball);
	
	vy = 3;
	vx = rgen.nextDouble(2.0, 4.0);
	if(rgen.nextBoolean(0.5)) vx=-vx;
}
// Moves the ball
private void moveBall(){	
	ball.move(vx, vy);	
}

// Ensures that the ball doesn't get out of the game frame
private void keepBallBounded(){
	
	if(ball.getX()<2*BALL_RADIUS||ball.getX()>(WIDTH-2*BALL_RADIUS)||ball.getY()<2*BALL_RADIUS){
		if(ball.getX()<2*BALL_RADIUS||ball.getX()>(WIDTH-2*BALL_RADIUS)){
			vx =-vx;
		}else if(ball.getY()<2*BALL_RADIUS){
			vy=-vy;
		}
	}
}

// Retrieves the object that the ball has collided with, if any.
private GObject getCollidingObject(){
	GObject obstacle;
	//try first corner
	obstacle = getElementAt(ball.getX(), ball.getY());
	
	// If there is nothing at first corner, try second corner
	if(obstacle==null){
		obstacle = getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY());
		//If there is nothing at second corner, try third corner
		if(obstacle==null){
			obstacle = getElementAt(ball.getX(), ball.getY()+2*BALL_RADIUS);
			// If there is nothing at third corner, try last corner
			if(obstacle==null){
				obstacle = getElementAt(ball.getX()+2*BALL_RADIUS, ball.getY()+2*BALL_RADIUS);
			}
		}
		
	}
		
return obstacle;
}

// Processes a collision if the ball has collided with something
private void processCollision(GObject collider){
	
	if(collider!=null){
	if(collider==paddle){
		vy = -vy;
		speed_counter--;
	}else if(collider!=score){
		vy = -vy;
		remove(collider);
		points +=1;
		score.setLabel("Score: " + points + " Lives: " + lives);
	}
	bounceClip.play();
	}
}
	
// Updates the number of lives that the player has
private void updateLives(){
	if(ball.getY()>HEIGHT-PADDLE_Y_OFFSET){
		lives--;
		remove(ball);
		score.setLabel("Score: " + points + " Lives: " + lives);
		
	if(lives>0){
		initBall();
		}else{
			add(new GLabel("You Lose :-(", WIDTH/2, HEIGHT/2));
		}
	}
}
 
// Checks to see if player has won
private void checkWin(){
	if(points>=NBRICK_ROWS*NBRICKS_PER_ROW){
		win = true;
		add(new GLabel("You Win!!!", WIDTH/2, HEIGHT/2));
	}
}

private void updateSpeed(){
	if(speed_counter==0){
		vx *= 1.2;
		vy *= 1.2;
		speed_counter=8;
	}
}
}	

