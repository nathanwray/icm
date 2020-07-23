package com.detroitsci.icm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

import com.jhlabs.awt.BasicGridLayout;

import sun.net.dns.ResolverConfiguration;

public class UserInterface {

   // PROPERTIES can be overridden in property file
   public static String disconnectLogPath = "";
   public static int defaultTestIntervalSeconds = 7;
   public static int failingTestIntervalSeconds = 1;
   public static int successesAfterFailRequired = 5;

   public static String connectedAudioFile;
   public static URL connectedURL;
   public static String disconnectedAudioFile;
   public static URL disconnectedURL;

   public static String[] dnsTestUrls;
   // PROPERTIES

   public static int primaryResumeSuccessesRemaining = 0;

   // Disconnections counts the number of times internet connectivity was lost.
   Disconnections disconnectionCounter = new Disconnections();

   // HttpConn is the class that performs the actual testing. I am creating one
   // object for each connection
   HttpConn netRun = new HttpConn("Primary", disconnectionCounter);
   Thread netThread = new Thread(netRun);

   // flags used to start/stop monitor threads
   byte threadFlag = 0;
   // byte threadFlagSecondary = 0;

   // disconnection flags and counter. Static variables.
   public static byte DISCONNECTEDPRIMARYSITE = 0;
   public static int DISCONNECTIONCOUNTER = 0;
   public static byte SAMEDISCONNECTION = 0;

   // Results are sent here from the Net instance (OUTPUT is a "global" variable)
   public static JTextArea OUTPUT = new JTextArea();
   JScrollPane scrOutput = new JScrollPane(OUTPUT);

   public static JTextArea DEBUG_OUTPUT = new JTextArea();
   JScrollPane scrDebug = new JScrollPane(DEBUG_OUTPUT);

   // user interface elements
   JFrame frame = new JFrame("Internet Connectivity Monitor v2.0");
   JButton btnMonitor = new JButton("Start Monitoring");
   JButton btnClear = new JButton("Clear Log");
   JButton btnSave = new JButton("Save Log");
   JButton btnExit = new JButton("Exit");
   JButton btnAbout = new JButton("About");

   public static JCheckBox CHKPLAYSOUND = new JCheckBox("Play sound on disconnect / reconnect");
   public static JCheckBox CHKPLAYSOUNDLOOP = new JCheckBox("Play sound continuously until reconnected");

   public static JCheckBox CHKAUTOSAVE = new JCheckBox("Automatically save log");
   JLabel lblAutoSaveLabel = new JLabel("Save path: ");
   public static JTextField TXTAUTOSAVEPATH = new JTextField(17);

   public static Color DARK_GREEN = new Color(0, 153, 0);

   JLabel lblDisconnectionsLabel = new JLabel("Disconnections: ");
   public static JLabel LBLDISCONNECTIONCOUNTER = new JLabel(Integer.toString(DISCONNECTIONCOUNTER));

   JLabel lblCurrentStateLabel = new JLabel("Current State: ");
   public static JLabel LBLCURRENTSTATE = new JLabel("UP");

   // panels to hold UI elements (used to properly arrange elements)
   JPanel pnlAddressInstruction = new JPanel();
   JPanel pnlAddress = new JPanel();
   JPanel pnlMonitorButton = new JPanel();
   JPanel pnlLeftSide = new JPanel();
   JPanel pnlRightSide = new JPanel();

   JPanel pnlBlankAfterFrequency = new JPanel();
   JPanel pnlBlankAfterMonitorButton = new JPanel();
   JPanel pnlBlankAfterDisconnectionsCounter = new JPanel();

   JPanel pnlBlankAfterPlaySoundGroup = new JPanel();
   JPanel pnlBlankAfterAutosaveGroup = new JPanel();
   JPanel pnlControlButtons = new JPanel();

   JPanel pnlPlaySound = new JPanel();
   JPanel pnlLoopSound = new JPanel();

   JPanel pnlAutosaveCheckbox = new JPanel();
   JPanel pnlAutosavePath = new JPanel();

   JPanel pnlDisconnections = new JPanel();

   JPanel pnlAbout = new JPanel();

   // here is where everything starts
   public static void main(String[] args) {

      UserInterface start = new UserInterface();
      start.loadProperties();
      start.BuildInterface();

   }

   private void loadProperties() {
      InputStream is = null;
      Properties p = new Properties();

      try {
         String propFileName = "icm.properties";

         is = getClass().getClassLoader().getResourceAsStream(propFileName);

         if (is != null) {
            p.load(is);
         } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
         }

      } catch (Exception e) {
         System.out.println("Exception: " + e);
      } finally {
         try {
            if (is != null) {
               is.close();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      if (p.containsKey("disconnectLogPath")) {
         try {
            File f = new File(p.getProperty("disconnectLogPath"));
            disconnectLogPath = f.getCanonicalPath();
         } catch (IOException ioe) {
            System.err.println("Could not parse disconnectLogPath from icm.properties: " + ioe);
         }
      }

      if (p.containsKey("defaultTestIntervalSeconds"))

      {
         try {
            defaultTestIntervalSeconds = Integer.parseInt(p.getProperty("defaultTestIntervalSeconds"));
         } catch (NumberFormatException nfe) {
            System.err.println("Could not parse defaultTestIntervalSeconds from icm.properties: " + nfe);
         }
      }

      if (p.containsKey("failingTestIntervalSeconds")) {
         try {
            failingTestIntervalSeconds = Integer.parseInt(p.getProperty("failingTestIntervalSeconds"));
         } catch (NumberFormatException nfe) {
            System.err.println("Could not parse failingTestIntervalSeconds from icm.properties: " + nfe);
         }
      }

      if (p.containsKey("successesAfterFailRequired")) {
         try {
            successesAfterFailRequired = Integer.parseInt(p.getProperty("successesAfterFailRequired"));
         } catch (NumberFormatException nfe) {
            System.err.println("Could not parse successesAfterFailRequired from icm.properties: " + nfe);
         }
      }

      if (p.containsKey("connectedAudioFile")) {
         connectedAudioFile = p.getProperty("connectedAudioFile");
      }

      if (p.containsKey("connectedURL")) {
         try {
            connectedURL = new URL(p.getProperty("connectedURL"));
         } catch (MalformedURLException mfe) {
            System.err.println("Could not parse connectedURL from icm.properties: " + mfe);
         }
      }

      if (p.containsKey("disconnectedAudioFile")) {
         disconnectedAudioFile = p.getProperty("disconnectedAudioFile");
      }

      if (p.containsKey("disconnectedURL")) {
         try {
            disconnectedURL = new URL(p.getProperty("disconnectedURL"));
         } catch (MalformedURLException mfe) {
            System.err.println("Could not parse disconnectedURL from icm.properties: " + mfe);
         }
      }

      if (p.containsKey("dnsTestUrls")) {
         dnsTestUrls = p.getProperty("dnsTestUrls").split(",");
      }

   }

   private void BuildInterface() {

      try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e) {
         System.err.println("unable to set look and feel");
         e.printStackTrace();
      }

      // format user interface elements

      lblDisconnectionsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
      lblDisconnectionsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      LBLDISCONNECTIONCOUNTER.setFont(new Font("Arial", Font.BOLD, 20));
      LBLDISCONNECTIONCOUNTER.setForeground(DARK_GREEN);
      LBLDISCONNECTIONCOUNTER.setHorizontalAlignment(SwingConstants.LEFT);

      lblCurrentStateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
      lblCurrentStateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      LBLCURRENTSTATE.setFont(new Font("Arial", Font.BOLD, 20));
      LBLCURRENTSTATE.setForeground(DARK_GREEN);
      LBLCURRENTSTATE.setHorizontalAlignment(SwingConstants.LEFT);

      CHKPLAYSOUNDLOOP.setEnabled(true);
      TXTAUTOSAVEPATH.setText(disconnectLogPath + File.separator + "disconnections-" + new Date().getTime() + ".log");
      TXTAUTOSAVEPATH.setEnabled(true);
      TXTAUTOSAVEPATH.setDisabledTextColor(Color.GRAY);

      OUTPUT.setLineWrap(true);
      ((DefaultCaret) OUTPUT.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      scrOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      OUTPUT.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

      DEBUG_OUTPUT.setLineWrap(true);
      ((DefaultCaret) DEBUG_OUTPUT.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      scrDebug.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      DEBUG_OUTPUT.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      // make the debug panel font 80% of the default size
      DEBUG_OUTPUT.setFont(new Font(DEBUG_OUTPUT.getFont().getFamily(), DEBUG_OUTPUT.getFont().getStyle(),
            (int) (DEBUG_OUTPUT.getFont().getSize() * 0.8)));

      scrOutput.setPreferredSize(new Dimension(300, 200));
      scrDebug.setPreferredSize(new Dimension(300, 200));

      pnlLeftSide.setPreferredSize(new Dimension(300, 500));
      pnlRightSide.setPreferredSize(new Dimension(375, 500));
      pnlBlankAfterFrequency.setPreferredSize(new Dimension(230, 20));
      pnlBlankAfterMonitorButton.setPreferredSize(new Dimension(230, 20));
      pnlBlankAfterDisconnectionsCounter.setPreferredSize(new Dimension(230, 20));
      pnlBlankAfterPlaySoundGroup.setPreferredSize(new Dimension(230, 10));
      pnlBlankAfterAutosaveGroup.setPreferredSize(new Dimension(230, 20));
      btnMonitor.setPreferredSize(new Dimension(170, 26));
      btnSave.setPreferredSize(new Dimension(103, 26));
      btnClear.setPreferredSize(new Dimension(103, 26));
      btnExit.setPreferredSize(new Dimension(103, 26));
      btnAbout.setPreferredSize(new Dimension(103, 26));

      // Create panels that will contain elements. Each panel contains elements
      // aligned horizontally.
      // For each "line" of elements I am using a separate panel
      pnlAddressInstruction.setLayout(new BasicGridLayout(1, 1));

      pnlAddress.setLayout(new BasicGridLayout(1, 2));

//      pnlFrequency.setPreferredSize(new Dimension(230, 20));
//      pnlFrequency.setLayout(new BasicGridLayout(1, 3));
//      pnlFrequency.add(lblFrequency);
//      pnlFrequency.add(txtFrequency);
//      pnlFrequency.add(lblSeconds);

      pnlPlaySound.setLayout(new BasicGridLayout(1, 1));
      pnlPlaySound.add(CHKPLAYSOUND);
      CHKPLAYSOUND.doClick();

      pnlLoopSound.setLayout(new BasicGridLayout(1, 1));
      pnlLoopSound.add(CHKPLAYSOUNDLOOP);

      pnlAutosaveCheckbox.setLayout(new BasicGridLayout(1, 1));
      pnlAutosaveCheckbox.add(CHKAUTOSAVE);
      CHKAUTOSAVE.doClick();

      pnlAutosavePath.setLayout(new BasicGridLayout(1, 2));
      pnlAutosavePath.add(lblAutoSaveLabel);
      pnlAutosavePath.add(TXTAUTOSAVEPATH);

      pnlDisconnections.setPreferredSize(new Dimension(230, 60));
      pnlDisconnections.setLayout(new BasicGridLayout(2, 2));
      pnlDisconnections.add(lblDisconnectionsLabel);
      pnlDisconnections.add(LBLDISCONNECTIONCOUNTER);

      pnlDisconnections.add(lblCurrentStateLabel);
      pnlDisconnections.add(LBLCURRENTSTATE);

      pnlMonitorButton.add(btnMonitor);

      pnlAbout.add(btnAbout);

      pnlControlButtons.add(btnSave);
      pnlControlButtons.add(btnClear);
      pnlControlButtons.add(btnExit);

      pnlLeftSide.setLayout(new BasicGridLayout(0, 1, 5, 5, 25, 15));
      pnlLeftSide.add(pnlAddressInstruction);
      pnlLeftSide.add(pnlAddress);
      pnlLeftSide.add(pnlBlankAfterFrequency);
      pnlLeftSide.add(pnlMonitorButton);
      pnlLeftSide.add(pnlBlankAfterMonitorButton);
      pnlLeftSide.add(pnlDisconnections);
      // pnlLeftSide.add(pnlBlankAfterDisconnectionsCounter);
      pnlLeftSide.add(Box.createVerticalStrut(5));
      pnlLeftSide.add(new JSeparator(SwingConstants.HORIZONTAL));
      pnlLeftSide.add(Box.createVerticalStrut(5));
      pnlLeftSide.add(pnlPlaySound);
      pnlLeftSide.add(pnlLoopSound);
      pnlLeftSide.add(pnlBlankAfterPlaySoundGroup);
      pnlLeftSide.add(pnlAutosaveCheckbox);
      pnlLeftSide.add(pnlAutosavePath);
      pnlLeftSide.add(Box.createVerticalStrut(5));
      pnlLeftSide.add(new JSeparator(SwingConstants.HORIZONTAL));
      pnlLeftSide.add(Box.createVerticalStrut(5));
      pnlLeftSide.add(pnlBlankAfterAutosaveGroup);

      pnlLeftSide.add(pnlAbout);

      pnlRightSide.setLayout(new BasicGridLayout(3, 1, 5, 5, 25, 15));
      pnlRightSide.add(scrOutput);
      pnlRightSide.add(scrDebug);
      pnlRightSide.add(pnlControlButtons);

      frame.getContentPane().add(BorderLayout.WEST, pnlLeftSide);
      frame.getContentPane().add(BorderLayout.EAST, pnlRightSide);
      frame.setResizable(false);

      frame.setLocationByPlatform(true);
      frame.setSize(700, 515); // 650,500
      frame.setVisible(true);

      btnMonitor.addActionListener(new StartMonitor());
      btnClear.addActionListener(new ClearTextArea());
      btnSave.addActionListener(new SaveLog());
      btnExit.addActionListener(new ExitProgram());
      CHKPLAYSOUND.addActionListener(new CheckboxStatus());
      CHKPLAYSOUNDLOOP.addActionListener(new CheckboxStatus());
      CHKAUTOSAVE.addActionListener(new CheckboxStatus());
      btnAbout.addActionListener(new AboutMessage());

   }

   class StartMonitor implements ActionListener {
      @SuppressWarnings("deprecation")
      public void actionPerformed(ActionEvent arg0) {

         OUTPUT.setEditable(false);

         netRun.setFlag((byte) 0);

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         btnMonitor.setText("Pause Monitoring");
         OUTPUT.append("Monitoring started " + String.format("%ta %<tb %<td %<tT", new Date())
               + System.getProperty("line.separator"));

         List<String> nameServers = ResolverConfiguration.open().nameservers();
         nameServers.forEach(
               (dns) -> DEBUG_OUTPUT.append("Using name server: " + dns + System.getProperty("line.separator")));

         DEBUG_OUTPUT.append(
               "Using " + dnsTestUrls.length + " test URLs from icm.properties" + System.getProperty("line.separator"));

         CHKPLAYSOUND.setEnabled(false);
         CHKPLAYSOUNDLOOP.setEnabled(false);
         CHKAUTOSAVE.setEnabled(false);
         TXTAUTOSAVEPATH.setEnabled(false);
         lblAutoSaveLabel.setForeground(Color.GRAY);
         btnClear.setEnabled(false);
         btnSave.setEnabled(false);

         // switch button functionality to from Start to End monitoring
         btnMonitor.removeActionListener(this);
         btnMonitor.addActionListener(new EndMonitor());

         // start monitor threads
         if (threadFlag == 0) {
            netThread.start();
            threadFlag = 1;
         } else {
            netThread.resume();
         }

      }
   }

   class EndMonitor implements ActionListener {
      @SuppressWarnings("deprecation")
      public void actionPerformed(ActionEvent arg0) {

         // pause monitor threads
         try {
            netThread.suspend();
         } catch (Exception e) {
            e.printStackTrace();
            DEBUG_OUTPUT.append("ERROR: Primary monitor thread suspend exception" + System.getProperty("line.separator")
                  + System.getProperty("line.separator"));
         }

         netRun.setFlag((byte) 1);

         if (CHKPLAYSOUND.isSelected() == true) {
            CHKPLAYSOUNDLOOP.setEnabled(true);
         }

         if (CHKAUTOSAVE.isSelected() == true) {
            TXTAUTOSAVEPATH.setEnabled(true);
            lblAutoSaveLabel.setForeground(Color.BLACK);
         }

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         // re-enable user interface elements
         btnMonitor.setText("Resume Monitoring");
         OUTPUT.append("Monitoring stopped by user" + System.getProperty("line.separator")
               + System.getProperty("line.separator"));

         OUTPUT.setEditable(true);

         CHKPLAYSOUND.setEnabled(true);
         CHKAUTOSAVE.setEnabled(true);
         btnClear.setEnabled(true);
         btnSave.setEnabled(true);

         // switch button functionality to from End to Start monitoring
         btnMonitor.removeActionListener(this);
         btnMonitor.addActionListener(new StartMonitor());
      }
   }

   class ClearTextArea implements ActionListener {
      public void actionPerformed(ActionEvent arg0) {

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         // clear output window text
         OUTPUT.setText("");

         btnMonitor.setText("Start Monitoring");

         // clear disconnection counter elements
         DISCONNECTEDPRIMARYSITE = 0;
         DISCONNECTIONCOUNTER = 0;
         SAMEDISCONNECTION = 0;
         LBLDISCONNECTIONCOUNTER.setText(Integer.toString(DISCONNECTIONCOUNTER));
         LBLDISCONNECTIONCOUNTER.setForeground(DARK_GREEN);
      }
   }

   class SaveLog implements ActionListener {
      public void actionPerformed(ActionEvent arg0) {

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         // ask user where to save output file
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.showSaveDialog(frame);

         File file = fileChooser.getSelectedFile();

         try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(OUTPUT.getText());
            bw.flush();
            bw.close();
         } catch (IOException e) {
            DEBUG_OUTPUT.append("File write error. The file is unavailable or read-only."
                  + System.getProperty("line.separator") + System.getProperty("line.separator"));
            e.printStackTrace();
         } catch (NullPointerException e) {
            UserInterface.DEBUG_OUTPUT
                  .append("Save file dialog canceled. File was not saved." + System.getProperty("line.separator"));
         }
      }
   }

   class CheckboxStatus implements ActionListener {
      public void actionPerformed(ActionEvent arg0) {

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         if (CHKPLAYSOUND.isSelected() == true) {
            CHKPLAYSOUNDLOOP.setEnabled(true);
         } else {
            CHKPLAYSOUNDLOOP.setEnabled(false);
            CHKPLAYSOUNDLOOP.setSelected(false);
         }

         if (CHKAUTOSAVE.isSelected() == true) {
            TXTAUTOSAVEPATH.setEnabled(true);
            lblAutoSaveLabel.setForeground(Color.BLACK);
         } else {
            TXTAUTOSAVEPATH.setEnabled(false);
            lblAutoSaveLabel.setForeground(Color.GRAY);
         }

      }
   }

   class AboutMessage implements ActionListener {
      public void actionPerformed(ActionEvent arg0) {

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         // show message box with application version information

         String aboutMessage = "<html><body><H3>Internet Connectivity Monitor</H3>" + "<H4>v2.0 - July 2020</H4>"
               + "Initial applicaiton developed by <b>Genc Alikaj</b> (<a href='mailto:gencalikaj@gmail.com' style='text-decoration: none'>gencalikaj@gmail.com</a>)<br><br>"
               + "The following portions were developed/fixed by <b>Jhobanny Morillo</b>:"
               + "<ul><li>Disconnections counter</li>" + "<li>Text formatting in log file</li></ul><br>"
               + "v2.0 The following changed were implemented by <b>Nathan Wray</b>:<ul>"
               + "<li>Removed second thread</li>" + "<li>Added UP/DOWN UI element</li>"
               + "<li>Converted to DNS resolution from socket-based detection (see README)</li>"
               + "<li>Added list of top sites to cycle through</li>" + "<li>Require multiple ("
               + successesAfterFailRequired + ") successes before cycling to UP</li>" + "<li>Cycle faster ("
               + failingTestIntervalSeconds + " sec) while down</li>" + "<li>Sound plays when reconnected</li>"
               + "<li>Altered outage sound file and volume</li>" + "<li>Made UI smaller</li>"
               + "<li>Separate outage logging and \"Debug\" logging</li>" + "</ul><br>"

               + "For more info and the latest version of the program visit:<br>"
               + "<a href='http://code.google.com/p/internetconnectivitymonitor/' style='text-decoration: none'>http://code.google.com/p/internetconnectivitymonitor/</a></body></html>";

         JOptionPane.showMessageDialog(null, aboutMessage, "About Internet Connectivity Monitor",
               JOptionPane.INFORMATION_MESSAGE);

      }
   }

   class ExitProgram implements ActionListener {
      public void actionPerformed(ActionEvent arg0) {

         // changing element focus back to the main frame to avoid ugly rectangles around
         // button text
         frame.requestFocus();

         // exit program
         System.exit(0);
      }
   }
}
