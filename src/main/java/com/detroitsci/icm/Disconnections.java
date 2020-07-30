package com.detroitsci.icm;

import java.awt.Color;

import javax.sound.sampled.Clip;

public class Disconnections {

   // audio file to play when disconnected
   Clip clpDisconnected = null;
   Clip clpConnected = null;

   public synchronized void run() {

      // increase disconnection counter when ONLY one website is being monitored
      if (UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 1 & UserInterface.SAMEDISCONNECTION == (byte) 0) {

         UserInterface.SAMEDISCONNECTION = (byte) 1;
         UserInterface.DISCONNECTIONCOUNTER++;
         UserInterface.LBLDISCONNECTIONCOUNTER.setText(Integer.toString(UserInterface.DISCONNECTIONCOUNTER));
         UserInterface.LBLDISCONNECTIONCOUNTER.setForeground(Color.RED);
         UserInterface.LBLCURRENTSTATE.setText("DOWN");
         UserInterface.LBLCURRENTSTATE.setForeground(Color.RED);

         // play sound when disconnected (when counter goes up by one)
         if (UserInterface.CHKPLAYSOUND.isSelected() == true) {
            if (clpDisconnected != null) {
               clpDisconnected.stop();
               clpDisconnected.setFramePosition(0);
               clpDisconnected.start();
            }
         }
      } else if (UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 0 & UserInterface.SAMEDISCONNECTION == (byte) 1) {

         UserInterface.SAMEDISCONNECTION = (byte) 0;

         UserInterface.LBLCURRENTSTATE.setText("UP");
         UserInterface.LBLCURRENTSTATE.setForeground(UserInterface.DARK_GREEN);

         if (UserInterface.CHKPLAYSOUND.isSelected() == true) {
            if (clpConnected != null) {
               clpConnected.stop();
               clpConnected.setFramePosition(0);
               clpConnected.start();
            }
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

   public void setConnectedClip(Clip clip) {
      clpConnected = clip;
   }

   public void setDisconnectedClip(Clip clip) {
      clpDisconnected = clip;
   }
}
