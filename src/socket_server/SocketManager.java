package socket_server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import socket_server.RequestDataManager.RequestDataReadListener;

public class SocketManager implements RequestDataReadListener{
	
	private static volatile SocketManager socketManager;
	
	public static SocketManager getSocketManager(){
		if( socketManager==null ){
			synchronized (SocketManager.class) {
				if(socketManager==null){
					socketManager = new SocketManager();
				}
			}
		}
		return socketManager;
	}
	
	
	private SocketListener socketListener = null;

	// 用于检测所有的Channel状态的selector
    private Selector selector = null;
    private SocketConfigurationModel socketConfigurationModel;
    private ServerSocketChannel server = null;
    private InetSocketAddress isa;
    
    private HashMap<Socket, RequestDataManager> clientMap;
    
    public SocketManager(){
    	
    	this.clientMap = new HashMap<Socket, RequestDataManager>();
    	initSocket();
    }
    
    /**
     *socket初始化 
     */
    private void initSocket(){
    	this.socketConfigurationModel = new SocketConfigurationModel();
        try {
			selector = Selector.open();
	        // 通过open方法来打开一个未绑定的ServerSocketChannel是咧
	        server = ServerSocketChannel.open();
	        isa = new InetSocketAddress(this.socketConfigurationModel.getIp(), this.socketConfigurationModel.getPort());
	        // 将该ServerSocketChannel绑定到指定的IP地址
	        server.bind(isa);
	        // 设置serverSocket已非阻塞方式工作
	        server.configureBlocking(false);
	        // 将server注册到指定的selector对象
	        server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    /**
     * 开启socket
     */
    public void startSocket(){
        try {
        	
        	
			while (selector.select() > 0) {
				
			    // 一次处理selector上的每个选择的SelectionKey
			    for (SelectionKey sk : selector.selectedKeys()) {
			        // 从selector上已选择的Kye集中删除正在处理的SelectionKey
			        selector.selectedKeys().remove(sk);
			        
			        
			        
			        // 如果sk对应的Channel包含客户端的连接请求----------------------------用户连接监听
			        if (sk.isAcceptable()) {
			            // 调用accept方法接收连接，产生服务器段的SocketChennal
			            SocketChannel sc = server.accept();
			            // 设置采用非阻塞模式
			            sc.configureBlocking(false);
			            //设置超时
			            sc.socket().setSoTimeout(this.socketConfigurationModel.getTimeOut());
			            // 将该SocketChannel注册到selector
			            sc.register(selector, SelectionKey.OP_READ);
			           // System.out.println("client link"+sc.socket().getInetAddress().getHostAddress()+" "+sc.socket().getPort() );
			            
			            this.addClient(sc.socket(), new RequestDataManager(sc.socket()));//保存客户socket
			            this.clientMap.get(sc.socket()).addReadListener(this);
			            this.listenerConnection(sc.socket());
			        }
			        
			        
			        
			        // 如果sk对应的Channel有数据需要读取----------------------------------用户读数据监听
			        if (sk.isReadable()) {
			            // 获取该SelectionKey对银行的Channel，该Channel中有刻度的数据
			            SocketChannel sc = (SocketChannel) sk.channel();			           
			            // 定义备注执行读取数据源的ByteBuffer
			            ByteBuffer buff = ByteBuffer.allocate(1024);
			           // String content = "";
			            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			           
			            try {
			            	
			            	 // 开始读取数据
			            	int isRead;
			                while ((isRead = sc.read(buff)) > 0) {
			                	byteStream.write(buff.array(), 0, isRead);
			                    buff.flip();
			                }
			                
			                if( isRead==-1 || ( sc.socket()!=null && !sc.socket().isConnected() ) ){//说明客户端断开连接 ----------------------------------用户断开连接                  
			                	//if( sc.socket()!=null&&!sc.socket().isConnected() ){
			                		System.out.println("socket manager close");
					                this.listenerDisConnection(sc.socket());
					               	this.clientMap.get(sc.socket()).close();//关掉监听
						           	this.removeClient(sc.socket());//移除用户------------------------------------连接断开
						           	sk.cancel();
						            if (sk.channel() != null) {
						               	//System.out.println("close");
						               	sk.channel().close();
						             }
			                //	}
			                	break;
			                }
			                
			                
			                
			                this.clientMap.get(sc.socket()).readBytes(byteStream.toByteArray());
			                byteStream.close();

			                // 将sk对应的Channel设置成准备下一次读取
			                sk.interestOps(SelectionKey.OP_READ);
			                
			            }catch ( IOException io ) { // 如果捕获到该sk对银行的Channel出现了异常，表明Channel对应的Client出现了问题，所以从Selector中取消
			                // 从Selector中删除指定的SelectionKey
			                
			            	if( io instanceof SocketTimeoutException ){//----------------------------------读取超时
			            		this.listenerReadDataTimeOut(sc.socket());
			                }else{
			                	this.listenerDisConnection(sc.socket());
			                	this.clientMap.get(sc.socket()).close();//关掉监听
				            	this.removeClient(sc.socket());//移除用户------------------------------------连接断开
				            	sk.cancel();
				                if (sk.channel() != null) {
				                	//System.out.println("close");
				                	sk.channel().close();
				                }
			                }
			            }
			            
			            
			            
			            
			            // 如果content的长度大于0,则连天信息不为空
			            
			                // 遍历selector里注册的所有SelectionKey
			                for (SelectionKey key : selector.keys()) {
			                    // 获取该key对应的Channel
			                    Channel targerChannel = key.channel();
			                    // 如果该Channel是SocketChannel对象
			                    if (targerChannel instanceof SocketChannel) {
			                        // 将读取到的内容写入该Channel中
			                        SocketChannel dest = (SocketChannel) targerChannel;
			                        dest.write(this.socketConfigurationModel.getCharseSet().encode("收到数据"));
			                    }
			                }
			            
			            
			        }//channel read end
			        
			        
			    }//socketkey for end
			    
			    
			}//selector while end
			
			
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * 增加一个用户
     * @param client
     * @param manager
     */
    private void addClient(Socket client,RequestDataManager manager){
    	if(this.clientMap.get(client)==null){
    		this.clientMap.put(client, manager);
    	}
    }
    
    /**
     * 移除用户
     * @param client
     */
    private void removeClient(Socket client){
    	if( this.clientMap.get(client)!=null ){
    		this.clientMap.remove(client);
    	}
    }
    
    /**
     * 监听socket数据传输
     * @param socketListener
     */
    public void addSocketListener(SocketListener socketListener){
    	this.socketListener = socketListener;
    }
    
    /**
     * 移除数据监听
     */
    public void removeSocketListener(){
    	this.socketListener = null;
    }
    
    private void listenerConnection(Socket socket){
    	if ( this.socketListener!=null ) {
        	this.socketListener.HaveClientConnectioned(socket);	
		}
    }
    
    private void listenerDataReading(Socket socket){
    	if ( this.socketListener!=null ) {
        	this.socketListener.HaveClientReading(socket);
		}
    }
    
    private void listenerDataReadComplete(Socket socket, byte[] headBytes, byte[] contentBytes){
    	if ( this.socketListener!=null ) {
        	this.socketListener.HaveClientReadComplete(socket, headBytes, contentBytes);
		}
    }
    
    private void listenerReadDataTimeOut(Socket socket){
    	if ( this.socketListener!=null ) {
        	this.socketListener.HaveClientReadTimeOut(socket);
		}
    }
    
    private void listenerDisConnection(Socket socket){
    	if ( this.socketListener!=null ) {
        	this.socketListener.HaveClientDisConnection(socket);
		}
    }

	@Override
	public void dataReading(Socket socket) {
		this.listenerDataReading(socket);
	}

	@Override
	public void dataReadComplete(Socket socket, byte[] headBytes, byte[] contentBytes) {
		this.listenerDataReadComplete(socket, headBytes, contentBytes);
	}
	
	
}
