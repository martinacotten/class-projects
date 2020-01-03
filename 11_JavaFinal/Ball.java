/*
 * @authors Marty Cotten, Taylor Dennison, Sarah Irons, Michael Lapa
 * ISTE-121-01 
 * Professor Floeser 
 * Final Project - Hungry Hippo 
 * @version 12/11/2019
 */

import java.util.*;
import java.awt.*;
import java.io.*;

// class Ball - produces and moves the balls across on the game board
public class Ball implements Serializable {
   
   // attributes 
   private int xPosition = 250;
   private int yPosition = 250;
   private final int YMAX = 500;
   private final int YMIN = 0;
   private final int XMAX = 500;
   private final int XMIN = 0;
   private int xSlope;
   private int ySlope;
   private Point location;
   
   // constructor 
   public Ball() {
   
      //create random x&y slope
      xSlope = getRandomInt(-10,10);
      ySlope = getRandomInt(-10,10);
      
      new BallThread(xSlope,ySlope).start(); // calls & starts thread
   }
   
//    public static void main(String[] args) {
//       new Ball();
//    }

   // a method to get ball's current X position 
   public int getXPosition() { 
      return xPosition;
   }
   
   // a method to get ball's current Y position
   public int getYPosition() { 
      return yPosition;
   }
   
   // a method to return both X and Y positions
   public String getDataString() {
      return xPosition + " " + yPosition;
   }
   
   // method that randomizes the given int value 
   public static int getRandomInt(double min, double max){
      
      int num;
      double x = (Math.random()*((max-min)+1))+min;
      
      //cast to int
      
      num = (int)x;
      
      //ensure not 0
      
      if (num != 0) {
         return num;
      }else {
         return num + 3; 
      }      
   } //end getRandom Int
   
   // class BallThread
   class BallThread extends Thread {
      
      // attributes
      private int xSlope;
      private int ySlope;
      
      // constructor
      public BallThread(int xSlope, int ySlope) {
         this.xSlope = xSlope;
         this.ySlope = ySlope;
      }
      
      // thread run method
      public void run() {
         
         //location = new Point();
         
         // ball always moving
         while (true) {
            
            // increment along slope
            xPosition = xPosition + xSlope;
            yPosition = yPosition + ySlope;
         
            // if statements to keep the balls within the window boundaries.
            if ( xPosition > XMAX) {
               xPosition = XMAX;
               // invert Slope
               xSlope = -xSlope;
            }
            if ( xPosition < XMIN ) {
               xPosition = XMIN;
               // invert Slope
               xSlope = -xSlope;
            }
            if ( yPosition > YMAX) {
               yPosition = YMAX;
               // invert Slope
               ySlope = -ySlope;
            }
            if ( yPosition < YMIN) {               
               yPosition = YMIN;
               // invert Slope
               ySlope = -ySlope;
            }
            
            // if statements for blue hippo boundaries
            
            // left boundary
            if ((xSlope > 0 && (xPosition >= 200 && xPosition <= 210)) && (yPosition >= 375 && yPosition < 500)) {
               xSlope = -xSlope;
            }
            
            //right boundary
            if ((xSlope < 0 && (xPosition <= 300 && xPosition >= 290)) && (yPosition >= 375 && yPosition < 500)) {
               xSlope = -xSlope;
            }
            
            //top boundary
            if ((xPosition >= 200 && xPosition <= 300) && (ySlope > 0 && (yPosition >= 375 && yPosition <= 385))) {
               ySlope = -ySlope;
            }
            
            // if statements for green hippo boundaries
            
            // left boundary
            if ((xSlope > 0 && (xPosition >= 200 && xPosition <= 210)) && (yPosition >= 0 && yPosition < 125)) {
               xSlope = -xSlope;
            }
            
            // right boundary
            if ((xSlope < 0 && (xPosition <= 300 && xPosition >= 290)) && (yPosition >= 0 && yPosition < 125)) {
               xSlope = -xSlope;
            }
            
            // bottom boundary
            if ((xPosition >= 200 && xPosition <= 300) && (ySlope < 0 && (yPosition <= 125 && yPosition >= 115))) {
               ySlope = -ySlope;
            } 
            
            // if statements for Red boundaries
            
            // top boundary
            if ((ySlope > 0 && (yPosition >= 195 && yPosition <= 205)) && (xPosition >= 0 && xPosition <= 125)) {
               ySlope = -ySlope;
            }
            
            // bottom boundary
            if ((ySlope < 0 && (yPosition >= 290 && yPosition <= 300)) && (xPosition >= 0 && xPosition <= 125)) {
               ySlope = -ySlope;
            }
            
            // right boundary
            if ((xSlope < 0 && (xPosition <= 125 && xPosition >= 115)) && (yPosition >= 200 && yPosition <= 300)) {
               xSlope = -xSlope;
            }
            
            // if statements for Yellow boundaries
            
            // top boundary
            if ((ySlope > 0 && (yPosition >= 195 && yPosition <= 205)) && (xPosition >= 375 && xPosition <= 500)) {
               ySlope = -ySlope;
            }
            
            // bottom boundary
            if ((ySlope < 0 && (yPosition >= 290 && yPosition <= 300)) && (xPosition >= 375 && xPosition <= 500)) {
               ySlope = -ySlope;
            }
            
            // right boundary
            if ((xSlope > 0 && (xPosition <= 385 && xPosition >= 375)) && (yPosition >= 200 && yPosition <= 300)) {
               xSlope = -xSlope;
            }
         
            //location.setLocation(xPosition, yPosition);
            
            // Let the thread sleep for a small time.  Helps control speed of balls and has InterruptedException 
            try {
               Thread.sleep(45); 
            }catch(InterruptedException ie){
               ie.printStackTrace();
            }
         }// end while
      }// end run
   } // end BallThread class
}// end of class Ball