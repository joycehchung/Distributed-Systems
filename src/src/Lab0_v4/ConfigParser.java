import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


class ConfigParser{
	
	public static void main(String[] args){
		loadConfig();
	}
	
	public static void loadConfig()
	{		try
		{
			HashMap<String, User> users = new HashMap<String, User>();
			
			FileInputStream fileInput = new FileInputStream("config");
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(fileInput);
			ArrayList<HashMap<String, Object> > config = (ArrayList<HashMap<String, Object> >)data.get("configuration");
			ArrayList<Rule> SendRules = new ArrayList<Rule>();
			ArrayList<Rule> ReceiveRules = new ArrayList<Rule>();
			Object local_name = "alice";
			String conf_filename = "config";
			for(HashMap<String, Object> row : config)
			{
				String Name = (String)row.get("name");
				User usr = new User(Name);
				usr.setIp((String)row.get("ip"));
				usr.setPort((Integer)row.get("port"));
				users.put(Name, usr);
				System.out.println(usr.toString());
			}
			if(!users.containsKey(local_name))
			{
				System.err.println("local_name: " + local_name + " isn't in " + conf_filename + ", please check again!");
				System.exit(1);
			}
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
				System.out.println("Send Rules");
				System.out.println(SendRules);
			}
			SendRules.toString();
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
				System.out.println("Receive Rules");
				System.out.println(ReceiveRules.toString());
			}
			ReceiveRules.toString();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	}


