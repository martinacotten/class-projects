import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

public class Client {

   private Socket cs;
   private String user;
   private String hippoColor;
   private int score;

   public Client(Socket _cs, String _user, String _hippoColor, int _score) {
      cs = _cs;
      user = _user;
      hippoColor = _hippoColor;
      score = _score;
   }
   
   public int getScore() {
      return score;
   }
   
   public String getUser() {
      return user;
   }
   
   public void setScore(int newScore) {
      score = newScore;
   }
   
   public String toString() {
      return user + " is the " + hippoColor + " hippo and is connected through socket: " + cs;
   }

}