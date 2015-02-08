import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.Timer;


public class Logger {
	public MessagePasser logger_MP = null;
	public PriorityQueue<TimeStampedMessage> logQueue = null;
	
	// GUI
    public static JFrame mainFrame = null;
    public static JTextArea msgText = null;
    public static JLabel statusBar = null;
    public static JToggleButton receiveButton = null;
    public static ActionAdapter buttonListener = null;
    public static Timer timer = null;
    
    public static class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {}
    }
	
    public TimeStampedMessage message = null;
    
    //Comparator implementation
    public static Comparator<TimeStampedMessage> timeStampComparator = new Comparator<TimeStampedMessage>(){
         
        @Override
        public int compare(TimeStampedMessage m1, TimeStampedMessage m2) {
	  		  String msg1 = m1.get_data().toString();
	  		  int ts1 = Integer.parseInt(msg1.substring(msg1.indexOf("TimeStamp:")+10, msg1.indexOf(" Seq:")));
	  		  String msg2 = m2.get_data().toString();
	  		  int ts2 = Integer.parseInt(msg2.substring(msg2.indexOf("TimeStamp:")+10, msg2.indexOf(" Seq:")));
            return (int) (ts1 - ts2);
        }
    };
    
	public Logger() {
		logger_MP = new MessagePasser("config", "logger");
		logQueue = new PriorityQueue<TimeStampedMessage>(10, timeStampComparator);
		
        // 	Set up the message pane
        JPanel messagePane = new JPanel(new BorderLayout());
        msgText = new JTextArea(10, 20);
        msgText.setLineWrap(true);
        msgText.setEditable(false);
        //msgText.setForeground(Color.blue);
        JScrollPane msgTextPane = new JScrollPane(msgText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagePane.add(msgTextPane, BorderLayout.CENTER);
        
        // 	Status bar
        statusBar = new JLabel();
        statusBar.setText("Connected");

        // Update Button
        JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPane.setPreferredSize(new Dimension(500,40));
        receiveButton = new JToggleButton("Receive Log Messages");
        receiveButton.setPreferredSize(new Dimension(300, 30));
        receiveButton.setEnabled(true);
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// Receive messages
            	logger_MP.receive();
            	if (!logger_MP.nodeME.receiveQueue.isEmpty()) {
		            try {
						message = logger_MP.nodeME.receiveQueue.take();
		            	logQueue.add(message);
						// Update clock after message received
						logger_MP.clockService.set_receiveTimeStamp(message.timeStamp);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

		            if (logger_MP.receiveDelay == 0 && !logger_MP.nodeME.delayedReceiveQueue.isEmpty()) {
		                for (int k=0; k < logger_MP.nodeME.delayedReceiveQueue.size(); k++) {
		                	try {
		                		message = logger_MP.nodeME.delayedReceiveQueue.take();
		                		logQueue.add(message);
								// Update clock after message received
		                		logger_MP.clockService.set_receiveTimeStamp(message.timeStamp);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		                }
		            }
            	}
            }    
        });
        receiveButton.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent ev) {
              if(ev.getStateChange()==ItemEvent.SELECTED){
                  statusBar.setText("Receiving log messages");
                  receiveButton.setText("Update Log");
                  timer.start();
              } 
              else if(ev.getStateChange()==ItemEvent.DESELECTED){
            	  timer.stop();
                  statusBar.setText("Updated Log");
            	  receiveButton.setText("Receive Log Messages");
            	  // Show updated log
            	  msgText.setText("");
            	  msgText.append("TimeStamp        Message\n");
            	  msgText.append("---------------------------------------------------------\n");
            	  
            	  for (int i=0; i<logQueue.size(); i++) {
            		  TimeStampedMessage ts_msg = (TimeStampedMessage) logQueue.toArray()[i];
            		  String msg = ts_msg.get_data().toString();
            		  int ts = Integer.parseInt(msg.substring(msg.indexOf("TimeStamp:")+10, msg.indexOf(" Seq:")));
            		  msgText.append(ts + "        " + msg +"\n");
            	  }
              }
           }
        });
        buttonsPane.add(receiveButton);
        
    // 	Set up the main pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.add(buttonsPane, BorderLayout.NORTH);
        mainPane.add(messagePane, BorderLayout.CENTER);
        mainPane.add(statusBar, BorderLayout.SOUTH);
    
    // 	Set up the main frame
        mainFrame = new JFrame("Distributed Systems: Lab 1 Logger");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(mainPane);
        mainFrame.setSize(mainFrame.getPreferredSize());
        mainFrame.setLocation(200, 200);
        mainFrame.pack();
        mainFrame.setVisible(true);
	}
	
    public static void main(String[] args) {
        new Logger();
    }
}
