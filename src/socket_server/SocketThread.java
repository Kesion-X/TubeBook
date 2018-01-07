package socket_server;

public class SocketThread extends Thread{
	
	
	@Override
	public void run() {
		SocketManager.getSocketManager().startSocket();
	}
	

}
