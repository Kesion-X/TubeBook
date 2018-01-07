package socket_server;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

import net.sf.json.JSONObject;

public class RequestDataManager {
	
	/**
	 * 读取监听接口
	 * @author kejianfang
	 */
	public interface RequestDataReadListener{
		public void dataReading(Socket socket);
		public void dataReadComplete(Socket socket,byte headBytes[],byte contentBytes[]);
	}
	private RequestDataReadListener readListener;
	
	private static final int HEAD_BYTES_LENGTH = 4;
	
	private Socket socket;
	
	private int readPos = 0;
	
	private byte headLengthBytes[];
	private int currentHeadLengthByteLength;
	
	private byte headBytes[];
	private int currentHeadByteLength;
	
	private byte contentBytes[];
	private int currentContentByteLength;
	private int contentLength;
	
	public RequestDataManager(Socket socket){
		initBytesLength();
		this.socket = socket;
	}
	
	/**
	 * 报文读取初始化处理
	 */
	private void initBytesLength(){
		this.headLengthBytes = new byte[4];
		this.headBytes = null;
		this.contentBytes = null;
		this.currentHeadLengthByteLength = 0;
		this.currentHeadByteLength = 0;
		this.currentContentByteLength = 0;
	}
	
	/**
	 * 数据读取
	 * @param bts
	 */
	public void readBytes(byte bts[]){
		this.readPos = 0;
		
		if( !isCompleteHeadByteLength() ){//读取head_length
			readByteHeadLength(bts);
			//System.out.println("request manager heand length:"+this.currentHeadLengthByteLength+" "+getheadLength());
			if( isCompleteHeadByteLength() ){//完成读取head_length是否完成，完成则读取head
				if( this.headBytes==null ){
					this.headBytes = new byte[getheadLength()];
				}
				readHead(bts);
				if( isCompleteHeadByte() ){//完成head
					try {
						String str = new String(this.headBytes,"Utf-8");
						JSONObject json = JSONObject.fromObject(str);
						this.contentLength = json.getInt("content-length");
						if( this.contentBytes==null ){
							this.contentBytes = new byte[this.contentLength];
						}
						//System.out.println("request manager: "+str+" "+this.contentLength);
						
						readContent(bts);
						
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}else if ( !isCompleteHeadByte() ){//读取head
			readHead(bts);
		}else{//读取content
			readContent(bts);
		}
		
		//数据读取监听
		readListener();
		
		// TODO 读取完成
		if( isCompleteContent() ){
			String head;
			String content;
			try {
				head = new String(this.headBytes,"Utf-8");
				content = new String(this.contentBytes,"Utf-8");
				//System.out.println("request manager complete\n"
				//		+"head:"+head+"\n"
				//		+"content:"+content);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initBytesLength();
		}
		
	}
	
	
	/**
	 * 读取超时处理
	 */
	public void readTimeOut(){
		this.initBytesLength();
	}
	
	/**
	 * 设置读取监听
	 * @param readListener
	 */
	public void addReadListener(RequestDataReadListener readListener){
		this.readListener = readListener;
	}
	
	/**
	 * close manager
	 */
	public void close(){
		this.socket = null;
		this.readListener = null;
	}
	
	/**
	 * 数据读取监听
	 */
	private void readListener(){
		if( isDataReadComplete() ){
			if( readListener!=null ){
				byte headBytes[] = new byte[this.headBytes.length];
				System.arraycopy(this.headBytes, 0, headBytes, 0, this.headBytes.length);
			
				byte contentBytes[] = new byte[this.contentBytes.length];
				System.arraycopy(this.contentBytes, 0, contentBytes, 0, this.contentBytes.length);

				this.readListener.dataReadComplete(this.socket, headBytes, contentBytes);
			}
		}else{
			if( readListener!=null ){
				this.readListener.dataReading(this.socket);
			}
			
		}
	}
	
	/**
	 * 判断是否读取完成
	 * @return
	 */
	private boolean isDataReadComplete(){
		return this.isCompleteHeadByteLength()&&this.isCompleteHeadByte()&&this.isCompleteContent();
	}
	
	
	/**
	 * content  读取
	 * @param bts
	 */
	private void readContent(byte bts[]){
		if( isCompleteContent() )
			return ;
		
		if( (bts.length-this.readPos)>=this.contentLength && this.currentContentByteLength==0 ){
			System.arraycopy(bts, this.readPos, this.contentBytes, 0, this.contentLength);
			this.currentContentByteLength = this.contentLength;
			this.readPos += this.contentLength;
		}else{
			int length = this.contentLength - this.currentContentByteLength;
			if( bts.length<length ){
				length = bts.length;
				System.arraycopy(bts, this.readPos, this.contentBytes, this.currentContentByteLength, length);
				this.currentContentByteLength += length;
			}else{
				System.arraycopy(bts, this.readPos, this.contentBytes, this.currentContentByteLength, length);
				this.currentContentByteLength = this.contentLength;
				this.readPos += this.contentLength;
			}
		}
	}
	
	/**
	 * content读取完成
	 * @return
	 */
	private boolean isCompleteContent(){
		return this.currentContentByteLength == this.contentLength ? true:false;
	}
	
	
	/**
	 *  报文头读取
	 * @param bts
	 */
	private void readHead(byte[] bts) {
		if( isCompleteHeadByte() )
			return ;
		
		if( (bts.length-this.readPos)>=getheadLength() && this.currentHeadByteLength==0 ){
			System.arraycopy(bts, this.readPos, this.headBytes, 0, getheadLength());
			this.currentHeadByteLength = getheadLength();
			this.readPos += getheadLength();
		}else{
			int length = getheadLength() - this.currentHeadByteLength;
			if( bts.length<length ){
				length = bts.length;
				System.arraycopy(bts, this.readPos, this.headBytes, this.currentHeadByteLength, length);
				this.currentHeadByteLength += length;
			}else{
				System.arraycopy(bts, this.readPos, this.headBytes, this.currentHeadByteLength, length);
				this.currentHeadByteLength = getheadLength();
				this.readPos += getheadLength();
			}
		}
	}
	
	/**
	 * 报文头读取完成
	 * @return
	 */
	private boolean isCompleteHeadByte(){
		return this.currentHeadByteLength == getheadLength() ? true:false;
	}
	
	/**
	 * 获取报文头字节长度
	 * @return
	 */
	private int getheadLength(){
		int len = 0;
		if( isCompleteHeadByteLength() ){
			len = (headLengthBytes[3] )|(headLengthBytes[2]<<8)|(headLengthBytes[1]<<16)|(headLengthBytes[0] << 24);
		}
		return len;
	}

	/**
	 * 报文头长度读取
	 * @param bts
	 */
	private void readByteHeadLength(byte bts[]){
		if( isCompleteHeadByteLength() )
			return ;
		
		if ( bts.length>=HEAD_BYTES_LENGTH && this.currentHeadLengthByteLength==0 ){
			System.arraycopy(bts, 0, this.headLengthBytes, 0, HEAD_BYTES_LENGTH);
			this.currentHeadLengthByteLength = HEAD_BYTES_LENGTH;
			this.readPos += HEAD_BYTES_LENGTH;
		}else{
			int length = HEAD_BYTES_LENGTH - this.currentHeadLengthByteLength;
			if (bts.length<length  ) {
				length = bts.length;
				System.arraycopy(bts, 0, this.headLengthBytes, this.currentHeadLengthByteLength, length);
				this.currentHeadLengthByteLength+=bts.length;
			}else{
				System.arraycopy(bts, 0, this.headLengthBytes, this.currentHeadLengthByteLength, length);
				this.currentHeadLengthByteLength = HEAD_BYTES_LENGTH;
				this.readPos += HEAD_BYTES_LENGTH;
			}
		}
	}
	
	/**
	 * 报文头长度读取完成
	 * @return
	 */
	private boolean isCompleteHeadByteLength(){
		return currentHeadLengthByteLength == HEAD_BYTES_LENGTH ? true:false;
	}
	

}
