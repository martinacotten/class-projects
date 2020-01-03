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
import java.awt.*;
import javax.swing.*;

// server class
public class HungryHippoServer implements HippoConstants {
   
   // attributes
   private Object lock = new Object();
   private Vector<PrintWriter> pwAll = new Vector<PrintWriter>();
   private Vector<Client> clients = new Vector<Client>();
   
   private boolean isBlue = false;
   private boolean isGreen = false;
   private boolean isRed = false;
   private boolean isYellow = false;
   private int clientCount = 0;
   private int totalCaught = 0;
   
   private JTextArea jtaClients;
   private JTextArea jtaText;
   
   private Scanner in;
   private int numPlayers = 0;
   private int numRounds = 1;
   private int timePerRound = 30;
   private boolean isRound = false;

   // main
   public static void main(String[] args) {
      new HungryHippoServer();
   } // end main
   
   // constructor
   public HungryHippoServer() {
      
      // gather input to determine players, values, and rounds
      in = new Scanner(System.in);
      do {
         System.out.print("How many players (1-4)?: ");
         numPlayers = in.nextInt();
         //System.out.print("How many rounds?: ");
         //numRounds = in.nextInt();
         //System.out.print("How many seconds would you like each round to be?: ");
         //timePerRound = in.nextInt();
      } while(numPlayers > 4 || numPlayers < 1);
      
      timePerRound *= 1000;
      
      // launch GUI
      new ServerWindow();
      // server connection set-up
      try {
         ServerSocket ss = new ServerSocket(PORT);
         Socket cs;
         
         // condition of success connection
         while(true) {
            jtaText.append("Waiting for a client connection\n\n");
            cs = ss.accept();
            jtaText.append("Have a client connection: " + cs + "\n\n");
            
            // creates and starts the server thread
            new HippoServerThread(cs).start();
         }
      // IOException 
      } catch (IOException ioe) {
         //ioe.printStackTrace();
      }
   } // end constructor
   
   // class HippoServerThread 
   class HippoServerThread extends Thread {
      
      // attributes
      private Socket cs;
      private String color;
      private Client client;
      
      private PrintWriter pw;
      private BufferedReader br;
      
      private String currMessage;
      
      private int threadCaught;
      
      // thread constructor
      public HippoServerThread(Socket _cs) {
         cs = _cs;
      }
      
      // thread run method
      public void run() {
      
         try {
            // creates and opens the BufferedReader and PrintWriter
            pw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            
            // adds the PrintWriter to the vector
            pwAll.add(pw);
            
            // sends the welcome message
            jtaText.append("Sending a welcome message to client\n\n");
            pw.println("Welcome to the server");
            pw.flush();
            
            // stores the client's chosen username
            String user = br.readLine();
            
            // tests what color hippo the client will be, makes sure there are < 4 clients
            if (clientCount >= 4 || clientCount >= numPlayers || (isBlue && isGreen && isRed && isYellow)) {
               // full number of players
               jtaText.append("All clients are already connected, wait until 1 disconnects\n\n");
               cs.close();
            } else if (!isBlue) {
               // given the position to one of the clients
               isBlue = true;
               color = "Blue";
               jtaText.append(user + " connected as the blue Hippo!\n\n");
               pw.println(user + " you are the blue hippo!");
               pw.flush();
               clientCount++;
               
               // create a new client and add it to the vector
               client = new Client(cs, user, color, 0);
               clients.add(client);
            } else if (!isGreen) {
               // given the position to one of the clients
               isGreen = true;
               color = "Green";
               jtaText.append(user + " connected as the green Hippo!\n\n");
               pw.println(user + " you are the green hippo!");
               pw.flush();
               clientCount++;
               
               // create a new client and add it to the vector
               client = new Client(cs, user, color, 0);
               clients.add(client); 
            } else if (!isRed) {
               // given the position to one of the clients
               isRed = true;
               color = "Red";
               jtaText.append(user + " connected as the red Hippo!\n\n");
               pw.println(user + " you are the red hippo!");
               pw.flush();
               clientCount++;
               
               // create a new client and add it to the vector
               client = new Client(cs, user, color, 0);
               clients.add(client);
            } else if (!isYellow) {
               // given the position to one of the clients
               isYellow = true;
               color = "Yellow";
               jtaText.append(user + " connected as the yellow Hippo!\n\n");
               pw.println(user + " you are the yellow hippo!");
               pw.flush();
               clientCount++;
               
               // create a new client and add it to the vector
               client = new Client(cs, user, color, 0);
               clients.add(client);
            } else {
               // error when something doesn't work while the clients connecting to the server
               jtaText.append("There has been an error, please restart all clients and the server\n\n");
               cs.close();
            }
            
            // once all clients are connected, create the ball vector and start the balls (print using the common writers)
            while(clientCount <= numPlayers - 1) {
               jtaText.append("Waiting for all clients to connect...\n\n");
               sleep(5000);
            }
            
            // add functionality to support a specified number of rounds ?
            for (int i = 0; i < numRounds; i++) {
               // allow a round to happen            
               isRound = true;
               
               // let the client know it is time to start the balls and start the timer for the round
               pw.println("!START!");
               pw.flush();
               
               jtaText.append("Starting the timer...\n\n");
               long start = System.currentTimeMillis();
               long end = start + timePerRound;
               
               // while loop to wait for input from clients
               while(isRound) {
                  // update the clients text area
                  jtaClients.setText(null);
                  for (Client client : clients) {
                     jtaClients.append(client.toString() + "\n");
                  }
                  
                  // section to test if the time >= round time, end the round then
                  if (System.currentTimeMillis() >= end) {
                     // send command to the clients to stop the balls
                     for (PrintWriter pw : pwAll) {
                        pw.println("!FINISH!");
                        pw.flush();
                     }
                     
                     // change a boolean to keep the while loop running
                     isRound = false;
                  }
                  
                  // reads input from clients
                  currMessage = br.readLine();
                  
                  if (currMessage.equals("!END!")) {
                     // remove the client from the vector (idk how rn lol)
                     
                     for (PrintWriter pw : pwAll) {
                        pw.println("!CHAT!");
                        pw.flush();
                        
                        pw.println(user + ": has left the chat");
                        pw.flush();
                     }
                     jtaText.append(user + ": has left the chat\n\n");
                     clients.remove(client);
                     
                     // makes the hippo available again and reduces the counter
                     clientCount--;
                     switch(color) {
                        case "Blue":
                           isBlue = false;
                        case "Green":
                           isGreen = false;
                        case "Red":
                           isRed = false;
                        case "Yellow":
                           isYellow = false;
                     }
                  } else if (currMessage.equals("!BALL!")) {
                     // if the ball message is received, receive the # of balls caught
                     String score = br.readLine();
                     threadCaught = Integer.parseInt(score);
                     client.setScore(threadCaught);
                     
                     // update the score for all of the threads
                     for (PrintWriter pw : pwAll) {
                        pw.println("!SCORE!");
                        pw.flush();
                        for (Client client : clients) {
                           pw.println(client.getUser() + ": " + client.getScore());
                           pw.flush();
                        }
                        pw.println("!ALL!");
                        pw.flush();
                     }
                     
                  } else {            
                     // for each pw in vector pw the message to the clients
                     for (PrintWriter pw : pwAll) {
                        pw.println("!CHAT!");
                        
                        pw.println(user + ": " + currMessage);
                        pw.flush();
                     }
                     jtaText.append(user + ": " + currMessage+"\n\n");
                  }
               } // end while loop
               
            } // end for loop (for the number of rounds)
            
            // once the rounds are over, get the client with the most balls caught
            int winningScore = 0;
            Client winningClient = new Client(cs, "ERROR", "NO HIPPO", 0);
            
            for (int i = 0; i < clients.size(); i++) {
               for (int j = i + 1; j < clients.size() - 1; j++) {
                  if (clients.get(i).getScore() > clients.get(j).getScore()) {
                     winningScore = clients.get(i).getScore();
                     winningClient = clients.get(i);
                  } else if (clients.get(i).getScore() < clients.get(j).getScore()) {
                     winningScore = clients.get(j).getScore();
                     winningClient = clients.get(j);
                  } else {
                     System.out.println("There is a tie!");
                  }
               }
            }
            
            // announce the winning client to each of the clients
            for (PrintWriter pw : pwAll) {
               pw.println("!WINNER!");
               pw.flush();
               
               pw.println("The winner is: " + winningClient.getUser() + " with " + winningClient.getScore() + " balls caught!");
               pw.flush();
            }
            
            br.close();
            pw.close();
            cs.close();
            
         } catch (IOException ioe) {
            jtaText.append("The client connection has been lost");
         } catch (InterruptedException ie) {
            // acknowledged
         }
      
      } // end run method
      
   } // end hipposerverthread class
   
   // GUI Server 
   class ServerWindow extends JFrame {
      
      // constructor of server window
      public ServerWindow() {
         
         // title
         super("Hungry Hungry Hippos Server");
         
         setSize(500,500);
         setLocation(700,200);
         
         // panel
         JPanel jpAll = new JPanel(new GridLayout(0,1));
         
         JPanel jpClients = new JPanel();
         jtaClients = new JTextArea(15,45);
         jtaClients.setEnabled(false);
         JScrollPane jsp = new JScrollPane( jtaClients );
         jpClients.add(jsp);
         jpAll.add(jpClients);
         
         JPanel jpText = new JPanel();
         jtaText = new JTextArea(15,45);
         jtaText.setEnabled(false);
         JScrollPane jsp2 = new JScrollPane(jtaText);
         jpText.add(jsp2);
         jpAll.add(jpText);
         
         add(jpAll);
            
         pack();
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
      } // end ServerWindow constructor
   } // end ServerWindow class
} // end hungryhipposerver class