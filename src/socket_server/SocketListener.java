package socket_server;

import java.net.Socket;

public interface SocketListener {
	
	public void HaveClientConnectioned(Socket socket);

	public void HaveClientReading(Socket socket);
	
	public void HaveClientReadComplete(Socket socket, byte[] headBytes, byte[] contentBytes);
	
	public void HaveClientReadTimeOut(Socket socket);
	
	public void HaveClientDisConnection(Socket socket);
	
}
