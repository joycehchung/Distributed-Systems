import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class MessagePasser {
	
	// Servers and sockets
	Socket[] mySockets = new Socket[3];
	ServerSocket myServerSocket;
	Socket[] otherSockets = new Socket[3];
	
	// Sequence number
	int sequenceNumber = 0;
	
	// Status
	public final static int DISCONNECTED = 0;
	public final static int CONNECTED = 1;
	public static int myStatus = DISCONNECTED;
	
	// GUI
	public static JFrame mainFrame = null;
	public static JTextField kindField = null;
	public static JTextArea msgText = null;
	public static JTextField msgLine = null;
	public static JLabel statusBar = null;
	public static JList<String> nodeList = null;
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
	
	// Nodes class
	public static class Node {
		public String name;
		public String ip;
		public int port;
		public BufferedReader[] inbox = new BufferedReader[3];
		public PrintWriter[] outbox = new PrintWriter[3];
		public BufferedReader[] inboxC = new BufferedReader[3];
		public PrintWriter[] outboxC = new PrintWriter[3];
	}
	
	public Node nodeME = new Node();
	public static ArrayList<Node> otherNodes = new ArrayList<Node>();
	public static String[] nodes = new String[3];
	public String whoString = null;
	public int numNodes = 0;
	public int nodeNumber = 0;
	
	// Java Swing ActionAdapter class for handling events (like button presses)
	// Information from http://www.lamatek.com/lamasoft/javadocs/swingextras/com/lamatek/event/ActionAdapter.html
	public static class ActionAdapter implements ActionListener {
	   public void actionPerformed(ActionEvent e) {}
	}
	
	// As a server, connect to n client nodes
	public int ConnectToClients(int i) {
		try {

			statusBar.setText("Waiting for client ... ");
			mainFrame.repaint();
			
			mySockets[i] = myServerSocket.accept();
			statusBar.setText("Connected to " + mySockets[i].getRemoteSocketAddress());
			mainFrame.repaint();
			
			myStatus = CONNECTED;
			nodeME.inbox[i] = new BufferedReader(new InputStreamReader(mySockets[i].getInputStream()));
			nodeME.outbox[i] = new PrintWriter(mySockets[i].getOutputStream(),true);
			
		} catch (IOException e1) { 
			e1.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	// As a client, connect to n server nodes
	public int ConnectToServers(int i) {
		try {
			otherSockets[i] = new Socket(otherNodes.get(i).ip, otherNodes.get(i).port);
				System.out.println("Connecting to server on port " + otherNodes.get(i).port); 
				System.out.println("Just connected to " + otherSockets[i].getRemoteSocketAddress()); 
				myStatus = CONNECTED;
			nodeME.inboxC[i] = new BufferedReader(new InputStreamReader(otherSockets[i].getInputStream()));
			nodeME.outboxC[i] = new PrintWriter(otherSockets[i].getOutputStream(),true);
			
		} catch(UnknownHostException unknownHost) {
            System.err.println("Unknown Host (Server)");
            return 0;
		} catch(ConnectException ce) {
            System.err.println("Server cannot be found ... May not be online yet");
            return 0;
		} catch (IOException e1) {
			e1.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	// Connect to other nodes function
	public void MakeConnections(int numNodes, int nodeNum) {
		int sum = 0;
		int s0 = 0; int s1 = 0; int s2 = 0;
		while (sum != numNodes) {
			if (nodeNum == 1) {
				if (s0 == 0) { s0 = ConnectToClients(0); }
				else if (s0 == 1 && s1 == 0) { s1 = ConnectToClients(1); }
				else if (s0 == 1 && s1 == 1 && s2 == 0) { s2 = ConnectToClients(2); }
				sum = s0 + s1 + s2;
			} else if (nodeNum == 2) {
				if (s0 == 0) { s0 = ConnectToServers(0); }
				else if (s0 == 1 && s1 == 0) { s1 = ConnectToClients(0); }
				else if (s0 == 1 && s1 == 1 && s2 == 0) { s2 = ConnectToClients(1); }
				sum = s0 + s1 + s2;
			} else if (nodeNum == 3) {
				if (s0 == 0) { s0 = ConnectToServers(0); }
				else if (s0 == 1 && s1 == 0) { s1 = ConnectToServers(1); }
				else if (s0 == 1 && s1 == 1 && s2 == 0) { s2 = ConnectToClients(0); }
				sum = s0 + s1 + s2;
			} else { 
				if (s0 == 0) { s0 = ConnectToServers(0); }
				else if (s0 == 1 && s1 == 0) { s1 = ConnectToServers(1); }
				else if (s0 == 1 && s1 ==1 && s2 == 0) { s2 = ConnectToServers(2); }
				sum = s0 + s1 + s2;
			}
		}
	}
	
	// Send information to other nodes
	public void SendInformation() {
		switch(nodeNumber) {
	 	   case 1: 
	 		   		nodeME.outbox[0].println(nodeME.name);
					nodeME.outbox[0].flush();
	 		   		nodeME.outbox[1].println(nodeME.name);
					nodeME.outbox[1].flush();
	 		   		nodeME.outbox[2].println(nodeME.name);
					nodeME.outbox[2].flush();
					break;	
	 	   case 2:
	 		   		nodeME.outbox[0].println(nodeME.name);
	 		   		nodeME.outbox[0].flush();
	 		   		nodeME.outbox[1].println(nodeME.name);
	 		   		nodeME.outbox[1].flush();
	 		   		nodeME.outboxC[0].println(nodeME.name);
	 		   		nodeME.outboxC[0].flush();
	 		   		break; 		   
	 	   case 3: 
			   		nodeME.outbox[0].println(nodeME.name);
			   		nodeME.outbox[0].flush();
			   		nodeME.outboxC[0].println(nodeME.name);
			   		nodeME.outboxC[0].flush();
			   		nodeME.outboxC[1].println(nodeME.name);
			   		nodeME.outboxC[1].flush();
			   		break;		 		   
	 	   case 4: 
			   		nodeME.outboxC[0].println(nodeME.name);
			   		nodeME.outboxC[0].flush();
			   		nodeME.outboxC[1].println(nodeME.name);
			   		nodeME.outboxC[1].flush();
			   		nodeME.outboxC[2].println(nodeME.name);
			   		nodeME.outboxC[2].flush();
			   		break;
	   	}	
	}
	
	// Receive information from other nodes
	public void ReceiveInformation() {
		try {
			switch(nodeNumber) {
		 	   case 1: 
		 		   		nodes[0] = nodeME.inbox[0].readLine();
		 		   		nodes[1] = nodeME.inbox[1].readLine();
		 		   		nodes[2] = nodeME.inbox[2].readLine();
						break;	
		 	   case 2:
	 		   			nodes[0] = nodeME.inboxC[0].readLine();
		 		   		nodes[1] = nodeME.inbox[0].readLine();
		 		   		nodes[2] = nodeME.inbox[1].readLine();
		 		   		break; 		   
		 	   case 3: 
				   		nodes[0] = nodeME.inboxC[0].readLine();
				   		nodes[1] = nodeME.inboxC[1].readLine();
				   		nodes[2] = nodeME.inbox[0].readLine();
				   		break;		 		   
		 	   case 4: 
				   		nodes[0] = nodeME.inboxC[0].readLine();
				   		nodes[1] = nodeME.inboxC[1].readLine();
				   		nodes[2] = nodeME.inboxC[2].readLine();
				   		break;
		   	}	
		} catch (IOException e) {
			System.out.println("Problem receiving node List");
		}		
	}
	
	// Message Passer constructor
	public MessagePasser(String configuration_filename, String local_name) {
		
			// Get rid of this once YAML parser is done
			Node nodeA = new Node(); nodeA.name = "alice"; nodeA.ip = "localhost"; nodeA.port = 12344;
			Node nodeB = new Node(); nodeB.name = "bob"; nodeB.ip = "localhost"; nodeB.port = 14255;
			Node nodeC = new Node(); nodeC.name = "charlie"; nodeC.ip = "localhost"; nodeC.port = 12998;
			Node nodeD = new Node(); nodeD.name = "daphnie"; nodeD.ip = "localhost"; nodeD.port = 1987;
			numNodes = 3;
			
			// Get my information (nodeME) and otherNodes information
			if (local_name.equals(nodeA.name)) {
				nodeME = nodeA;
				otherNodes.add(nodeB); otherNodes.add(nodeC); otherNodes.add(nodeD);
				nodes[0] = nodeB.name; nodes[1] = nodeC.name; nodes[2] = nodeD.name;
				nodeNumber = 1;
			} else if (local_name.equals(nodeB.name)) {
				nodeME = nodeB;
				otherNodes.add(nodeA); otherNodes.add(nodeC); otherNodes.add(nodeD);
				nodes[0] = nodeA.name; nodes[1] = nodeC.name; nodes[2] = nodeD.name;
				nodeNumber = 2;
			} else if (local_name.equals(nodeC.name)) {
				nodeME = nodeC;
				otherNodes.add(nodeA); otherNodes.add(nodeB); otherNodes.add(nodeD);
				nodes[0] = nodeA.name; nodes[1] = nodeB.name; nodes[2] = nodeD.name;
				nodeNumber = 3;
			} else if (local_name.equals(nodeD.name)) {
				nodeME = nodeD;
				otherNodes.add(nodeA); otherNodes.add(nodeB); otherNodes.add(nodeC);
				nodes[0] = nodeA.name; nodes[1] = nodeB.name; nodes[2] = nodeC.name;
				nodeNumber = 4;
			}
			// Set up server socket
			try {
				myServerSocket = new ServerSocket(nodeME.port);
			} catch (IOException e) {
				System.out.println("Problem setting up server socket");
			}
			
		
			// Set up GUI
			    // Set up the status bar
			    statusBar = new JLabel();
			    statusBar.setText("Disconnected");

			    // Node Information Pane
			    JPanel pane = null;
		    	JPanel infoPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    	infoPane.setPreferredSize(new Dimension(250, 500));
		    	pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    	pane.setPreferredSize(new Dimension(250, 20));
		    	pane.add(new JLabel("My Name:"));
		    		myNameLabel = new JLabel();
	    			myNameLabel.setText(nodeME.name); 
	    			pane.add(myNameLabel);
	    			infoPane.add(pane);
    			pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    			pane.setPreferredSize(new Dimension(250, 20));
    			pane.add(new JLabel("My IP:"));
    				myIpLabel = new JLabel();	
					myIpLabel.setText(nodeME.ip);
					pane.add(myIpLabel);
					infoPane.add(pane);
				pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
				pane.setPreferredSize(new Dimension(250, 20));
				pane.add(new JLabel("My Port:"));
					myPortLabel = new JLabel();	
					myPortLabel.setText((new Integer(nodeME.port)).toString());
					pane.add(myPortLabel);
					infoPane.add(pane);
					
					
			    // Set up the kind text area
				JLabel emptyLabel = new JLabel();
				emptyLabel.setText("____________________________________");
				JLabel messageInfoLabel = new JLabel();
				messageInfoLabel.setText("MESSAGE OPTIONS:");
				JLabel kindLabel = new JLabel();
				kindLabel.setText("Kind :");
				kindLabel.setPreferredSize(new Dimension(150,30)); 
				kindField = new JTextField();
				kindField.setEnabled(false);
				kindField.setPreferredSize(new Dimension(150,30)); 
				infoPane.add(emptyLabel);
				infoPane.add(messageInfoLabel);
				infoPane.add(kindLabel);
				infoPane.add(kindField);
						
				// Node List
				JPanel nodeListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				nodeListPanel.setPreferredSize(new Dimension(250, 80));
				pane = new JPanel(new FlowLayout(FlowLayout.CENTER));
				pane.setPreferredSize(new Dimension(250, 20));
		    	pane.add(new JLabel("Connected Nodes:"));
				infoPane.add(pane);
				nodeList = new JList<String>(nodes);
				nodeList.setEnabled(false);
				nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				nodeListPanel.add(nodeList);
				infoPane.add(nodeListPanel);
				listListener = new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent arg0) {
						whoString = nodeList.getSelectedValue().toString();
						whoLabel.setText("SENDING MESSAGE TO: " + nodeList.getSelectedValue().toString());
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
		                  kindField.setEnabled(true);
		                  statusBar.setText("Connected");
		                  mainFrame.repaint();
		                  
		                  // Connect Function
		                  MakeConnections(numNodes, nodeNumber);	               
		                  
		                  // Send messages indicating identity
		                  SendInformation();
		                  
		                  // Receive messages indicating identities
		                  ReceiveInformation();
		                  mainFrame.repaint();
		               }
		               
		               // Disconnect from all other nodes
		               else {
		                  connectButton.setEnabled(true);
		                  disconnectButton.setEnabled(false);
		                  sendButton.setEnabled(false);
		                  receiveButton.setEnabled(false);
		                  connectionStatus = DISCONNECTED;
		                  nodeList.setEnabled(false);
		                  kindField.setEnabled(false);
		                  msgLine.setText(""); 
		                  msgLine.setEnabled(false);
		                  statusBar.setText("Disconnected");
		                  mainFrame.repaint();
		                  
		                  // Close up everything
		                  if (myServerSocket != null) {
			                  try {
								myServerSocket.close();
			                  } catch (IOException e1) {
								e1.printStackTrace();
			                  }
		                  } else {
			                  try {
								mySockets[0].close();
			                  } catch (IOException e1) {
								e1.printStackTrace();
			                  }
		                  }
		                  
		                  try {
							nodeME.inbox[0].close();
		                  } catch (IOException e1) {
							e1.printStackTrace();
		                  }
		                  nodeME.outbox[0].close();
		                  
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
			            	   
			            	   // Create message
			            	   String destination = nodeList.getSelectedValue().toString();
			            	   String kind = kindField.getText();
			            	   Object data = msgLine.getText();
			            	   message = data.toString();
			            	   msgLine.setText("");
			            	   kindField.setText("");
			            	   
			            	   if (!message.equals("")) {
				            	   Message theMessage = new Message(destination, kind, data);
				            	   theMessage.set_seqNum(sequenceNumber);
				            	   theMessage.set_source(nodeME.name);
				            	   sequenceNumber++;
			                	   send(theMessage); 
			                	   mainFrame.repaint();
			            	   }
			               }
			               // Receive message
			               else if (e.getActionCommand().equals("receive")){
			            	   receive(); // right now this isn't using the Message class
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
			    whoLabel.setText("Messages History :");
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
	public void send(Message theMessage) {
 	   int rnum = nodeList.getSelectedIndex();
 	   String rname = nodeList.getSelectedValue().toString();
       //message = msgLine.getText();
       //msgLine.setText("");
       
	    if (!message.equals("")) {
	    	
	 	   // Show message to be sent in history panel
	 	   msgText.append("\nSENT to " + rname + " > " + message + "\n");
	 	   //msgLine.selectAll();
	 	   
	 	   // Append message to be sent to Send buffer
	 	   toSend.setLength(0);
	 	   toSend.append(message + "\r\n");
	 	   
	 	   switch(nodeNumber) {
		 	   case 1:
		 		   		if (rnum <= 2) {
			 		   		nodeME.outbox[rnum].println(theMessage);
							nodeME.outbox[rnum].flush();
		 		   		}
						break;
		 	   case 2:
			 		   if (rnum == 0) {	  
			 			   	nodeME.outboxC[rnum].println(theMessage);
		 			   		nodeME.outboxC[rnum].flush();
			 		   } else { 
			 			   	nodeME.outbox[rnum-1].println(theMessage);
			 			   	nodeME.outbox[rnum-1].flush();
			 		   }
			 		   break;
		 	   case 3: 
			 		   if (rnum <= 1) {
			 			   	nodeME.outboxC[rnum].println(theMessage);
			 			   	nodeME.outboxC[rnum].flush();
			 		   } else {
			 			   	nodeME.outbox[0].println(theMessage);
			 			   	nodeME.outbox[0].flush();
			 		   }
			 		   break;
		 	   case 4:
			 		   if (rnum <= 2) {
			 			   	nodeME.outboxC[rnum].println(theMessage);
			 			   	nodeME.outboxC[rnum].flush();
			 		   }
			 		   break;
	 	   	}
	    }
	}
	
	// Receive message method ** Have to update to use Message class eventually
	public Message receive() {
		String msg = null;
	
		String dest = null;
		String src = null;
		int sNum = 0;
		String knd = null;
		Object dta = null;
		Message gotMessage = new Message(dest, knd, dta);
		
		try {
			if (nodeME.inbox[0] != null && nodeME.inbox[0].ready()) {
				msg = nodeME.inbox[0].readLine();
					dest = msg.substring(msg.indexOf("to:")+3, msg.indexOf(" Seq:"));
					src = msg.substring(5, msg.indexOf(" to:"));
					sNum = Integer.parseInt(msg.substring(msg.indexOf("Seq:")+4, msg.indexOf(" Kind:")));
					knd = msg.substring(msg.indexOf("Kind:")+5, msg.indexOf(" Data:"));
					dta = msg.substring(msg.indexOf("Data:")+5, msg.length());
				gotMessage.set_destination(dest);
				gotMessage.set_source(src);
				gotMessage.set_seqNum(sNum);
				gotMessage.set_kind(knd);
				gotMessage.set_data(dta);

				toReceive.append(msg + "\n");
			} 

			if (nodeME.inbox[1] != null && nodeME.inbox[1].ready()) {
				msg = nodeME.inbox[1].readLine(); 
				System.out.println(msg);
				toReceive.append(msg + "\n");
			}
			
			if (nodeME.inbox[2] != null && nodeME.inbox[2].ready()) {
				msg = nodeME.inbox[2].readLine(); 
				System.out.println(msg);
				toReceive.append(msg + "\n");
			}
			
			if (nodeME.inboxC[0] != null && nodeME.inboxC[0].ready()) {
				msg = nodeME.inboxC[0].readLine();
				System.out.println(msg);
				toReceive.append(msg + "\n");
			} 

			if (nodeME.inboxC[1] != null && nodeME.inboxC[1].ready()) {
				msg = nodeME.inboxC[1].readLine(); 
				System.out.println(msg);
				toReceive.append(msg + "\n");
			}
			
			if (nodeME.inboxC[2] != null && nodeME.inboxC[2].ready()) {
				msg = nodeME.inboxC[2].readLine(); 
				System.out.println(msg);
				toReceive.append(msg + "\n");
			}
			
			if (toReceive.length() != 0) {
				String a[] = toReceive.toString().split("\n"); 
				String outMessage = a[0];
				msgText.append(outMessage + "\n");
				toReceive.delete(toReceive.indexOf(outMessage), toReceive.indexOf(outMessage) + outMessage.length()+1);
			}
			
		} catch (IOException e) {
			System.out.print("IO Exception: Problem receiving a message\n");
			e.printStackTrace();
		}

		return gotMessage;
	}
	
	
  public static void main(String[] args) {
	  
	  //** UNCOMMENT THIS LINE to run from command line 
	  	//new MessagePasser(args[0], args[1]);
	  
	  // Use this version if running from within eclipse
	  new MessagePasser("config_file", "bob");
  }
}

