package socket_server;

public class SocketRequestDataModel {

	private RequestHeadModel requestHeadModel;
	private ContentDataModel contentData;

	public SocketRequestDataModel(){
		this.requestHeadModel = new RequestHeadModel();
		this.contentData = new ContentDataModel();
	}
}
