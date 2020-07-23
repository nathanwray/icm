package com.detroitsci.icm;

import java.awt.Color;
import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Disconnections {

   // audio file to play when disconnected
   Clip clpDisconnected;

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
            try {
               Clip clpDisconnected = AudioSystem.getClip();
               clpDisconnected
                     .open(AudioSystem.getAudioInputStream(new File("sounds" + File.separator + "goddamnit.wav")));
               FloatControl gain = (FloatControl) clpDisconnected.getControl(FloatControl.Type.MASTER_GAIN);
               gain.setValue(gain.getMaximum());

               clpDisconnected.start();
            } catch (Exception e) {
               e.printStackTrace();
               UserInterface.DEBUG_OUTPUT
                     .append("(Information) Cannot play sound" + System.getProperty("line.separator"));
            }
         }
      } else if (UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 0 & UserInterface.SAMEDISCONNECTION == (byte) 1) {

         UserInterface.SAMEDISCONNECTION = (byte) 0;

         UserInterface.LBLCURRENTSTATE.setText("UP");
         UserInterface.LBLCURRENTSTATE.setForeground(UserInterface.DARK_GREEN);

         if (UserInterface.CHKPLAYSOUND.isSelected() == true) {
            try {
               Clip clpDisconnected = AudioSystem.getClip();
               clpDisconnected
                     .open(AudioSystem.getAudioInputStream(new File("sounds" + File.separator + "connected.wav")));
               FloatControl gain = (FloatControl) clpDisconnected.getControl(FloatControl.Type.MASTER_GAIN);
               gain.setValue(((gain.getMaximum() - gain.getMinimum()) / 2) + gain.getMinimum());

               clpDisconnected.start();
            } catch (Exception e) {
               e.printStackTrace();
               UserInterface.DEBUG_OUTPUT
                     .append("(Information) Cannot play sound" + System.getProperty("line.separator"));
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
      }

      // loop disconnection sound if option is selected.
      if (UserInterface.CHKPLAYSOUNDLOOP.isSelected() == true & UserInterface.SAMEDISCONNECTION == (byte) 1) {

         try {
            clpDisconnected = AudioSystem.getClip();
            clpDisconnected
                  .open(AudioSystem.getAudioInputStream(new File("sounds" + File.separator + "disconnected.wav")));
            clpDisconnected.start();
         } catch (Exception e) {
            UserInterface.DEBUG_OUTPUT.append("(Information) Cannot play sound" + System.getProperty("line.separator"));
         }
      }

   }

   // free clip memory resources
   public synchronized void clearAudioFile() {
      clpDisconnected = null;
      System.gc();
   }

}
