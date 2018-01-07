package socket_server;

public class RequestHeadModel {
	
	private String method;
	private String url;
	private String contentType;
	private String contentLength;
	
	
	public RequestHeadModel(){
		
	}
	
	public RequestHeadModel(String method, String url, String contentType, String contentLength) {
		super();
		this.method = method;
		this.url = url;
		this.contentType = contentType;
		this.contentLength = contentLength;
	}
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentLength() {
		return contentLength;
	}
	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

}
