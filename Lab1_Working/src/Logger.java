import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;
import javax.swing.Timer;

public class Logger {
	
	// Logger Message Passer
	public MessagePasser logger_MP = null;
	public String[] names = null;
	
	// Logger Message Queue
	public BlockingQueue<TimeStampedMessage> logQueue = new LinkedBlockingQueue<TimeStampedMessage>();
	public TimeStampedMessage message = null;
	
	// GUI
    public static JFrame mainFrame = null;
    public static JTextArea msgText = null;
    public static JLabel statusBar = null;
    public static JToggleButton receiveButton = null;
    public static ActionAdapter buttonListener = null;
    public static Timer timer = null;
    public static class ActionAdapter implements ActionListener { public void actionPerformed(ActionEvent e) {} }

    
    @SuppressWarnings("static-access")
	public String ChangeTimeStampFormat (String old_ts) {
    	names = logger_MP.nodes;
    	int[] values = new int[names.length];
		for (int j=0; j<names.length; j++) {
			values[j] = 0;
		}
    	String new_ts;
    	
    	String[] parts = old_ts.split(",");
    	for (int i=0; i<parts.length; i++) {
    		String aname = parts[i].substring(0, parts[i].indexOf("="));
    		int atimestamp = Integer.parseInt(parts[i].substring(parts[i].indexOf("=")+1,parts[i].length()));
    		for (int j=0; j<names.length; j++) {
    			if (names[j].equalsIgnoreCase(aname)) {
    				values[j] = atimestamp;
    				break;
    			}
    		}
    	}
    	new_ts = "("+values[0]+","+values[1]+","+values[2]+")";
    	return new_ts;
    }
    
    // Determine Concurrency Function
    public void DetermineConcurrency () {
    	String msg; String log_ts; 
    	TimeStampedMessage ts_msg;
    	String tsp; String new_tsp;
		String[] sortedArray = new String[logQueue.size()];
		String[] statusArray = new String[logQueue.size()];
		
		msgText.setText("");
		msgText.append("Index	Status	TimeStamp	LogTimeStamp			Message\n");
		msgText.append("------------------------------------------------------------------------------------------------------------------------------------------------------\n");

		// Get and Sort array first
		for (int i=0; i<logQueue.size(); i++) {
			ts_msg = (TimeStampedMessage) logQueue.toArray()[i];
			msg = ts_msg.get_data().toString();
			tsp = msg.substring(msg.indexOf("TimeStamp:")+10, msg.indexOf(" Seq:"));
			new_tsp = ChangeTimeStampFormat(tsp);
			log_ts = ts_msg.get_timeStamp().ts;
			sortedArray[i] = new_tsp + "	" + log_ts + "	" + msg;
		}
		Arrays.sort(sortedArray);
		
		// Check if equal/same events
		String m; 	String t;
		String ms; 	String ts;
		String[] ts_parts;
		String[] t_parts;
		boolean neg = false;
		boolean pos = false;
		statusArray[0] = " ";
		for (int j=0; j<logQueue.size(); j++) {
			ms = sortedArray[j].toString();
			ts = ms.substring(ms.indexOf("(")+1, ms.indexOf(")"));
			ts_parts = ts.split(",");	

			// Check every other element to see if equal/same events
			for (int k=j+1; k<logQueue.size(); k++) {
				System.out.println("Checking: " + j + " and " + k);
				m = sortedArray[k].toString();
				t = m.substring(m.indexOf("(")+1, m.indexOf(")"));
				t_parts = t.split(",");
				if (t.equals(ts)) {
					statusArray[k] = j + "=" + k;
					
				// Check if NOT equal
				} else {
					pos = false; 
					neg = false;
					for (int q=0; q<t_parts.length; q++) {
						// Check if A >= B 
						if ((Integer.parseInt(t_parts[q])-Integer.parseInt(ts_parts[q])) > 0) {
							pos = true;
						}
						// Check if A <= B
						if ((Integer.parseInt(t_parts[q])-Integer.parseInt(ts_parts[q])) < 0) {
							neg = true;
						}
					}
					if (pos && neg) {
						statusArray[k] = j + "||" + k;
						System.out.println("Concurrent!");
					} else {
						statusArray[k] = " ";
					}
				}
			}
		}
		
		// Finally group together all results
		for (int i=0; i<logQueue.size(); i++) {
			msgText.append(i + "	" + statusArray[i] + "	" + sortedArray[i].toString()+"\n");
		}
    }
    
    // Logger constructor
	@SuppressWarnings("static-access")
	public Logger() {
		
		// Logger MessagePasser
		logger_MP = new MessagePasser("config", "logger");
		
		// Disable buttons for Logger (because NA)
		logger_MP.sendButton.setEnabled(false); 
		logger_MP.receiveButton.setEnabled(false);
		logger_MP.logButton.setEnabled(false);
		logger_MP.nodeList.setEnabled(false);
		logger_MP.msgLine.setEnabled(false);
		logger_MP.kindField.setEnabled(false);
		
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
        timer = new Timer(700, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// Receive messages
            	logger_MP.receive();
            	if (!logger_MP.nodeME.receiveQueue.isEmpty()) {
		            try {
						message = logger_MP.nodeME.receiveQueue.take();
		            	logQueue.add(message);
						// Update clock after message received
						logger_MP.clockService.set_receiveTimeStamp(message.get_timeStamp());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

		            if (logger_MP.receiveDelay == 0 && !logger_MP.nodeME.delayedReceiveQueue.isEmpty()) {
		                for (int k=0; k < logger_MP.nodeME.delayedReceiveQueue.size(); k++) {
		                	try {
		                		message = logger_MP.nodeME.delayedReceiveQueue.take();
		                		logQueue.add(message);
								// Update clock after message received
		                		logger_MP.clockService.set_receiveTimeStamp(message.get_timeStamp());
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

            	  DetermineConcurrency();
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
	
	// Logger main
    public static void main(String[] args) {
        new Logger();
    }
}
