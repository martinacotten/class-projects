 /*
 * @authors Marty Cotten, Taylor Dennison, Sarah Irons, Michael Lapa
 * ISTE-121-01 
 * Professor Floeser 
 * Final Project - Hungry Hippo 
 * @version 12/11/2019
 */
 
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.JApplet;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

// general GUI 
public class HungryHippoChatClient extends JFrame implements ActionListener, HippoConstants {
   
   // JFrame GUI components
   private JPanel jpCenterCanvas,
                  jpSouthScoreButton,
                  jpEastChatPanel;
   private Canvas canvas;
   // JTextArea    
   private JTextArea jtaChatArea, 
                     jtaMessageArea,
                     jtaScoreArea;
                     
   // JButton, JMenu stuff
   private JButton jbSend;
   private JMenuBar mBar;
   private JMenu mFile;
   private JMenuItem iExit;
   private JMenu mHelp;
   private JMenuItem iInfo;
   
   // IO components
   private String username;
   private PrintWriter pw;
   private BufferedReader br;
   private Socket s;
   
   private Vector<Ball> ballsVector = new Vector<Ball>();
   private Vector<Ball> caught = new Vector<Ball>();
   private Object lock = new Object();
   private boolean canModify = true;
   private boolean activeGame = false;
   
   private boolean isBlue = false;
   private boolean isGreen = false;
   private boolean isYellow = false;
   private boolean isRed = false;
   private String color;
   
   private BallFrame bf;
   
   private Polygon hippo1Head;
   private Polygon hippo2Head;
   private Polygon hippo3Head;
   private Polygon hippo4Head;
   
   // set up components for GUI window
   
   // main method
   public static void main(String [] args){
      String host = "";
      if (args.length == 1) {
         host = args[0];
      } else {
         System.out.println("You must specify a host when you start the client");
         System.exit(0);
      }
      
      // prompt
      boolean notValid=true;
      String enterUserName="";
      Scanner scan = new Scanner(System.in);
      
      // validates the entered input for username
      do{
         System.out.print("Enter username: ");
         enterUserName = scan.nextLine();
         
         if( "".equals(enterUserName) ){
            System.out.println("You must enter your username to log in");
         }
         else {
            notValid = false;
            System.out.println("Logging in...");
         }
      } while(notValid);
       
      // starts GUI after input validated
      try{
         new HungryHippoChatClient(enterUserName, host);
      }catch(Exception e){
         e.printStackTrace();
      }
   } // end main
     
   // constructor
   public HungryHippoChatClient(String username, String _host){
      this.username = username;
      String host = _host;
      boolean isOffline = true;
      boolean isRunning = true;
      int counter = 1; 
      
      // validates connection to server
      while(isOffline){
         try{
            s  = new Socket(host,16789);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            
            System.out.println(br.readLine());
            pw.println(username);  // sends the username to server
            pw.flush();
            
            color = br.readLine();
            if (color.contains("blue")) {
               isBlue = true;
            } else if (color.contains("green")) {
               isGreen = true;
            } else if (color.contains("red")) {
               isRed = true;
            } else if (color.contains("yellow")) {
               isYellow = true;
            } else {
               System.out.println("Error determining color...");
            }
            
            GUI();
            new MsgThread().start();  // creates the thread to listen for any messages
            isOffline=false; // the connection is validated
         
         } catch(ConnectException ce){
            System.out.println("The server is not live. Attempt #"+counter+"...");
            counter++;
         } catch(UnknownHostException uhe){
            uhe.printStackTrace();
         } catch(IOException ioe){
            ioe.printStackTrace();
         }
      } // end while isOffline loop
      
      // records the # of attempts to connect the server while the server is not live
      if(counter!=1){
         System.out.println("Successful connection. The server is live. It took attempt #"+counter+" to connect the server.");
      }
      
      // checks and listens to the current connection between client and server
      while(isRunning){
      
         // if the connection is lost, the client will get user friendly pop up and close its socket
         if(pw.checkError())
         {
            JOptionPane.showMessageDialog(null,"The server is no longer live. You cannot continue communicating. Please restart and wait to reconnect the server.","Connection Lost",JOptionPane.WARNING_MESSAGE);
            try{
               s.close();
               jbSend.setEnabled(false);
               
            }
            catch(IOException ioe){
              ioe.printStackTrace();
            }
            isRunning=false;
         }
      }
   
   } // end constructor
   
   // GUI constructor
   public void GUI(){
   
      setTitle("Hungry Hungry Hippo ("+username+")");
      
      // creates the menu bar
      mBar = new JMenuBar();
      setJMenuBar(mBar);
      
      // creates File Menu
      mFile = new JMenu("File");
      mFile.setMnemonic('F');
      
      // creates menu item for File Menu
      iExit = new JMenuItem("Exit");
      iExit.setMnemonic('E');
      
      //creates Help Menu
      
      mHelp = new JMenu("Help");
      mHelp.setMnemonic('H');
      
      //creates menu item for Help Menu
      
      iInfo = new JMenuItem("Info");
      iInfo.setMnemonic('I');
      
      
      // builds menu bar, menus, and menu items 
      mBar.add(mFile);
      mBar.add(mHelp);
      mFile.add(iExit);
      mHelp.add(iInfo);
      
      // add actionlisteners to menu items
      iExit.addActionListener(this);
      
      //readMeText 
      String readMeText = "Thanks for playing Hungry Hungry Hippos!  Heres how the game works: \n" +
                           "There are 4 players in the game, no more, no less. After the fourth player\n"+
                           "connects to the server, the game will begin.  There are 50 Balls that will be\n"+
                           "randomly moving in any direction, bouncing off walls and the hippos bodies.\n"+  
                           "Each player will be controlling a hippo, the color of which will be given to\n"+
                           "each client as they connect.  By clicking the game area, the perspective hippo\n"+ 
                           "in that client will open and close its mouth.  If any ball is caught in the mouth\n"+ 
                           "of a hippo when the player clicks, the ball will be eaten, and a point will be\n"+ 
                           "awarded to that player/hippo.  Scores are represented at the bottom of the window.\n"+
                           "The game is over when all balls have been eaten.  Whoever has the highest score at\n"+ 
                           "the end of the game wins!\n\n"+
         
                           "There is also a chat interface on the right side of the display.  The top portion is\n"+
                           "the message area and the bottom portion is where a player can type and send a message.\n"+
                           "These messages are public and all players will see messages and responses.\n\n"+
                           
                           "To restart the game, disconnect and reconnect all clients. ";
                            
      iInfo.addActionListener( (e) -> JOptionPane.showMessageDialog( jpCenterCanvas , readMeText ) );
      
      // window listener for X icon
      addWindowListener(
         new WindowAdapter()
         {
            public void windowClosing(WindowEvent we)
            {
               pw.println("!END!");
               pw.flush();
               System.exit(0);
            }
         });
      
      this.setLayout( new BorderLayout() ); // sets the GUI with this style of layout
      
      // declares some panels
      jpEastChatPanel = new JPanel( new GridLayout(0,1) );
      jpCenterCanvas = new JPanel( new FlowLayout() );
      jpSouthScoreButton = new JPanel( new FlowLayout() );
      
      // East Panel with chat window and message text area
      jtaChatArea = new JTextArea(16, 25);
      jtaChatArea.setBorder( BorderFactory.createLineBorder(Color.BLACK, 1) );
      jtaChatArea.setEditable(false);
      JScrollPane jspScrollBar = new JScrollPane(jtaChatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jtaMessageArea = new JTextArea(16, 15);
      jtaMessageArea.setBorder( BorderFactory.createLineBorder(Color.BLACK, 1) );
      jpEastChatPanel.add( jtaChatArea );
      jpEastChatPanel.add( jtaMessageArea );
      
      
      add(jpEastChatPanel, "East");
      
      // South Panel with score panel and send button
      jtaScoreArea = new JTextArea("", 5,60);
      jtaScoreArea.setBorder( BorderFactory.createLineBorder(Color.BLACK, 1) );
      jtaScoreArea.setEditable(false);
      jbSend = new JButton("Send");
      jbSend.addActionListener(this);
      jpSouthScoreButton.add(jtaScoreArea);
      jpSouthScoreButton.add(jbSend);
      
      add(jpSouthScoreButton, "South");
      
      jpEastChatPanel.getRootPane().setDefaultButton(jbSend); // allows the send button to be used with ENTER key (this doesn't work????)
      
      //Center Panel with canvas
      // canvas = new Canvas();
   //       canvas.setSize(700,700);
   //       canvas.setBackground(Color.WHITE);
   // 
   //       jpCenterCanvas.add( canvas );
   //       add(jpCenterCanvas, "Center");
      
      // adds the game to GUI and visualizes
      bf= new BallFrame();
            
      bf.setSize(700,700);
      bf.setVisible(true);
      jpCenterCanvas.add(bf);
      add(bf, "Center");
      
      // adds mouse listener for mouse clicking on hippo's head
      bf.addMouseListener(
         new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
               if (activeGame) {
                  try {
                     // System.out.println("the mouse was clicked");
                  
                  // sends the command that an object will be sent
                     pw.println("!BALL!");
                     pw.flush();
                     
                     // go through the balls and see if any intersect the hippo heads
                     if (canModify) {
                        canModify = false;
                        for (Ball ball : ballsVector) {
                           if (isBlue && hippo1Head.intersects(new Rectangle2D.Double(ball.getXPosition(), ball.getYPosition(), 8, 8))) {
                              caught.add(ball);
                           } else if (isGreen && hippo2Head.intersects(new Rectangle2D.Double(ball.getXPosition(), ball.getYPosition(), 8, 8))) {
                              caught.add(ball);
                           } else if (isRed && hippo3Head.intersects(new Rectangle2D.Double(ball.getXPosition(), ball.getYPosition(), 8, 8))) {
                              caught.add(ball);
                           } else if (isYellow && hippo4Head.intersects(new Rectangle2D.Double(ball.getXPosition(), ball.getYPosition(), 8, 8))) {
                              caught.add(ball);
                           }
                        }
                     }
                     canModify = true;
                     
                     pw.println(caught.size());
                     pw.flush();
                  
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } else {
                  pw.println("!CHAT!");
                  pw.flush();
                  
                  pw.println("No round is active, restart the server if you want to play again or wait for another round to start");
                  pw.flush();
               }
            }
         
         }); // end mouse listener
               
      // creates and starts new thread
      new Thread( bf ).start();
      
      // builds the GUI 
      pack();
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      setVisible(true);
   }// end GUI constructor
   
   
   // actionPerformed method
   public void actionPerformed(ActionEvent ae){
      // exit button
      if ( ae.getSource() == iExit ) {
         pw.println("!END!");
         pw.flush();
         System.exit(0);
         
      // send button
      } else if (ae.getSource() == jbSend ) {
         pw.println("!CHAT!");
         pw.flush();
         
         pw.println( jtaMessageArea.getText() );
         pw.flush();
         jtaMessageArea.setText("");
      }
   
   } //end actionPerformed method
   
   // inner class for Messages Thread
   class  MsgThread extends Thread {
      public void run() {
         try {
            // append the text color to the chat
            jtaChatArea.append(color + "\n");
         
            while(true) {
               String msg = br.readLine();
               
               if (msg.equals("!CHAT!")) {
                  // add whatever text is sent from the server into the chat window
                  String line = br.readLine();
                  line = br.readLine();
                  line = br.readLine();
                  jtaChatArea.append(line + "\n"); 
               }
               else if (msg.equals("!START!")) {
                  try {
                     // create the balls and add them to the vector, then allow them to start
                     canModify = false;
                     for (int i = 1; i <= 30; i++) {
                        ballsVector.add(new Ball());
                     }
                     sleep(5000);
                     canModify = true;
                     activeGame = true;
                  } catch (InterruptedException ie) {
                     // ahhh
                  } catch (ConcurrentModificationException cme) {
                     // please
                  }
               }
               else if (msg.equals("!FINISH!")) {
                  // the end of the game has been reached, stop the balls and remove them from the vector
                  activeGame = false;
                  canModify = false;
                  ballsVector.removeAllElements();
                  sleep(5000);
               } else if (msg.equals("!SCORE!")) {
                  jtaScoreArea.setText("");
                  String score;
                  while (!(score = br.readLine()).equals("!ALL!")) {
                     jtaScoreArea.append(score + "\n");
                  }
               } else if (msg.equals("!WINNER!")) {
                  String winner = br.readLine();
                  winner = br.readLine();
                  winner = br.readLine();
                  jtaScoreArea.append(winner);
               }
            }
         }catch(IOException ioe){
            ioe.printStackTrace();
         } catch (ConcurrentModificationException cme) {
            // acknowledged, this will not affect functionality
         } catch (InterruptedException ie) {
            // well
         }
      }
   } // end inner class
   
   // another inner class to handle the painting of the hippos and balls
   class BallFrame extends JPanel implements Runnable {
   
      public void run() {
        
         while (true) {
         
            repaint();
            
         }//end while
      }
   
      // method to call and paint with given Ball class
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         // iterates through the caught balls vector to find the ones to remove from all of the balls
         if (canModify && activeGame) {
            Vector<Ball> toRemove = new Vector<Ball>();
            for (Ball ball : ballsVector) {
               for (Ball cBall : caught) {
                  if (cBall == ball) {
                     toRemove.add(ball);
                  }
               }
            }
            
            ballsVector.removeAll(toRemove);
            
            // draws the balls from the given vector in-game
            for (Ball ball : ballsVector) {
               g.drawRect(ball.getXPosition(),ball.getYPosition(), 8, 8);
            }
         }
         
         // Blue Hippo Body
         Polygon hippo1 = new Polygon();
         hippo1Head = new Polygon();
      
         // body points   
         hippo1.addPoint(200,500); 
         hippo1.addPoint(300,500);  
         hippo1.addPoint(300,375);
         hippo1.addPoint(200,375);
      
         // head points
         hippo1Head.addPoint(200,373); 
         hippo1Head.addPoint(300,373);  
         hippo1Head.addPoint(275,325);
         hippo1Head.addPoint(225,325);
      
         // sets color, fills, and draws
         g.setColor( Color.BLUE );
         g.fillPolygon(hippo1);
         g.drawPolygon(hippo1);
      
         g.setColor( Color.BLUE );
         g.fillPolygon(hippo1Head);
         g.drawPolygon(hippo1Head);
      
         // Green Hippo Body
         Polygon hippo2 = new Polygon();
         hippo2Head = new Polygon();
      
         // adds points
         hippo2.addPoint(200,0); 
         hippo2.addPoint(300,0);  
         hippo2.addPoint(300,125);
         hippo2.addPoint(200,125);
      
         // head points
         hippo2Head.addPoint(200,127); 
         hippo2Head.addPoint(300,127);  
         hippo2Head.addPoint(275,175);
         hippo2Head.addPoint(225,175);
      
         // sets color, fills, and draws
         g.setColor( Color.green );
         g.fillPolygon(hippo2);
         g.drawPolygon(hippo2);
      
         g.setColor( Color.green );
         g.fillPolygon(hippo2Head);
         g.drawPolygon(hippo2Head);
         
         // Red Hippo Body
         Polygon hippo3 = new Polygon();
         hippo3Head = new Polygon();
      
         // add points
         hippo3.addPoint(0,200); 
         hippo3.addPoint(0,300);  
         hippo3.addPoint(125,300);
         hippo3.addPoint(125,200);
      
         // add head
         hippo3Head.addPoint(127,200); 
         hippo3Head.addPoint(127,300);  
         hippo3Head.addPoint(175,275);
         hippo3Head.addPoint(175,225);
      
         // set color, fill, and draw
         g.setColor( Color.red );
         g.fillPolygon(hippo3);
         g.drawPolygon(hippo3);
      
         g.setColor( Color.red );
         g.fillPolygon(hippo3Head);
         g.drawPolygon(hippo3Head);
      
         // Yellow Hippo Body
         Polygon hippo4 = new Polygon();
         hippo4Head = new Polygon();
      
         // adds points
         hippo4.addPoint(500,200); 
         hippo4.addPoint(500,300);  
         hippo4.addPoint(375,300);
         hippo4.addPoint(375,200);
      
         // adds head
         hippo4Head.addPoint(373,200); 
         hippo4Head.addPoint(373,300);  
         hippo4Head.addPoint(325,275);
         hippo4Head.addPoint(325,225);
      
         // sets color, fills, and draws
         g.setColor( Color.yellow );
         g.fillPolygon(hippo4);
         g.drawPolygon(hippo4);
         
         g.setColor( Color.yellow );
         g.fillPolygon(hippo4Head);
         g.drawPolygon(hippo4Head);
      } // end paintComponent
   } // end class BallFrame   
} // end HungryHippo