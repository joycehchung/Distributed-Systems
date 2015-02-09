import java.io.*;
import java.net.*;


public class GetConfigFile {
	private URL configFileUrl;
	private HttpURLConnection connection;
	private BufferedInputStream bufferInputStream;
	private StringBuffer buffer;
	private int numRead;
	private char c;
	private final String configFileText;
	
	public GetConfigFile() throws IOException{
		
		// URL of configuration file
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
	    buffer = new StringBuffer();
	    while ((numRead = bufferInputStream.read()) != -1) 
	    {
	        c = (char) numRead;
	        buffer.append(c);
	    }
	    
	    // Get file contents from buffer
	    configFileText = buffer.toString();
	}
	
	public String get_configFile() {
		return this.configFileText;
	}
	
    public static void main(String[] args) {
    	try {
    		GetConfigFile cf = new GetConfigFile();
    		String configFile = cf.get_configFile();
    		System.out.println(configFile);
		} catch (IOException e) {
			System.err.println("Error reading configuration file.");
		}

    }
}
