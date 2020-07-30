package com.detroitsci.icm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;

public class HttpConn implements Runnable {

   private byte runFlag = 0;

   private byte fileLocked = 0;

   String objectName = "Primary";

   Disconnections disconnectionObject;

   // private String address;
   private int addressOffset = 0;
   private Date downSince = null;

   // beta testing this, it's not going well
   // (many false negatives; walmart, apple, ebay all seem to fail this 100%)
   private boolean testReachable = false;

   private static final int REACHABLE_MAX_WAIT_MILLIS = 30000;

   // using a custom constructor that assigns a name to this object
   // and brings a reference to the Disconnections class
   public HttpConn(String name, Disconnections dis) {
      objectName = name;
      disconnectionObject = dis;
   }

   // run method required for implementing Runnable interface.
   // The loop begins when the thread starts (this method is the first thing that
   // runs).
   // Process depends on byte flag at the top of the class.
   public void run() {

      java.security.Security.setProperty("networkaddress.cache.ttl", "0");
      java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
      System.setProperty("sun.net.inetaddr.ttl", "0");
      System.setProperty("sun.net.inetaddr.negative.ttl", "0");

      while (runFlag == 0) {
         long startedAt = new Date().getTime();
         StartTest();
         long frequencyMilliseconds = UserInterface.DISCONNECTEDPRIMARYSITE == (byte) 1
               ? (UserInterface.failingTestIntervalSeconds * 1000)
               : (UserInterface.defaultTestIntervalSeconds * 1000);

         // wait a few seconds before repeating the process. The waiting time is defined
         // by the user (in the "frequency" field). Subtract the time spent testing.
         long waitMilliseconds = Math.max(frequencyMilliseconds - (new Date().getTime() - startedAt), 0L);
         try {
            Thread.sleep(waitMilliseconds);
         } catch (Exception ex) {
            UserInterface.DEBUG_OUTPUT.append("Thread.sleep exception on CATCH" + System.getProperty("line.separator"));
         }
      }
   }

   // mutator that sets flag value in order to start or stop test loop
   public void setFlag(byte x) {
      runFlag = x;
   }

   // web address mutator
   public void setTestReachable(boolean shouldTest) {
      testReachable = shouldTest;
   }

   public String getNextAddress() {
      String addr = UserInterface.dnsTestUrls[addressOffset];

      addressOffset = ++addressOffset % UserInterface.dnsTestUrls.length;

      return addr;
   }

   public void StartTest() {
      String address = getNextAddress();

      try {

         // define URL and try to connect to server
         InetAddress ip = InetAddress.getByName(new URL(address).getHost());
         UserInterface.DEBUG_OUTPUT.append("Address for " + address + " is " + ip.getHostAddress());

         if (testReachable) {
            if (!ip.isReachable(REACHABLE_MAX_WAIT_MILLIS)) {
               UserInterface.DEBUG_OUTPUT.append(System.getProperty("line.separator"));
               throw new IOException(address + " not reachable");
            }
            UserInterface.DEBUG_OUTPUT.append(" (reachable)");
         }
         UserInterface.DEBUG_OUTPUT.append(System.getProperty("line.separator"));

         if (UserInterface.primaryResumeSuccessesRemaining > 0) {
            UserInterface.primaryResumeSuccessesRemaining--;
         } else if (downSince != null) {
            String date = String.format("%ta %<tb %<td %<tT", downSince);
            long downSeconds = (new Date().getTime() - downSince.getTime()) / 1000;

            UserInterface.OUTPUT
                  .append(date + ": system down " + downSeconds + " seconds" + System.getProperty("line.separator"));
            // write or update the automatic log file if the option is selected
            AutoWriteLogFile();

            downSince = null;
            UserInterface.DISCONNECTEDPRIMARYSITE = 0;
         }

         // perform a disconnection counter check
         disconnectionObject.run();

      } catch (Exception e) {
         UserInterface.DEBUG_OUTPUT.append("Exception testing " + address + System.getProperty("line.separator"));

         if (downSince == null) {
            downSince = new Date();
         }
         UserInterface.primaryResumeSuccessesRemaining = UserInterface.successesAfterFailRequired;

         // announce to the disconnection flag that the connection was unsuccessful
         // if (objectName == "Primary") {
         UserInterface.DISCONNECTEDPRIMARYSITE = 1;

         // perform a disconnection counter check
         disconnectionObject.stopAudioFile();
         disconnectionObject.run();
      }
   }

   public void AutoWriteLogFile() {
      if (fileLocked != 1 && UserInterface.CHKAUTOSAVE.isSelected()) {
         try {
            // get path from textbox, and then add escape characters to slashes to avoid
            // errors.
            String rawPath = UserInterface.TXTAUTOSAVEPATH.getText();
            String fixedPath = rawPath.replace("\\", "\\\\");

            File logFile = new File(fixedPath);

            // create path if missing
            logFile.getParentFile().mkdirs();

            // write contents of Output textarea to file
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile));
            bw.write(UserInterface.OUTPUT.getText());

            // close filewriter
            bw.flush();
            bw.close();

         } catch (Exception e) {
            UserInterface.DEBUG_OUTPUT.append(System.getProperty("line.separator")
                  + "Cannot automatically create or write to file. Please check path."
                  + System.getProperty("line.separator"));
            UserInterface.DEBUG_OUTPUT
                  .append("It's possible that access to the selected location is denied. Try using a different path."
                        + System.getProperty("line.separator") + System.getProperty("line.separator"));
            e.printStackTrace();
            // stop trying to automatically write to file if it's not accessible
            fileLocked = 1;
         }
      }
   }

}
