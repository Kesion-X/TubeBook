package socket_server;

import java.nio.charset.Charset;

public class SocketConfigurationModel {
	
	
	private static final String IP = "127.0.0.1";
	private static final int PORT = 8080;
	private static final String CHAR_SET = "UTF-8";
	private static final int TIME_OUT = 5000;
	
	private String ip;
	private int port; 
	private Charset charseSet;
	private int timeOut ;
	
	public SocketConfigurationModel(){
		this.ip = IP;
		this.port = PORT;
		this.charseSet = Charset.forName(CHAR_SET);
		this.timeOut = TIME_OUT;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public Charset getCharseSet() {
		return charseSet;
	}


	
}
