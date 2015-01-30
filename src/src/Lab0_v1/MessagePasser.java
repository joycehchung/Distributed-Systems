import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class MessagePasser {
	
	// Servers and sockets
	Socket mySocket;
	ServerSocket myServerSocket;
	Socket otherSocket;
	ServerSocket otherServerSocket;
	
	// Status
	public final static int DISCONNECTED = 0;
	public final static int CONNECTED = 1;
	public final static int ISSERVER = 1;
	public final static int ISNOTSERVER = 0;
	public static int myStatus = DISCONNECTED;
	public static int iAmServer = ISSERVER;
	
	// GUI
	public static JFrame mainFrame = null;
	public static JTextArea msgText = null;
	public static JTextField msgLine = null;
	public static JLabel statusBar = null;
	public static JList nodeList = null;
	public static JLabel myNameLabel = null;
	public static JLabel myIpLabel = null;
	public static JLabel myPortLabel = null;
	public static JButton connectButton = null;
	public static JButton disconnectButton = null;
	public static JButton sendButton = null;
	public static JButton receiveButton = null;
	public static JLabel whoLabel = null;
	public static int connectionStatus = DISCONNECTED;
    public static ActionAdapter buttonListener = null;
    public static ListSelectionListener listListener = null;
    public static ActionAdapter messageButtonListener = null;
	   
	// For now just passing through message (will eventually need to use Message class)
	public static String message = "";
	public static StringBuffer toReceive = new StringBuffer("");
	public static StringBuffer toSend = new StringBuffer("");
	public static BufferedReader getIn = null;
	public static PrintWriter sendOut = null;
	
	// Action adapter for easy event-listener coding
	public static class ActionAdapter implements ActionListener {
	   public void actionPerformed(ActionEvent e) {}
	}
	
	// Message Passer constructor
	public MessagePasser(String configuration_filename, String local_name, 
			final String myName, final String myIP, final int myPort, String nodes[],
			final String otherName, final String otherIP, final int otherPort, final int swap) {
		
			// Set up GUI
			    // Set up the status bar
			    statusBar = new JLabel();
			    statusBar.setText("Disconnected");
		
			    // Set up the information and message pane 
			    // Parameters include hard-coded NAME, IP, PORT, NODES for now

			    // Node Information Pane
			    JPanel pane = null;
		    	JPanel infoPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    	infoPane.setPreferredSize(new Dimension(250, 500));
		    	pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    	pane.setPreferredSize(new Dimension(250, 20));
		    	pane.add(new JLabel("My Name:"));
		    		myNameLabel = new JLabel();
	    			myNameLabel.setText(myName); 
	    			pane.add(myNameLabel);
	    			infoPane.add(pane);
    			pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    			pane.setPreferredSize(new Dimension(250, 20));
    			pane.add(new JLabel("My IP:"));
    				myIpLabel = new JLabel();	
					myIpLabel.setText(myIP);
					pane.add(myIpLabel);
					infoPane.add(pane);
				pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
				pane.setPreferredSize(new Dimension(250, 20));
				pane.add(new JLabel("My Port:"));
					myPortLabel = new JLabel();	
					myPortLabel.setText((new Integer(myPort)).toString());
					pane.add(myPortLabel);
					infoPane.add(pane);
						
				// Node List
				JPanel nodeListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				nodeListPanel.setPreferredSize(new Dimension(250, 80));
				pane = new JPanel(new FlowLayout(FlowLayout.CENTER));
				pane.setPreferredSize(new Dimension(250, 20));
		    	pane.add(new JLabel("Connected Nodes:"));
				infoPane.add(pane);
				nodeList = new JList(nodes);
				nodeList.setEnabled(false);
				nodeListPanel.add(nodeList);
				infoPane.add(nodeListPanel);
				listListener = new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent arg0) {
						whoLabel.setText("Messages sent to " + nodeList.getSelectedValue().toString() + " :");
						
					}
				};
				nodeList.addListSelectionListener(listListener);

				// Begin/End Connect Button
				JPanel connectPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
				connectPane.setPreferredSize(new Dimension(250,30));
				buttonListener = new ActionAdapter() {
		            public void actionPerformed(ActionEvent e) {
		               // Connect to other nodes
		               if (e.getActionCommand().equals("connect")) {
		                  connectButton.setEnabled(false);
		                  disconnectButton.setEnabled(true);
		                  sendButton.setEnabled(true);
		                  receiveButton.setEnabled(true);
		                  connectionStatus = CONNECTED;
		                  nodeList.setEnabled(true);
		                  msgLine.setEnabled(true);
		                  statusBar.setText("Connected");
		                  mainFrame.repaint();
				  		      	if (swap == 0) {
									try { // As a server try to connect to client of other node
										myServerSocket = new ServerSocket(myPort);
										myServerSocket.setSoTimeout(10000); 
												System.out.println("Waiting for client on port " + myServerSocket.getLocalPort() + "..."); 
												mySocket = myServerSocket.accept();
												System.out.println("Just connected to " + mySocket.getRemoteSocketAddress());  
												myStatus = CONNECTED;
												iAmServer = ISSERVER; // Redundant, just put it here for now
										getIn = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
										sendOut = new PrintWriter(mySocket.getOutputStream(),true);
									} catch (IOException e1) { // As a client, try to connect to server of other node
										e1.printStackTrace();
										try {
											otherSocket = new Socket(otherIP, otherPort);
													System.out.println("Connecting to server on port " + otherPort); 
													System.out.println("Just connected to " + otherSocket.getRemoteSocketAddress()); 
													myStatus = CONNECTED;
													iAmServer = ISNOTSERVER;
											getIn = new BufferedReader(new InputStreamReader(otherSocket.getInputStream()));
											sendOut = new PrintWriter(otherSocket.getOutputStream(),true);
										} catch (IOException e2) {
											e2.printStackTrace();
										}
									}
								} else {
									try { // As a client, try to connect to server of other node
										otherSocket = new Socket(otherIP, otherPort);
											System.out.println("Connecting to server on port " + otherPort); 
											System.out.println("Just connected to " + otherSocket.getRemoteSocketAddress()); 
											myStatus = CONNECTED;
											iAmServer = ISNOTSERVER;
										getIn = new BufferedReader(new InputStreamReader(otherSocket.getInputStream()));
										sendOut = new PrintWriter(otherSocket.getOutputStream(),true);
									} catch (IOException e1) { // As a server try to connect to client of other node
										e1.printStackTrace();
										try {
											myServerSocket = new ServerSocket(myPort);
											myServerSocket.setSoTimeout(10000); 
													System.out.println("Waiting for client on port " + myServerSocket.getLocalPort() + "..."); 
													mySocket = myServerSocket.accept();
													System.out.println("Just connected to " + mySocket.getRemoteSocketAddress());  
													myStatus = CONNECTED;
													iAmServer = ISSERVER; // Redundant, just put it here for now
											getIn = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
											sendOut = new PrintWriter(mySocket.getOutputStream(),true);
										} catch (IOException e2) {
											e2.printStackTrace();
										}
									}
								}
		               }
		               // Disconnect from all other nodes
		               else {
		                  connectButton.setEnabled(true);
		                  disconnectButton.setEnabled(false);
		                  sendButton.setEnabled(false);
		                  receiveButton.setEnabled(false);
		                  connectionStatus = DISCONNECTED;
		                  nodeList.setEnabled(false);
		                  msgLine.setText(""); 
		                  msgLine.setEnabled(false);
		                  statusBar.setText("Disconnected");
		                  mainFrame.repaint();
		                  
		                  // Close up everything
			                  try {
								myServerSocket.close();
			                  } catch (IOException e1) {
								e1.printStackTrace();
			                  }
			                  try {
								mySocket.close();
			                  } catch (IOException e1) {
								e1.printStackTrace();
			                  }
			                  try {
								getIn.close();
			                  } catch (IOException e1) {
								e1.printStackTrace();
			                  }
			                  sendOut.close();
		                  
		               }
		            }
		         };
		         connectButton = new JButton("Go Online");
		         connectButton.setPreferredSize(new Dimension(100, 30));
		         connectButton.setActionCommand("connect");
		         connectButton.addActionListener(buttonListener);
		         connectButton.setEnabled(true);
		         disconnectButton = new JButton("Go Offline");
			     disconnectButton.setPreferredSize(new Dimension(100, 30));
			     disconnectButton.setActionCommand("disconnect");
			     disconnectButton.addActionListener(buttonListener);
			     disconnectButton.setEnabled(false);
			     connectPane.add(connectButton);
			     connectPane.add(disconnectButton);
			     infoPane.add(connectPane);
			    
			   // Send/Receive Button
			     JPanel connectPane2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			     connectPane2.setPreferredSize(new Dimension(250,30));
			      messageButtonListener = new ActionAdapter() {
			            public void actionPerformed(ActionEvent e) {
			               // Send message
			               if (e.getActionCommand().equals("send")) {
			                   message = msgLine.getText();
			                   if (!message.equals("")) {
			                	   // Show message to be sent in history panel
			                	   msgText.append("\nSENT > " + message + "\n");
			                	   msgLine.selectAll();
			                	   // Append message to be sent to Send buffer
			                	   toSend.append(message + "\n");
			                	   // Call send method
			                	   send();
			                  }
			                  mainFrame.repaint();
			               }
			               // Receive message
			               else if (e.getActionCommand().equals("receive")){
			            	   receive(); // right now this isn't using the Message class
			            	   message = toReceive.toString();
			            	   if (!message.equals("")) {
			            		   msgText.append(message + "\n");
			            		   toReceive.delete(toReceive.indexOf(message), 
			            				   			toReceive.indexOf(message) + message.length());
			            	   }
			            	   mainFrame.repaint();
			               }
			            }
			         };
			      sendButton = new JButton("Send");
			      sendButton.setPreferredSize(new Dimension(100, 30));
			      sendButton.setActionCommand("send");
			      sendButton.addActionListener(messageButtonListener);
			      sendButton.setEnabled(false);
			      receiveButton = new JButton("Receive");
			      receiveButton.setPreferredSize(new Dimension(100, 30));
			      receiveButton.setActionCommand("receive");
			      receiveButton.addActionListener(messageButtonListener);
			      receiveButton.setEnabled(false);
			      connectPane2.add(sendButton);
			      connectPane2.add(receiveButton);
			      infoPane.add(connectPane2);
			      
			    // Set up the who label
			    whoLabel = new JLabel();
			    whoLabel.setText("Messages sent to ____ :");
			    whoLabel.setPreferredSize(new Dimension(50,20));
			    
			    // Set up the message pane
		    	JPanel messagePane = new JPanel(new BorderLayout());
			        msgText = new JTextArea(10, 20);
			        msgText.setLineWrap(true);
			        msgText.setEditable(false);
			        msgText.setForeground(Color.blue);
			        JScrollPane msgTextPane = new JScrollPane(msgText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			        msgText.append(toReceive.toString());
			        msgLine = new JTextField();
			        msgLine.setEnabled(false);
			        msgLine.setPreferredSize(new Dimension(150,30));
			        messagePane.add(msgTextPane, BorderLayout.CENTER);
			        messagePane.add(whoLabel, BorderLayout.PAGE_START);
			        messagePane.add(msgLine, BorderLayout.PAGE_END);
			        
			    // Set up the main pane
			    JPanel mainPane = new JPanel(new BorderLayout());
			    mainPane.add(statusBar, BorderLayout.SOUTH);
			    mainPane.add(infoPane, BorderLayout.WEST);
			    mainPane.add(messagePane, BorderLayout.CENTER);
		
			    // Set up the main frame
			    mainFrame = new JFrame("Distributed Systems: Lab 0");
			    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    mainFrame.setContentPane(mainPane);
			    mainFrame.setSize(mainFrame.getPreferredSize());
		      	mainFrame.setLocation(200, 200);
		      	mainFrame.pack();
		      	mainFrame.setVisible(true);
	}

	// Send message method
	void send() {
		if (toSend.length() != 0) {
			sendOut.println(toSend);
			sendOut.print(toSend);
			sendOut.flush();
            toSend.setLength(0);
		}
	}
	
	// Receive message method ** Have to update to use Message class eventually
	Message receive() {
		try {
		if(getIn.ready()) {
			String msg = getIn.readLine();
			if ((msg != null) && (msg.length() != 0)) {
				System.out.println(msg);
				toReceive.append(msg);
			}
		}
		} catch (IOException e) {
			System.out.print("IO Exception: Problem receiving a message\n");
			e.printStackTrace();
		}
		// Replace this later
		return null;
	}
	
	
  public static void main(String[] args) {
	  
		// Temporary hard coded information that should be pulled from configuration file
		int portA = 2992;
		int portB = 1991;
		String nameA = "Alice";
		String nameB = "Bob";
		String ipA = "localhost";
		String ipB = "localhost";
		String nodes[] = {	"Alice", "Bob", "Charlie", "Daphnie" };
	  	String arg0 = "config_name";
	  	String arg1 = "local_name";
	  	
	  	// MessagePasser instance #1 (Node Alice) connecting to instance #2 (Node Bob)
	  	// The last parameter is a swap parameter that can equal 0 or 1 -- temporarily dealing with
	  	// setting up a TCP server/client connection simultaneously
		new MessagePasser(arg0, arg1, nameA, ipA, portA, nodes, nameB, ipB, portB, 0);

  }
}

