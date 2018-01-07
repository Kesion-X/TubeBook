package socket_server;

import java.net.Socket;

public interface SocketListener {
	
	public void haveClientConnectioned(Socket socket);

	public void haveClientReading(Socket socket);
	
	public void haveClientReadComplete(Socket socket, byte[] headBytes, byte[] contentBytes);
	
	public void haveClientReadTimeOut(Socket socket);
	
	public void haveClientDisConnection(Socket socket);
	
	public void serverWriteDataing(Socket socket);
	
	public void serverWriteDataComplete(Socket socket);
	
	public void serverWriteDataError(Socket socket, String err);
}
