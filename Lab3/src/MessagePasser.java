import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unused")
public class MessagePasser {
	
    // Sockets
    Socket mySockets[];
    ServerSocket myServerSocket;
    Socket otherSockets[];
    
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
    public static MyNode nodeME = new MyNode();
    public static ArrayList<Node> otherNodes = new ArrayList<Node>();
    public static String[] nodes;
    
    // Groups
    public static ArrayList<String> groups = new ArrayList<String>();
    public static ArrayList<Group> myGroups = new ArrayList<Group>();
   // public static boolean remulticast = false;
    
    // ClockService
    public ClockService clockService = null;
    private final static int EQUAL = 0;
	private final static int BEFORE = 1;
    private final static int AFTER = 2;
    private final static int CONCURRENT = 3;
    private final static int ERROR = 4;
    
    // Logging
    public static TimeStampedMessage currentMessage = null;
    public static TimeStampedMessage logMessage = null;
    public static ArrayList<TimeStampedMessage> history = new ArrayList<TimeStampedMessage>();
    
    // Configuration File
	public static URL configFileUrl;
	public static HttpURLConnection connection;
	public static BufferedInputStream bufferInputStream;
    
    // Handle Java Swing events
    public static class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {}
    }
    
    // As a server, connect to client nodes
    public boolean ConnectToClients(int i) {
        try {
            statusBar.setText("Waiting for client ... ");
            mainFrame.repaint();
            
            mySockets[i] = myServerSocket.accept();
            mySockets[i].setSoTimeout(500);
            
            statusBar.setText("Connected to " + mySockets[i].getRemoteSocketAddress());
            mainFrame.repaint();
            
            myStatus = CONNECTED;

            ObjectOutputStream out = new ObjectOutputStream(mySockets[i].getOutputStream());
            out.flush();
            nodeME.out.add(out);
            nodeME.in.add(new ObjectInputStream(mySockets[i].getInputStream()));
//            nodeME.inS[i] = new ObjectInputStream(mySockets[i].getInputStream());
//            nodeME.outS[i] = new ObjectOutputStream(mySockets[i].getOutputStream());
//            nodeME.inbox[i] = new BufferedReader(new InputStreamReader(mySockets[i].getInputStream()));
//            nodeME.outbox[i] = new PrintWriter(mySockets[i].getOutputStream(),true);
            
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }
    
    // As a client, connect to n server nodes
    public boolean ConnectToServers(int i) {
        try {
            otherSockets[i] = new Socket(otherNodes.get(i).ip, otherNodes.get(i).port);
            otherSockets[i].setSoTimeout(500);
            
            statusBar.setText("Connected to " + otherSockets[i].getRemoteSocketAddress());
            mainFrame.repaint();
            
            myStatus = CONNECTED;
            ObjectOutputStream out = new ObjectOutputStream(otherSockets[i].getOutputStream());
            out.flush();
            nodeME.out.add(out);
            nodeME.in.add(new ObjectInputStream(otherSockets[i].getInputStream()));
//            nodeME.inC[i] = new ObjectInputStream(otherSockets[i].getInputStream());
//            nodeME.outC[i] = new ObjectOutputStream(otherSockets[i].getOutputStream());
//            nodeME.inboxC[i] = new BufferedReader(new InputStreamReader(otherSockets[i].getInputStream()));
//            nodeME.outboxC[i] = new PrintWriter(otherSockets[i].getOutputStream(),true);
            
        } catch(UnknownHostException unknownHost) {
            System.err.println("Unknown Host (Server)");
            return false;
        } catch(ConnectException ce) {
            //System.err.println("Server cannot be found ... May not be online yet");
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }
    
    // Connect to other nodes function (some logic here)
    public void MakeConnections() {   	
        int sum = 0;
        int tot = nodeME.numOtherNodes+1;
        int stot = nodeME.nodeNumber-1;
        int ltot = tot-nodeME.nodeNumber;
        
        ArrayList<Integer> S = new ArrayList<Integer>();
        ArrayList<Integer> L = new ArrayList<Integer>();

        System.out.println("Connected to no nodes, need S(" + stot + ") and L(" + ltot + ")");
        
        while (sum != nodeME.numOtherNodes) {        	
        	if (S.size() < stot) { 
            	if (ConnectToServers(S.size())) { S.add(1); }
            } else if (L.size() < ltot) {
            	if (ConnectToClients(L.size())) { L.add(1); }
            }
        	sum = S.size() + L.size();
        }
    }
    
    // Send information to other nodes
    public void SendInformation() {
    	for (int i=0; i<nodeME.out.size(); i++) {
    		try {
				nodeME.out.get(i).writeObject(nodeME.name);
				nodeME.out.get(i).flush();
			} catch (IOException e) {
				System.err.println("Problem sending information out.");
			}
    	}
    }
    
    // Receive information from other nodes
    public void ReceiveInformation() {
    	for (int i=0; i<nodeME.in.size(); i++) {
			try {
				nodes[i] = (String) nodeME.in.get(i).readObject();
				System.out.println("Received info from: " + nodes[i] + " on node: " + i);
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Problem sending information out.");
			}
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

			int index = 1;
			
			for(HashMap<String, Object> row : config)
			{
				String Name = (String)row.get("name");
				User usr = new User(Name);
				usr.setIp((String)row.get("ip"));
				usr.setPort((Integer)row.get("port"));
				users.put(Name, usr);
				System.out.println(usr.toString());
				
				if (usr.getName().equals(local_name)) {
					nodeME.name = usr.getName();
					nodeME.ip = usr.getIp();
					nodeME.port = usr.getPort();
					nodeME.nodeNumber = index;
				} else {
					Node newNode = new Node();
					newNode.name = usr.getName();
					newNode.ip = usr.getIp();
					newNode.port = usr.getPort();
					otherNodes.add(newNode);
				}
				index++;
			}
			
			nodeME.numOtherNodes = otherNodes.size();

			// Instantiate nodes array (for GUI)
			nodes = new String[nodeME.numOtherNodes];
			for (int i=0; i<nodeME.numOtherNodes; i++) {
				nodes[i] = otherNodes.get(i).name;
			}

			
			if(!users.containsKey(local_name))
			{
				System.err.println("local_name: " + local_name + " isn't in " + conf_filename + ", please check again!");
				System.exit(1);
			}
			
			// Get Groups
			ArrayList<HashMap<String, Object> > group_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("groups");
			for(HashMap<String, Object> group_rule : group_rule_arr){
				String groupName = (String)group_rule.get("name");
				for (String key:group_rule.keySet())
				{
					String myName = "";

					if (key.equals("members")){
						boolean add_to_group = false;
						List<String> members = (List<String>) group_rule.get("members");
						if (members.contains(local_name)){
							add_to_group = true;
						}

						for (Iterator<String> iter = members.listIterator(); iter.hasNext(); ) {
						    String a = iter.next();
						    if (a.contains(local_name.toString())) {
						        iter.remove();
						    }
						}
						myName = local_name.toString();
						String[] stringArray = members.toArray(new String[members.size()]);
						
						if (add_to_group){
							Group group = new Group(groupName, stringArray, myName);
							myGroups.add(group);
						}
					}
				}
			}
			// Get group names for GUI
	        for (int i=0; i<myGroups.size(); i++) {
	        	groups.add(myGroups.get(i).groupName);
	        }
			
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
		int index = 100;
		for (int i=0; i<nodes.length; i++) {
			if(name.equals(nodes[i])) { 
				index = i;
				break;
			}
		}
		return index;
	}
	
	// Setup GUI function
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void SetupGui() {
        // 	Status bar
	        statusBar = new JLabel();
	        statusBar.setText("Disconnected");
        
        // 	Node Information Pane
	        JPanel pane = null;
	        JPanel infoPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        infoPane.setPreferredSize(new Dimension(320, 600));
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
	        //nodeListPanel.setPreferredSize(new Dimension(250, 80));
	        pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        pane.setPreferredSize(new Dimension(320, 20));
	        pane.add(new JLabel("Connected Nodes:"));
	        infoPane.add(pane);
	        // Combine nodes and groups lists
	        ArrayList<String> namesList = new ArrayList<String>();
	        namesList.addAll(Arrays.asList(nodes));
	        namesList.addAll(groups);
	        nodeList = new JList(namesList.toArray());
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
	        clockPane.setPreferredSize(new Dimension(320,60));
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
				logicalButton.setEnabled(false);
			} else {
				logicalButton.setEnabled(true);
			}
			vectorButton = new JButton("Vector Clock");
			vectorButton.setPreferredSize(new Dimension(120, 30));
			vectorButton.setActionCommand("vector");
			vectorButton.addActionListener(clockButtonListener);
			vectorButton.setEnabled(true);
	        pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        pane.setPreferredSize(new Dimension(310, 20));
	        pane.add(new JLabel("Select a ClockService:"));
	        clockPane.add(pane);
			clockPane.add(logicalButton);
			clockPane.add(vectorButton);
			infoPane.add(clockPane);
			
	        JLabel timeLabel = new JLabel();
	        timeLabel.setText("TimeStamp :");
			timeButtonListener = new ActionAdapter() {
	            public void actionPerformed(ActionEvent e) {
	            	if (e.getActionCommand().equals("timestamp")) {
	            		String timeString = "<html>TimeStamps: <br>" + nodeME.name + ": " + clockService.get_clockTimeStamp().ts + "<br>";
	            		for (Group g : myGroups) {
	            			timeString = timeString + g.groupName + ": " + g.groupVectorClock.get_clockTimeStamp().ts + "<br>";
	            		}
	            		timeString = timeString + "</html>";
	            		timeLabel.setText(timeString);
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
                        if (theMessage.get_nodeIndex() == 100) {
                        	theMessage.set_multicast(true);
                        	for (int i=0; i<myGroups.size(); i++) {
                        		if (myGroups.get(i).groupName.equals(selectedName)) {
                        			theMessage.set_timeStamp(myGroups.get(i).groupVectorClock.get_sendTimeStamp());
                        			break;
                        		}
                        	}
                        } else {
                        	theMessage.set_timeStamp(clockService.get_sendTimeStamp());
                        }
	                        
	                    // Clear GUI text fields
	                    msgLine.setText("");
	                    kindField.setText("");
	                    currentMessage = theMessage;
	                    
	                    if (theMessage.get_multicast()) {
	                    	Multicast(theMessage);
	                    } else {
	                    	send(theMessage);
	                    }
	                    
	                    // Update clock (if not multicast; if multicast, updated in multicast function)
                        if (!theMessage.get_multicast()) {
                        	clockService.updateTimeStamp();
                        }
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
	        mainFrame = new JFrame("Distributed Systems: Lab 3 Mutual Exclusion");
	        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        mainFrame.setContentPane(mainPane);
	        mainFrame.setSize(mainFrame.getPreferredSize());
	        mainFrame.setLocation(200, 200);
	        mainFrame.pack();
	        mainFrame.setVisible(true);
	} 

    // Send message function
    public void send(TimeStampedMessage theMessage) {
    	
    	if (theMessage.get_multicast()) {
    		String oldDestination = theMessage.get_destination();
    		String oldSource = theMessage.get_source();
    		theMessage.set_destination(nodes[theMessage.get_nodeIndex()]);
    		theMessage.set_source(nodeME.name);
    		sendRule = CheckRule(theMessage, CHECKSEND);
    		theMessage.set_destination(oldDestination);
    		theMessage.set_source(oldSource);
    		System.out.println("Sending Multicast to " + theMessage.get_destination() + " from " + theMessage.get_source());
    	} else {
    		sendRule = CheckRule(theMessage, CHECKSEND);
    	}
    	
    	switch (sendRule) {
    	case DROP:
    		// Don't add the message to the send queue
            sendDelay = 0;
            break;
    	case DUPLICATE:
    		DoSend(theMessage);
        	// Do second send (duplicate flag set to true)
            theMessage.set_duplicate(true);
            DoSend(theMessage);
            break;
    	case DELAY:
    		sendDelay = 1;
            nodeME.delayedSendQueue.add(theMessage);
            break;
    	default:
    		DoSend(theMessage);
            sendDelay = 0;
            break;
    	}
        
        // Check to see if delayed messages should be sent
        TimeStampedMessage delayedMessage;
        if (sendDelay == 0 && !nodeME.delayedSendQueue.isEmpty()) {
            for (int k=0; k < nodeME.delayedSendQueue.size(); k++) {
            	try {
            		delayedMessage = nodeME.delayedSendQueue.take();
            		DoSend(delayedMessage);
				} catch (InterruptedException e) {
					System.err.println("Error retrieving from delayed message queue");
				}
            }
        }
    }
    
    // Multicast message method
    public void Multicast(TimeStampedMessage message) {
    	String destGroup = message.get_destination();
    	int index = 0;
    	boolean found = false;
    	
    	if (message.get_remulticast() && message.get_source().equals(nodeME.name)) {
    		System.out.println("Not re-multicasting a multicast message I originally sent!");
    	} else {
	    	// Search for the group to multicast messages to from myGroups
	    	for (int i=0; i<myGroups.size(); i++) {
	    		if (myGroups.get(i).groupName.equals(destGroup)) {
	    			found = true;
	    			
	    			// Update vector clock for GROUP if using vector clocks
	    			if (!message.get_remulticast()) {
    					myGroups.get(i).groupVectorClock.updateTimeStamp();
	    			}
	    			
	    			// Send message out to each node in the Group
	    			for (int j=0; j<myGroups.get(i).numMembers; j++) {
	    				
	    				index = GetSendNodeIndex(myGroups.get(i).groupMembers.get(j));
	
	    				if (index != 100) {
		    				message.set_nodeIndex(index);
		    				if (!message.get_remulticast()) {
	    						message.set_timeStamp(myGroups.get(i).groupVectorClock.get_sendTimeStamp());
		    				}
		    				send(message);
	    				}
	    			}
	    			if (!message.get_remulticast()) {
	    				// Show message to be sent in history panel
	    		        msgText.append("\nMULTICAST to " + message.get_destination() + " > " + message.toString() + "\n");
	    			} else {
	    				msgText.append("\n...re-multicasting to ensure reliability\n");
	    			}
	    			break;
	    		}
	    	}
	    	
	    	// Print out error if the group name was not found and no messages were multicast
	    	if (!found) {
				System.err.println("Group name not found.");
			}
    	}
    }
    
    // Send message from send queue
    public void DoSend(TimeStampedMessage theMessage) {
    	
    	// Only show the message in history panel if it isn't a multicast OR a remulticast
    	if (!theMessage.get_remulticast() && !theMessage.get_multicast()) {
	        msgText.append("\nSENT to " + theMessage.get_destination() + " > " + theMessage.toString() + "\n");
    	}
    	
        // Send message out through PrintWriter and the appropriate socket (nodeIndex) 
        int nodeIndex = theMessage.get_nodeIndex();
        System.out.println("Sending to node : " + nodes[nodeIndex]);
        try {
        	nodeME.out.get(nodeIndex).flush();
			nodeME.out.get(nodeIndex).writeObject(theMessage);
			nodeME.out.get(nodeIndex).flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Problem sending message out.");
		}
        mainFrame.repaint();
    }
    
    // Apply message receive rule
    public void DoReceiveRule(TimeStampedMessage gotMessage, int receiveRule) {
    	receiveDelay = 0;
    	switch (receiveRule) {
	    	case DROP:
	    		break;
	    	case DUPLICATE:
	    		if (!gotMessage.get_multicast()) {
	        		nodeME.receiveQueue.add(gotMessage);
	                nodeME.receiveQueue.add(gotMessage);
	        	} else {
	        		ReceiveMulticast(gotMessage);
	        	}
	    		break;
	    	case DELAY:
	    		receiveDelay = 1;
	            nodeME.delayedReceiveQueue.add(gotMessage);
	    		break;
			default:
				if (!gotMessage.get_multicast()) {
	        		nodeME.receiveQueue.add(gotMessage);
	        	} else {
	        		ReceiveMulticast(gotMessage);
	        	}
	            break;
    	}
    }
    
    // Put multicast message into group's holdqueue
    public void ReceiveMulticast (TimeStampedMessage message) {
    	System.out.println("Received Multicast!");
    	boolean do_remulticast = true;
    	for (int i=0; i<myGroups.size(); i++) {
    		if (message.get_destination().equals(myGroups.get(i).groupName)) {
    			
    			//Remulticast message if first time receiving it
    			for (TimeStampedMessage m : myGroups.get(i).groupHoldQueue) {
    				if (m.toString().equals(message.toString())) {
    					do_remulticast = false;
    				}
    			}
    			if (do_remulticast) {
    				TimeStampedMessage resend = message;
    				resend.set_remulticast(true);
    				resend.set_multicast(true);
    				Multicast(resend);
    				resend.set_remulticast(false);
    				// Copy of message sent to self just goes into holdqueue
    				System.out.println("Added self-multicast message.");
    				myGroups.get(i).groupHoldQueue.add(message);
    			} 
    			// Add message received to hold queue
    			myGroups.get(i).groupHoldQueue.add(message);
    			break;
    		}
    	}
    }
    
    
    // Receive message function
    public void receive() {
    	TimeStampedMessage gotMessage = null;
    	for (int i=0; i<nodeME.in.size(); i++) {
			try {
					gotMessage = (TimeStampedMessage) nodeME.in.get(i).readObject();
                    // Check receive rule
	            		String oldDestination = gotMessage.get_destination();
	            		String oldSource = gotMessage.get_source();
	            		gotMessage.set_destination(nodeME.name);
	            		gotMessage.set_source(nodes[i]);
	            		receiveRule = CheckRule(gotMessage, CHECKRECEIVE);
	            		gotMessage.set_destination(oldDestination);
	            		gotMessage.set_source(oldSource);
                    // Apply receive rule
	            		DoReceiveRule(gotMessage, receiveRule);
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("No new messages from " + nodes[i]);
			}
    	}   
    }
    
    
    // Causally order HoldQueue for each group
    public void CausallyOrder() {
    	
    	for (int j=0; j<myGroups.size(); j++) { 	
    		if (!myGroups.get(j).groupHoldQueue.isEmpty()) {
    			TimeStampedMessage[] holdQueueArray = new TimeStampedMessage[myGroups.get(j).groupHoldQueue.size()];
    			for (int k=0; k<myGroups.get(j).groupHoldQueue.size(); k++) {
    				holdQueueArray[k] = (TimeStampedMessage) myGroups.get(j).groupHoldQueue.toArray()[k];
    			}
    			Arrays.sort(holdQueueArray);
    			myGroups.get(j).groupHoldQueue.clear();
    			for (int i=0; i<holdQueueArray.length; i++) {
    				myGroups.get(j).groupHoldQueue.add(holdQueueArray[i]);
    				System.out.println("HQ("+i+"): " + holdQueueArray[i].get_timeStamp().ts);
    			}
    		}
    	}
    }
    
    
    // Receive message from HoldQueue
    public void ReceiveHoldQueue() {
    	TimeStampedMessage msg;
    	
    	// Reorder hold queues
    	CausallyOrder();
    	
    	// Check all Group holdQueues to see if a message should be delivered
    	for (int i=0; i<myGroups.size(); i++) { 
    		
    		if (!myGroups.get(i).groupHoldQueue.isEmpty()) {
    			
    			// Add up multicast message count
    			msg = myGroups.get(i).groupHoldQueue.get(0);
    			System.out.println("My timestamp: " + myGroups.get(i).groupVectorClock.get_clockTimeStamp().ts);
    			System.out.println("Message being considered: " + msg.toString());
    			System.out.println("I have " + myGroups.get(i).groupHoldQueue.size() + " message(s) total");
    			System.out.println("I need " + myGroups.get(i).numMembers + " copies");
    			myGroups.get(i).groupHoldQueueCount = 1;
    			for (int j=1; j<myGroups.get(i).groupHoldQueue.size(); j++) {
    				if (msg.toString().equals(myGroups.get(i).groupHoldQueue.get(j).toString())) {
    					myGroups.get(i).groupHoldQueueCount++;
    				}
    			}
    			System.out.println("I have " + myGroups.get(i).groupHoldQueueCount + " copies");
    			
        		// Check if multicast message received from all members of group
        		if (myGroups.get(i).groupHoldQueueCount >= myGroups.get(i).numMembers) {
        			// Check to see if you have to wait for earlier messages or if you are the one who sent the timestamp
            		if (!myGroups.get(i).groupVectorClock.CheckWait(msg.get_timeStamp(), msg.get_source()) ||
            			 msg.get_source().equals(nodeME.name)) {
            			
            			// Remove all K of the message being delivered from the holdqueue
            			for (int k=0; k<myGroups.get(i).groupHoldQueueCount; k++) {
            				myGroups.get(i).groupHoldQueue.remove(0);
            			}
            			
            			// Add message to receive queue
    					nodeME.receiveQueue.add(msg);
        			}
        		} 
    		}

    		myGroups.get(i).groupHoldQueueCount = 0;
		}	
    }
    
   
    // Process Message == Deliver Message from the receiveQueue
    public TimeStampedMessage ProcessMessage() {
    	TimeStampedMessage msg = null;	
    	
    	// Receive qualified messages from holdqueue
    	ReceiveHoldQueue();
    	
        // Retrieve a message from the front of the receiveQueue and display it
        if (!nodeME.receiveQueue.isEmpty()) {
            try {
            	msg = nodeME.receiveQueue.take();
				// Update clock after message received
            	if (msg.get_destination().equals(nodeME.name)) {
            		clockService.updateTimeStamp();
            		clockService.set_receiveTimeStamp(msg.get_timeStamp());
            	} else {
            		for (int i=0; i<myGroups.size(); i++) {
                		if (myGroups.get(i).groupName.equals(msg.get_destination())) {
                			myGroups.get(i).groupVectorClock.set_receiveMulticastTimeStamp(msg.get_timeStamp(), msg.get_source());
                			break;
                		}
                	}
            	}
	            msgText.append("\nRECEIVED: " + msg.toString() + "\n");
	            history.add(msg);
	            currentMessage = msg;
	            mainFrame.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        // If a message was received, check to see if delayed received messages should also now be received
        if (receiveDelay == 0 && !nodeME.delayedReceiveQueue.isEmpty()) {
            for (int k=0; k < nodeME.delayedReceiveQueue.size(); k++) {
            	try {
            		msg = nodeME.delayedReceiveQueue.take();
            		if (msg.get_multicast()) {
            			ReceiveMulticast(msg);
            		} else {
						// Update clock after message received
                		clockService.set_receiveTimeStamp(msg.get_timeStamp());
                		// Show delayed messages as received if not multicast (if multicast goes to holdqueue)
	                    msgText.append("\n" + msg.toString() + "\n");
	                    currentMessage = msg;
	                    mainFrame.repaint();
            		}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
        
        return msg;
    }

    // TimeStampedMessage Passer constructor
    public MessagePasser(String configuration_filename, String local_name) {

    	// Load information from configuration file
    	LoadConfig(configuration_filename, local_name);
    	
    	// Console output
        System.out.println("Hello, my name is " + nodeME.name);
        System.out.println("I am in " + myGroups.size() + " group(s)");
        System.out.println(groups.toString());
        
        // Set up server socket
        try {
            myServerSocket = new ServerSocket(nodeME.port);
        } catch (IOException e) {
            System.out.println("Problem setting up server socket");
        }  
        
        // Set up sockets
        int nclients = nodeME.nodeNumber-1;
        int nservers = nodeME.numOtherNodes+1-nodeME.nodeNumber;
        mySockets = new Socket[nservers];
        otherSockets = new Socket[nclients];
        
        // Set up GUI
        SetupGui();
    }
}




