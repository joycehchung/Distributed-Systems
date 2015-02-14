import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
	
    // Sockets
    Socket[] mySockets = new Socket[3];
    ServerSocket myServerSocket;
    Socket[] otherSockets = new Socket[3];
    
    // Sequence number
    int sequenceNumber = 0;
    
    // Rules
    public final static int NONE = 0;
    public final static int DROP = 1;
    public final static int DUPLICATE = 2;
    public final static int DELAY = 3;
    public final static int CHECKSEND = 0;
    public final static int CHECKRECEIVE = 1;
    public static int sendRule = NONE;
    public static int receiveRule = NONE;
    public static int sendDelay = 0;
    public static int receiveDelay = 0;
	public static ArrayList<Rule> SendRules = new ArrayList<Rule>();
	public static ArrayList<Rule> ReceiveRules = new ArrayList<Rule>();
    
    // Status
    public final static int DISCONNECTED = 0;
    public final static int CONNECTED = 1;
    public static int myStatus = DISCONNECTED;
    public static String selectedName = null;
    public static int selectedIndex = 0;
    
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
    public static JButton logicalButton = null;
    public static JButton vectorButton = null;
    public static JButton timeStampButton = null;
    public static JButton connectButton = null;
    public static JButton disconnectButton = null;
    public static JButton sendButton = null;
    public static JButton receiveButton = null;
    public static JButton logButton = null;
    public static JLabel whoLabel = null;
    public static int connectionStatus = DISCONNECTED;
    public static ActionAdapter buttonListener = null;
    public static ActionAdapter clockButtonListener = null;
    public static ActionAdapter timeButtonListener = null;
    public static ListSelectionListener listListener = null;
    public static ActionAdapter messageButtonListener = null;
    
    // Nodes
    public static Node nodeME = new Node();
    public static ArrayList<Node> otherNodes = new ArrayList<Node>();
    public static String[] nodes = new String[3];
    public static Node nodeA = new Node(); 
    public static Node nodeB = new Node(); 
    public static Node nodeC = new Node(); 
    public static Node nodeD = new Node();
    
    // Groups
    public static ArrayList<String> groups = new ArrayList<String>();
    public static ArrayList<Group> myGroups = new ArrayList<Group>();
    
    // ClockService
    public ClockService clockService = null;
    
    // Logging
    public static TimeStampedMessage currentMessage = null;
    public static TimeStampedMessage logMessage = null;
    
    // Configuration File
	public static URL configFileUrl;
	public static HttpURLConnection connection;
	public static BufferedInputStream bufferInputStream;
    
    // Handle Java Swing events
    public static class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {}
    }
    
    // As a server, connect to client nodes
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
            
            statusBar.setText("Connected to " + otherSockets[i].getRemoteSocketAddress());
            mainFrame.repaint();
            
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
    
    // Connect to other nodes function (some logic here)
    public void MakeConnections() {
        int sum = 0;
        int s0 = 0; int s1 = 0; int s2 = 0;
        while (sum != nodeME.numNodes) {
            if (nodeME.nodeNumber == 1) {
                if (s0 == 0) { s0 = ConnectToClients(0); }
                else if (s0 == 1 && s1 == 0) { s1 = ConnectToClients(1); }
                else if (s0 == 1 && s1 == 1 && s2 == 0) { s2 = ConnectToClients(2); }
                sum = s0 + s1 + s2;
            } else if (nodeME.nodeNumber == 2) {
                if (s0 == 0) { s0 = ConnectToServers(0); }
                else if (s0 == 1 && s1 == 0) { s1 = ConnectToClients(0); }
                else if (s0 == 1 && s1 == 1 && s2 == 0) { s2 = ConnectToClients(1); }
                sum = s0 + s1 + s2;
            } else if (nodeME.nodeNumber == 3) {
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
        switch(nodeME.nodeNumber) {
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
            switch(nodeME.nodeNumber) {
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
            System.out.println("Connected to: " + nodes[0] + ", " + nodes[1] + ", " + nodes[2]);
        } catch (IOException e) {
            System.out.println("Problem receiving node List");
        }
    }
    	    
	// Load Configuration File Function	
	@SuppressWarnings("unchecked")
	public static void LoadConfig(String conf_filename, Object local_name) {
		try	{
			
			// URL of config file
			configFileUrl = new URL("https://www.dropbox.com/s/iombuj3et0qd9sb/config?dl=1");
			
			// Setup connection
		    HttpURLConnection.setFollowRedirects(true);
		    connection = (HttpURLConnection) configFileUrl.openConnection();
		    connection.setDoOutput(false);
		    connection.setReadTimeout(20000);
		    connection.setRequestProperty("Connection", "keep-alive");
		    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		    ((HttpURLConnection) connection).setRequestMethod("GET");
		    connection.setConnectTimeout(5000);
			    
		    // Use Buffer to get characters from file
		    bufferInputStream = new BufferedInputStream(connection.getInputStream());
		    
			HashMap<String, User> users = new HashMap<String, User>();
			
			//FileInputStream fileInput = new FileInputStream(conf_filename);
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(bufferInputStream);
			
			// Get Configuration Information
			ArrayList<HashMap<String, Object> > config = (ArrayList<HashMap<String, Object> >)data.get("configuration");

			int i = 0;
			
			for(HashMap<String, Object> row : config)
			{
				String Name = (String)row.get("name");
				User usr = new User(Name);
				usr.setIp((String)row.get("ip"));
				usr.setPort((Integer)row.get("port"));
				users.put(Name, usr);
				System.out.println(usr.toString());
					switch (i) {
					case 0: nodeA.name = usr.getName(); nodeA.ip = usr.getIp(); nodeA.port = usr.getPort();
					case 1: nodeB.name = usr.getName(); nodeB.ip = usr.getIp(); nodeB.port = usr.getPort();
					case 2: nodeC.name = usr.getName(); nodeC.ip = usr.getIp(); nodeC.port = usr.getPort();
					case 3: nodeD.name = usr.getName(); nodeD.ip = usr.getIp(); nodeD.port = usr.getPort();
					}
				i++;
			}
			nodeME.numNodes = i-1;

			if(!users.containsKey(local_name))
			{
				System.err.println("local_name: " + local_name + " isn't in " + conf_filename + ", please check again!");
				System.exit(1);
			}
			
			// Get Groups

			
			// Get Send Rules
			ArrayList<HashMap<String, Object> > send_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("sendRules");
			
			for(HashMap<String, Object> send_rule : send_rule_arr)
			{
				String action = (String)send_rule.get("action");
				Rule r = new Rule(action);
				for(String key: send_rule.keySet())
				{
					if(key.equals("src"))
						r.set_source((String)send_rule.get(key));
					if(key.equals("dest"))
						r.set_destination((String)send_rule.get(key));
					if(key.equals("kind"))
						r.set_kind((String)send_rule.get(key));
					if(key.equals("seqNum"))
						r.set_seqNum((Integer)send_rule.get(key));

				}
				SendRules.add(r);
			}
			SendRules.toString();
			System.out.println(SendRules.toString());
			ArrayList<HashMap<String, Object> > receive_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("receiveRules");
			for(HashMap<String, Object> receive_rule : receive_rule_arr)
			{
				String action = (String)receive_rule.get("action");
				Rule r = new Rule(action);
				for(String key: receive_rule.keySet())
				{
					if(key.equals("src"))
						r.set_source((String)receive_rule.get(key));
					if(key.equals("dest"))
						r.set_destination((String)receive_rule.get(key));
					if(key.equals("kind"))
						r.set_kind((String)receive_rule.get(key));
					if(key.equals("seqNum"))
						r.set_seqNum((Integer)receive_rule.get(key));
				}
				ReceiveRules.add(r);
			}
			ReceiveRules.toString();
			System.out.println(ReceiveRules.toString());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	// Update Rules from Configuration File 
	@SuppressWarnings("unchecked")
	public void UpdateRules() throws IOException{
		// Clear old rules
		ReceiveRules.clear();
		SendRules.clear();
		
		// URL of configuration file
		configFileUrl = new URL("https://www.dropbox.com/s/iombuj3et0qd9sb/config?dl=1");
		
		// Setup connection
	    HttpURLConnection.setFollowRedirects(true);
	    connection = (HttpURLConnection) configFileUrl.openConnection();
	    connection.setDoOutput(false);
	    connection.setReadTimeout(10000);
	    connection.setRequestProperty("Connection", "keep-alive");
	    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
	    ((HttpURLConnection) connection).setRequestMethod("GET");
	    connection.setConnectTimeout(10000);
		    
	    // Use Buffer to get characters from file
	    bufferInputStream = new BufferedInputStream(connection.getInputStream());
	    
	    try	{
	    	// Use YAML to parse data from file
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(bufferInputStream);

			// Get Send Rules
			ArrayList<HashMap<String, Object> > send_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("sendRules");			
				for(HashMap<String, Object> send_rule : send_rule_arr)
				{
					String action = (String)send_rule.get("action");
					Rule r = new Rule(action);
					for(String key: send_rule.keySet())
					{
						if(key.equals("src"))
							r.set_source((String)send_rule.get(key));
						if(key.equals("dest"))
							r.set_destination((String)send_rule.get(key));
						if(key.equals("kind"))
							r.set_kind((String)send_rule.get(key));
						if(key.equals("seqNum"))
							r.set_seqNum((Integer)send_rule.get(key));
	
					}
					SendRules.add(r);
				}
				System.out.println(SendRules.toString());
			
			// Get Receive Rules
			ArrayList<HashMap<String, Object> > receive_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("receiveRules");
				for(HashMap<String, Object> receive_rule : receive_rule_arr)
				{
					String action = (String)receive_rule.get("action");
					Rule r = new Rule(action);
					for(String key: receive_rule.keySet())
					{
						if(key.equals("src"))
							r.set_source((String)receive_rule.get(key));
						if(key.equals("dest"))
							r.set_destination((String)receive_rule.get(key));
						if(key.equals("kind"))
							r.set_kind((String)receive_rule.get(key));
						if(key.equals("seqNum"))
							r.set_seqNum((Integer)receive_rule.get(key));
					}
					ReceiveRules.add(r);
				}
				System.out.println(ReceiveRules.toString());
		}
		catch(Exception ex)
		{
			System.err.println("Error updating rules.");
		}
	}
	
	// Check Rule function
	public int CheckRule(TimeStampedMessage message, int type) {
		
		// Update rules by re-reading config file if needed
		try {
			UpdateRules();
		} catch (IOException e) {
			System.err.println("Error updating rules!!!");
		}
		
		int ret = 0;
		
		ArrayList<Rule> rule_arr = null;
		if(type == 0)
			rule_arr = SendRules;
		else if(type == 1)
			rule_arr = ReceiveRules;
		else
		{
			System.err.println("error use of CheckRule with type = " + type);
			System.exit(1);
		}
		for(Rule rule: rule_arr)
		{
			if((rule.get_source() != null) && !(rule.get_source().equals(message.get_source())))
				continue;
			else if((rule.get_destination() != null) && !(rule.get_destination().equals(message.get_destination())))
				continue;
			else if((rule.get_kind() != null) && !(rule.get_kind().equals(message.get_kind())))
				continue;
			else if((rule.get_seqNum() != null) && !(rule.get_seqNum().equals(message.get_seqNum())))
				continue;
	
	
	
			rule.addMatch(); // already matched rule!
			// Get rule action
			if (rule.get_action().equals("drop")) {
				ret = 1;
			} else if (rule.get_action().equals("duplicate")) {
				ret = 2;
			} else if (rule.get_action().equals("delay")) {
				ret = 3;
			} else {
				ret = 0;
			}
			System.out.println("RULE: " + rule.toString());
			return (ret);  // match this rule
		}
		return (0);  // if no rules match, return null
	}	

	// Get send node index
	public int GetSendNodeIndex(String name) {
		
		int index = 0;
		if (name.equals(nodes[0])) {
			index = 0;
		} else if (name.equals(nodes[1])) {
			index = 1;
		} else if (name.equals(nodes[2])) {
			index = 2;
		} else {
			index = 100;
		}
		System.out.println("Sending to: " + name + " " + index);
		return index;
	}
	
	// Setup GUI function
	public void SetupGui() {
        // 	Status bar
	        statusBar = new JLabel();
	        statusBar.setText("Disconnected");
        
        // 	Node Information Pane
	        JPanel pane = null;
	        JPanel infoPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        infoPane.setPreferredSize(new Dimension(320, 500));
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
        
        // 	TimeStampedMessage Options Heading Text
	        JLabel emptyLabel = new JLabel();
	        emptyLabel.setText("____________________________________");
	        JLabel messageInfoLabel = new JLabel();
	        messageInfoLabel.setText("MESSAGE OPTIONS:");
	        messageInfoLabel.setPreferredSize(new Dimension(300,30));
	        
        // 	TimeStampedMessage Kind Text Area
	        JLabel kindLabel = new JLabel();
	        kindLabel.setText("Kind :");
	        kindLabel.setPreferredSize(new Dimension(150,30));
	        kindField = new JTextField();
	        kindField.setEnabled(false);
	        kindField.setPreferredSize(new Dimension(170,30));
	        infoPane.add(emptyLabel);
	        infoPane.add(messageInfoLabel);
	        infoPane.add(kindLabel);
	        infoPane.add(kindField);
        
        // 	Node List (of Nodes and Groups to send message to)
	        JPanel nodeListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        nodeListPanel.setPreferredSize(new Dimension(250, 80));
	        pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        pane.setPreferredSize(new Dimension(250, 20));
	        pane.add(new JLabel("Connected Nodes:"));
	        infoPane.add(pane);
	        // Combine nodes and groups lists
	        ArrayList<String> namesList = new ArrayList<String>();
	        namesList.addAll(Arrays.asList(nodes));
	        namesList.addAll(groups);
	        nodeList = new JList<String>((String[]) namesList.toArray());
	        nodeList.setEnabled(false);
	        nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        nodeListPanel.add(nodeList);
	        infoPane.add(nodeListPanel);
	        // Selection listener that displays who the message will be sent to
	        listListener = new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent arg0) {
	            	selectedName = nodeList.getSelectedValue().toString();
	            	selectedIndex = nodeList.getSelectedIndex();
	                whoLabel.setText("SENDING MESSAGE TO: " + selectedName + " (" + selectedIndex + ")");
	            }
	        };
	        nodeList.addListSelectionListener(listListener);
        
        //	Clock Pane and Logical/Vector Clock Radio Buttons
	        JPanel clockPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        clockPane.setPreferredSize(new Dimension(250,30));
			clockButtonListener = new ActionAdapter() {
	            public void actionPerformed(ActionEvent e) {
	            	if (e.getActionCommand().equals("logical")) {
	            		clockService = ClockService.CreateClockService(ClockService.ClockTypes.LOGICAL, nodes, nodeME.name);
	            		connectButton.setEnabled(true);
	            		timeStampButton.setEnabled(true);
	            		logicalButton.setEnabled(false);
	            		vectorButton.setEnabled(false);
	            	} else if (e.getActionCommand().equals("vector")) {
	            		clockService = ClockService.CreateClockService(ClockService.ClockTypes.VECTOR, nodes, nodeME.name);
	            		connectButton.setEnabled(true);
	            		timeStampButton.setEnabled(true);
	            		logicalButton.setEnabled(false);
	            		vectorButton.setEnabled(false);
	            	} else {
	            		System.err.println("Error in Clock selection");
	            	}
	            }
		    };
			logicalButton = new JButton("Logical Clock");
			logicalButton.setPreferredSize(new Dimension(120, 30));
			logicalButton.setActionCommand("logical");
			logicalButton.addActionListener(clockButtonListener);
			if (nodeME.name.equals("logger")) {
				// Change back to false later!!
				logicalButton.setEnabled(true);
			} else {
				logicalButton.setEnabled(true);
			}
			vectorButton = new JButton("Vector Clock");
			vectorButton.setPreferredSize(new Dimension(120, 30));
			vectorButton.setActionCommand("vector");
			vectorButton.addActionListener(clockButtonListener);
			vectorButton.setEnabled(true);
			clockPane.add(logicalButton);
			clockPane.add(vectorButton);
			infoPane.add(clockPane);
			
	        JPanel timePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        timePane.setPreferredSize(new Dimension(310,30));
	        JLabel timeLabel = new JLabel();
	        timeLabel.setText("TimeStamp :");
	        timeLabel.setPreferredSize(new Dimension(310,30));
			timeButtonListener = new ActionAdapter() {
	            public void actionPerformed(ActionEvent e) {
	            	if (e.getActionCommand().equals("timestamp")) {
	            		timeLabel.setText("TimeStamp: " + clockService.get_clockTimeStamp().ts);
	            		// Can add this back if we want this to be an event
	            		clockService.updateTimeStamp();
	            	}
	            }
		    };
			timeStampButton = new JButton("Get TimeStamp");
			timeStampButton.setPreferredSize(new Dimension(250, 30));
			timeStampButton.setActionCommand("timestamp");
			timeStampButton.addActionListener(timeButtonListener);
			timeStampButton.setEnabled(false);
			infoPane.add(timeLabel);
			infoPane.add(timeStampButton);
	        
        //	Begin/End Connections Button
	        JPanel connectPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        connectPane.setPreferredSize(new Dimension(250,30));
	        buttonListener = new ActionAdapter() {
	            public void actionPerformed(ActionEvent e) {
	                
	                // Connect to other nodes
	                if (e.getActionCommand().equals("connect")) {
			                  connectButton.setEnabled(false);
			                  disconnectButton.setEnabled(true);
			                  connectionStatus = CONNECTED;
			                  statusBar.setText("Connected");
			                  mainFrame.repaint();
			                  
			                  // Connect Function
			                  MakeConnections();
			                  
			                  // Send messages indicating identity
			                  SendInformation();
			                  
			                  // Receive messages indicating identities
			                  ReceiveInformation();
			                  
			                  if (nodeME.name.equals("logger")) {
				                  sendButton.setEnabled(false); 
				                  receiveButton.setEnabled(false);
				                  logButton.setEnabled(false);
				                  nodeList.setEnabled(false);
				                  msgLine.setEnabled(false);
				                  kindField.setEnabled(false);
			                  } else {
				                  sendButton.setEnabled(true); 
				                  receiveButton.setEnabled(true);
				                  logButton.setEnabled(true);
				                  nodeList.setEnabled(true);
				                  msgLine.setEnabled(true);
				                  kindField.setEnabled(true);
			                  }
			                  
			                  mainFrame.repaint();
	                }
	                
	                // Disconnect from all other nodes
	                else {
			                  connectButton.setEnabled(true);
			                  disconnectButton.setEnabled(false);
			                  connectionStatus = DISCONNECTED;
			                  statusBar.setText("Disconnected");
			                  sendButton.setEnabled(false);
			                  receiveButton.setEnabled(false);
			                  logButton.setEnabled(false);
			                  nodeList.setEnabled(false);
			                  kindField.setEnabled(false);
			                  msgLine.setText("");
			                  msgLine.setEnabled(false); 
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
	        connectButton.setEnabled(false);
	        disconnectButton = new JButton("Go Offline");
			disconnectButton.setPreferredSize(new Dimension(100, 30));
			disconnectButton.setActionCommand("disconnect");
			disconnectButton.addActionListener(buttonListener);
			disconnectButton.setEnabled(false);
			connectPane.add(connectButton);
			connectPane.add(disconnectButton);
			infoPane.add(connectPane);
        
        // 	Send/Receive TimeStampedMessage Button
			JPanel connectPane2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			connectPane2.setPreferredSize(new Dimension(250,30));
	        messageButtonListener = new ActionAdapter() {
	            public void actionPerformed(ActionEvent e) {
	                // Send message
	                if (e.getActionCommand().equals("send")) {
	                    // Create message
	                	TimeStampedMessage theMessage = new TimeStampedMessage(selectedName, kindField.getText(), msgLine.getText());
                        theMessage.set_seqNum(sequenceNumber); 
                        sequenceNumber++;
                        theMessage.set_source(nodeME.name);
                        theMessage.set_nodeIndex(GetSendNodeIndex(selectedName));
                        theMessage.set_timeStamp(clockService.get_sendTimeStamp());
	                    // Clear GUI text fields
	                    msgLine.setText("");
	                    kindField.setText("");
	                    currentMessage = theMessage;
	                    send(theMessage);
	                    // Update clock
	            		clockService.updateTimeStamp();
	                    mainFrame.repaint();
	                // Receive message
	                } else if (e.getActionCommand().equals("receive")){
	                    receive();
	                    ProcessMessage();
	                    mainFrame.repaint();
                    // Log message
	                } else if (e.getActionCommand().equals("log")){
	                	logMessage = new TimeStampedMessage("logger", currentMessage.get_kind(), currentMessage.toString());
	                	logMessage.set_seqNum(sequenceNumber); 
                        sequenceNumber++;
                        logMessage.set_source(nodeME.name);
                        logMessage.set_nodeIndex(GetSendNodeIndex("logger"));
                        logMessage.set_timeStamp(clockService.get_sendTimeStamp());
	                    DoSend(logMessage);
	                    // Update clock
	            		clockService.updateTimeStamp();
	                    mainFrame.repaint();
	                }
	            }
	        };
	        sendButton = new JButton("Send");
	        sendButton.setPreferredSize(new Dimension(80, 30));
	        sendButton.setActionCommand("send");
	        sendButton.addActionListener(messageButtonListener);
	        sendButton.setEnabled(false);
	        receiveButton = new JButton("Receive");
	        receiveButton.setPreferredSize(new Dimension(80, 30));
	        receiveButton.setActionCommand("receive");
	        receiveButton.addActionListener(messageButtonListener);
	        receiveButton.setEnabled(false);
	        logButton = new JButton("Log");
	        logButton.setPreferredSize(new Dimension(70, 30));
	        logButton.setActionCommand("log");
	        logButton.addActionListener(messageButtonListener);
	        logButton.setEnabled(false);
	        connectPane2.add(sendButton);
	        connectPane2.add(receiveButton);
	        connectPane2.add(logButton);
	        infoPane.add(connectPane2);
		
        // 	Set up the who label
	        whoLabel = new JLabel();
	        whoLabel.setText("Messages History :");
	        whoLabel.setPreferredSize(new Dimension(50,20));
		
        // 	Set up the message pane
	        JPanel messagePane = new JPanel(new BorderLayout());
	        msgText = new JTextArea(10, 20);
	        msgText.setLineWrap(true);
	        msgText.setEditable(false);
	        msgText.setForeground(Color.blue);
	        JScrollPane msgTextPane = new JScrollPane(msgText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	        msgLine = new JTextField();
	        msgLine.setEnabled(false);
	        msgLine.setPreferredSize(new Dimension(150,30));
	        messagePane.add(whoLabel, BorderLayout.PAGE_START);
	        messagePane.add(msgLine, BorderLayout.PAGE_END);
	        messagePane.add(msgTextPane, BorderLayout.CENTER);
	        
        // 	Set up the main pane
	        JPanel mainPane = new JPanel(new BorderLayout());
	        mainPane.add(statusBar, BorderLayout.SOUTH);
	        mainPane.add(infoPane, BorderLayout.WEST);
	        mainPane.add(messagePane, BorderLayout.CENTER);
        
        // 	Set up the main frame
	        mainFrame = new JFrame("Distributed Systems: Lab 1");
	        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        mainFrame.setContentPane(mainPane);
	        mainFrame.setSize(mainFrame.getPreferredSize());
	        mainFrame.setLocation(200, 200);
	        mainFrame.pack();
	        mainFrame.setVisible(true);
	} 

    // Send message function
    public void send(final TimeStampedMessage theMessage) {
    	int index = theMessage.get_nodeIndex();
    	
        sendRule = CheckRule(theMessage, CHECKSEND);
        if (sendRule == DROP) {
            // Don't add the message to the send queue
            sendDelay = 0;
        } else if (sendRule == DUPLICATE) {
        	if (index > 2) {
	        	// Do multicast (duplicate flag set to false)
        		Multicast(theMessage);
	        	// Do second multicast (duplicate flag set to true)
	            theMessage.set_duplicate(true);
	            Multicast(theMessage);
        	} else {
	        	// Do send (duplicate flag set to false)
	        	DoSend(theMessage);
	        	// Do second send (duplicate flag set to true)
	            theMessage.set_duplicate(true);
	            DoSend(theMessage);
        	}
            sendDelay = 0;
        } else if (sendRule == DELAY) {
            sendDelay = 1;
            nodeME.delayedSendQueue.add(theMessage);
        } else {
        	if (index > 2) {
        		Multicast(theMessage);
        	} else {
        		DoSend(theMessage);
        	}
            sendDelay = 0;
        }
        
        // Check to see if delayed messages should be sent
        if (sendDelay == 0 && !nodeME.delayedSendQueue.isEmpty()) {
            for (int k=0; k < nodeME.delayedSendQueue.size(); k++) {
            	try {
					DoSend(nodeME.delayedSendQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    // Multicast message method
    public void Multicast(TimeStampedMessage message) {
    	String destGroup = message.get_destination();
    	
    	
    }
    
    // Send message from send queue
    public void DoSend(TimeStampedMessage theMessage) {
    	
        // Show message to be sent in history panel
        msgText.append("\nSENT to " + theMessage.get_destination() + " > " + theMessage.toString() + "\n");
        
        // Send message out through PrintWriter and the appropriate socket (nodeIndex) 
        int nodeIndex = theMessage.get_nodeIndex();
        switch(nodeME.nodeNumber) {
            case 1:
                if (nodeIndex <= 2) {
                    nodeME.outbox[nodeIndex].println(theMessage);
                    nodeME.outbox[nodeIndex].flush();
                }
                break;
            case 2:
                if (nodeIndex == 0) {
                    nodeME.outboxC[nodeIndex].println(theMessage);
                    nodeME.outboxC[nodeIndex].flush();
                } else {
                    nodeME.outbox[nodeIndex-1].println(theMessage);
                    nodeME.outbox[nodeIndex-1].flush();
                }
                break;
            case 3:
                if (nodeIndex <= 1) {
                    nodeME.outboxC[nodeIndex].println(theMessage);
                    nodeME.outboxC[nodeIndex].flush();
                } else {
                    nodeME.outbox[0].println(theMessage);
                    nodeME.outbox[0].flush();
                }
                break;
            case 4:
                if (nodeIndex <= 2) {
                    nodeME.outboxC[nodeIndex].println(theMessage);
                    nodeME.outboxC[nodeIndex].flush();
                }
                break;
        }
        mainFrame.repaint();
    }
    
    // Parse message function
    public TimeStampedMessage ParseMessage(String msg) {
    	// Parse line read from BufferedReader to get TimeStampedMessage
        String dest = null;
        String src = null;
        int sNum = 0;
        String knd = null;
        Object dta = null;
        TimeStamp ts = new TimeStamp("");
        boolean dup = false;
        TimeStampedMessage gotMessage = new TimeStampedMessage(dest, knd, dta);
      
        src = msg.substring(5, msg.indexOf(" To:"));
        dest = msg.substring(msg.indexOf("To:")+3, msg.indexOf(" TimeStamp:"));
        ts.ts = msg.substring(msg.indexOf("TimeStamp:")+10, msg.indexOf(" Seq:"));
        sNum = Integer.parseInt(msg.substring(msg.indexOf("Seq:")+4, msg.indexOf(" Kind:")));
        knd = msg.substring(msg.indexOf("Kind:")+5, msg.indexOf(" Dup:"));
        dup = Boolean.parseBoolean(msg.substring(msg.indexOf("Dup:")+4, msg.indexOf(" Data:")));
        dta = msg.substring(msg.indexOf("Data:")+5, msg.length());
        gotMessage.set_destination(dest);
        gotMessage.set_source(src);
        gotMessage.set_seqNum(sNum);
        gotMessage.set_kind(knd);
        gotMessage.set_data(dta);
        gotMessage.set_duplicate(dup);
        gotMessage.set_timeStamp(ts);
        
        return gotMessage;
    }
    
    // Apply message receive rule
    public void DoReceiveRule(TimeStampedMessage gotMessage, int receiveRule) {
        if (receiveRule == NONE) {
        	nodeME.receiveQueue.add(gotMessage);
            receiveDelay = 0;
        } else if (receiveRule == DROP) {
            receiveDelay = 0;
        } else if (receiveRule == DUPLICATE) {
        	nodeME.receiveQueue.add(gotMessage);
        	// EDITED OUT: Lab says only send rule should have duplicate flag
        	// Set duplicate flag to true for second message received
            // gotMessage.set_duplicate(true);
            nodeME.receiveQueue.add(gotMessage);
            receiveDelay = 0;
        } else if (receiveRule == DELAY) {
            receiveDelay = 1;
            nodeME.delayedReceiveQueue.add(gotMessage);
        }
    }
    
    // Receive message function
    public void receive() {
    	TimeStampedMessage gotMessage = null;
        try {
            if (nodeME.inbox[0] != null && nodeME.inbox[0].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inbox[0].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            }
            
            if (nodeME.inbox[1] != null && nodeME.inbox[1].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inbox[1].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            }
            
            if (nodeME.inbox[2] != null && nodeME.inbox[2].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inbox[2].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            }
            
            if (nodeME.inboxC[0] != null && nodeME.inboxC[0].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inboxC[0].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            } 
            
            if (nodeME.inboxC[1] != null && nodeME.inboxC[1].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inboxC[1].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            }
            
            if (nodeME.inboxC[2] != null && nodeME.inboxC[2].ready()) {
            	// Get message and parse from string to TimeStampedMessage
            	gotMessage = ParseMessage(nodeME.inboxC[2].readLine());
                // Check receive rule
                receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
                // Apply receive rule
                DoReceiveRule(gotMessage, receiveRule);
            }
        } catch (IOException e) {
            System.out.print("IO Exception: Problem receiving a message\n");
            e.printStackTrace();
        }
    }
    
    public TimeStampedMessage ProcessMessage() {
    	TimeStampedMessage msg = null;
        // Retrieve a message from the front of the receiveQueue and display it
        if (!nodeME.receiveQueue.isEmpty()) {
            try {
            	msg = nodeME.receiveQueue.take();
				// Update clock after message received
				clockService.set_receiveTimeStamp(msg.get_timeStamp());
	            msgText.append("\n" + msg.toString() + "\n");
	            currentMessage = msg;
	            mainFrame.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            
            // If a message was received, check to see if delayed received messages 
            // should also now be received
            if (receiveDelay == 0 && !nodeME.delayedReceiveQueue.isEmpty()) {
                for (int k=0; k < nodeME.delayedReceiveQueue.size(); k++) {
                	try {
                		msg = nodeME.delayedReceiveQueue.take();
						// Update clock after message received
						clockService.set_receiveTimeStamp(msg.get_timeStamp());
	                    msgText.append("\n" + msg.toString() + "\n");
	                    currentMessage = msg;
	                    mainFrame.repaint();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                }
            }
        }
        return msg;
    }

    // TimeStampedMessage Passer constructor
    public MessagePasser(String configuration_filename, String local_name) {

    	// Load information from configuration file
    	LoadConfig(configuration_filename, local_name);
    	
        // Set my information (nodeME) and otherNodes information
        if (local_name.equals(nodeA.name)) {
            nodeME.name = nodeA.name; nodeME.ip = nodeA.ip; nodeME.port = nodeA.port;
            otherNodes.add(nodeB); otherNodes.add(nodeC); otherNodes.add(nodeD);
            nodes[0] = nodeB.name; nodes[1] = nodeC.name; nodes[2] = nodeD.name;
            nodeME.nodeNumber = 1;
        } else if (local_name.equals(nodeB.name)) {
            nodeME.name = nodeB.name; nodeME.ip = nodeB.ip; nodeME.port = nodeB.port;
            otherNodes.add(nodeA); otherNodes.add(nodeC); otherNodes.add(nodeD);
            nodes[0] = nodeA.name; nodes[1] = nodeC.name; nodes[2] = nodeD.name;
            nodeME.nodeNumber = 2;
        } else if (local_name.equals(nodeC.name)) {
            nodeME.name = nodeC.name; nodeME.ip = nodeC.ip; nodeME.port = nodeC.port;
            otherNodes.add(nodeA); otherNodes.add(nodeB); otherNodes.add(nodeD);
            nodes[0] = nodeA.name; nodes[1] = nodeB.name; nodes[2] = nodeD.name;
            nodeME.nodeNumber = 3;
        } else if (local_name.equals(nodeD.name)) {
            nodeME.name = nodeD.name; nodeME.ip = nodeD.ip; nodeME.port = nodeD.port;
            otherNodes.add(nodeA); otherNodes.add(nodeB); otherNodes.add(nodeC);
            nodes[0] = nodeA.name; nodes[1] = nodeB.name; nodes[2] = nodeC.name;
            nodeME.nodeNumber = 4;
        }
        
        // Set up group information: Group(String groupName, String[] nodes, String myName)
        
        
        // Set up server socket
        try {
            myServerSocket = new ServerSocket(nodeME.port);
        } catch (IOException e) {
            System.out.println("Problem setting up server socket");
        }  
        
        // Set up GUI
        SetupGui();
    }
}




