package com.detroitsci.icm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.sound.sampled.Clip;

public class Disconnections {

   // audio file to play when disconnected
   Clip clpDisconnected = null;
   Clip clpConnected = null;

   URL connectedUrl;
   URL disconnectedUrl;

   public synchronized void run() {

      // increase disconnection counter when ONLY one website is being monitored
      if (UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 1 & UserInterface.SAMEDISCONNECTION == (byte) 0) {

         UserInterface.SAMEDISCONNECTION = (byte) 1;
         UserInterface.DISCONNECTIONCOUNTER++;
         UserInterface.LBLDISCONNECTIONCOUNTER.setText(Integer.toString(UserInterface.DISCONNECTIONCOUNTER));
         UserInterface.LBLDISCONNECTIONCOUNTER.setForeground(Color.RED);
         UserInterface.LBLCURRENTSTATE.setText("DOWN");
         UserInterface.LBLCURRENTSTATE.setForeground(Color.RED);

         // play sound
         if (UserInterface.CHKPLAYSOUND.isSelected() == true && (clpDisconnected != null)) {
            clpDisconnected.stop();
            clpDisconnected.setFramePosition(0);
            clpDisconnected.start();
         }

         if (disconnectedUrl != null) {
            hitUrl(disconnectedUrl);
         }
      } else if (UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 0 & UserInterface.SAMEDISCONNECTION == (byte) 1) {

         UserInterface.SAMEDISCONNECTION = (byte) 0;

         UserInterface.LBLCURRENTSTATE.setText("UP");
         UserInterface.LBLCURRENTSTATE.setForeground(UserInterface.DARK_GREEN);

         if (UserInterface.CHKPLAYSOUND.isSelected() == true && (clpConnected != null)) {
            clpConnected.stop();
            clpConnected.setFramePosition(0);
            clpConnected.start();
         }
         if (connectedUrl != null) {
            hitUrl(connectedUrl);
         }
      } else if (UserInterface.SAMEDISCONNECTION == (byte) 1) {

         if (UserInterface.primaryResumeSuccessesRemaining < UserInterface.successesAfterFailRequired) {
            // in the process of coming back up
            UserInterface.LBLCURRENTSTATE.setText("UP in " + (1 + UserInterface.primaryResumeSuccessesRemaining));
            UserInterface.LBLCURRENTSTATE.setForeground(Color.ORANGE);
         } else {
            UserInterface.LBLCURRENTSTATE.setText("DOWN");
            UserInterface.LBLCURRENTSTATE.setForeground(Color.RED);
         }

         // loop disconnection sound if option is selected.
         if (UserInterface.CHKPLAYSOUNDLOOP.isSelected() == true && clpDisconnected != null
               && (!clpDisconnected.isRunning())) {
            clpDisconnected.stop();
            clpDisconnected.setFramePosition(0);
            clpDisconnected.start();
         }
      }
   }

   // free clip memory resources
   public synchronized void stopAudioFile() {
      if (clpDisconnected != null)
         clpDisconnected.stop();
   }

   private void hitUrl(URL url) {
      new Thread() {
         public void run() {
            BufferedReader reader = null;
            HttpURLConnection connection = null;
            try {
               connection = (HttpURLConnection) url.openConnection();
               connection.setRequestMethod("GET");
               connection.setReadTimeout(15 * 1000);
               connection.connect();
               reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
               while (reader.readLine() != null) {
               }
            } catch (Exception e) {
               UserInterface.DEBUG_OUTPUT.append("Error accessing URL " + url + ": " + e + UserInterface.LSEP);
            } finally {
               try {
                  if (reader != null)
                     reader.close();
                  if (connection != null)
                     connection.disconnect();
               } catch (Exception e) {
               }
            }
         }
      }.start();
   }

   public void setConnectedUrl(URL cUrl) {
      connectedUrl = cUrl;
   }

   public void setDisconnectedUrl(URL dUrl) {
      disconnectedUrl = dUrl;
   }

   public void setConnectedClip(Clip clip) {
      clpConnected = clip;
   }

   public void setDisconnectedClip(Clip clip) {
      clpDisconnected = clip;
   }
}
