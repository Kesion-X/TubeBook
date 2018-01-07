package socket_server;

public class ClientAddressModel {

	
	private String address;
	private int port;
	
	public ClientAddressModel(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
