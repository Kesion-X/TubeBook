package socket_server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketMain {
	// 用于检测所有的Channel状态的selector
    private Selector selector = null;
    static final int PORT = 8080;
    // 定义实现编码、解码的字符串集对象
    private Charset charse = Charset.forName("UTF-8");
 
    private RequestDataManager requestDataManager;
    
    public void init() throws IOException {
    //	this.requestDataManager = new RequestDataManager();
        selector = Selector.open();
        // 通过open方法来打开一个未绑定的ServerSocketChannel是咧
        ServerSocketChannel server = ServerSocketChannel.open();
        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
        // 将该ServerSocketChannel绑定到指定的IP地址
        server.bind(isa);
        // 设置serverSocket已非阻塞方式工作
        server.configureBlocking(false);
        // 将server注册到指定的selector对象
        server.register(selector, SelectionKey.OP_ACCEPT);
        //selector.s
        while (selector.select() > 0) {
        	
            // 一次处理selector上的每个选择的SelectionKey
            for (SelectionKey sk : selector.selectedKeys()) {
                // 从selector上已选择的Kye集中删除正在处理的SelectionKey
                selector.selectedKeys().remove(sk);
                // 如果sk对应的Channel包含客户端的连接请求
                if (sk.isAcceptable()) {
                    // 调用accept方法接收连接，产生服务器段的SocketChennal
                    SocketChannel sc = server.accept();
                    // 设置采用非阻塞模式
                    sc.configureBlocking(false);
                    // 将该SocketChannel注册到selector
                    sc.register(selector, SelectionKey.OP_READ);
                    System.out.println("client link");
                    System.out.println( sc.socket().getInetAddress().getHostAddress()+" "+sc.socket().getPort());
                    
                }
                // 如果sk对应的Channel有数据需要读取
                if (sk.isReadable()) {
                    // 获取该SelectionKey对银行的Channel，该Channel中有刻度的数据
                    SocketChannel sc = (SocketChannel) sk.channel();
                    // 定义备注执行读取数据源的ByteBuffer
                    ByteBuffer buff = ByteBuffer.allocate(1024);
                    String content = "";
		            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    // 开始读取数据
                    try {
                    	int isRead;
                        while ((isRead = sc.read(buff)) > 0) {
                        	byteStream.write(buff.array(), 0, isRead);
                            buff.flip();
                          //  content += charse.decode(buff);
                        }
                        if(isRead==-1){//说明客户端断开连接                   
                       	 sk.cancel();
                            if (sk.channel() != null) {
                            	System.out.println("close");
                                sk.channel().close();
                            }
                            break;
                       }
                        System.out.println("length"+byteStream.toByteArray().length);
                        this.requestDataManager.readBytes(byteStream.toByteArray());
                       
                        byte res[] = new byte[4] ;
                        res[0] = byteStream.toByteArray()[0];
                        res[1] = byteStream.toByteArray()[1];
                        res[2] = byteStream.toByteArray()[2];
                        res[3] = byteStream.toByteArray()[3];
                        int len = (res[0] & 24) | (res[1] << 16) // | 表示安位或   
                        		| (res[2] << 8) | (res[3]); 
                        content += new String(byteStream.toByteArray(),"UTF-8");

                        System.out.println("读取的数据：" +len+"| |"+ content);
                        // 将sk对应的Channel设置成准备下一次读取
                        sk.interestOps(SelectionKey.OP_READ);
                    }
                    
                    // 如果捕获到该sk对银行的Channel出现了异常，表明
                    // Channel对应的Client出现了问题，所以从Selector中取消
                    catch (IOException io) {
                        // 从Selector中删除指定的SelectionKey
                        sk.cancel();
                        if (sk.channel() != null) {
                        	System.out.println("close");
                            sk.channel().close();
                        }
                    }
                    // 如果content的长度大于0,则连天信息不为空
                    if (content.length() > 0) {
                        // 遍历selector里注册的所有SelectionKey
                        for (SelectionKey key : selector.keys()) {
                            // 获取该key对应的Channel
                            Channel targerChannel = key.channel();
                            // 如果该Channel是SocketChannel对象
                            if (targerChannel instanceof SocketChannel) {
                                // 将读取到的内容写入该Channel中
                                SocketChannel dest = (SocketChannel) targerChannel;
                                dest.write(charse.encode(content));
                            }
                        }
                    }
                }
            }
        }
 
    }
     
    public static void main(String [] args) throws IOException{
    	System.out.println("socket server");
        new SocketMain().init();
        
    }
 
}
