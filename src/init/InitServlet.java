package init;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import socket_server.SocketListener;
import socket_server.SocketManager;
import socket_server.SocketThread;

public class InitServlet extends HttpServlet implements SocketListener {
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		
		SocketThread soccThread = new SocketThread();
		soccThread.start();
		SocketManager.getSocketManager().addSocketListener(this);
	}

	/**
		 * Constructor of the object.
		 */
	public InitServlet() {
		super();
		
	}

	/**
		 * Destruction of the servlet. <br>
		 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
		 * The doGet method of the servlet. <br>
		 *
		 * This method is called when a form has its tag value method equals to get.
		 * 
		 * @param request the request send by the client to the server
		 * @param response the response send by the server to the client
		 * @throws ServletException if an error occurred
		 * @throws IOException if an error occurred
		 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
		 * The doPost method of the servlet. <br>
		 *
		 * This method is called when a form has its tag value method equals to post.
		 * 
		 * @param request the request send by the client to the server
		 * @param response the response send by the server to the client
		 * @throws ServletException if an error occurred
		 * @throws IOException if an error occurred
		 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
		 * Initialization of the servlet. <br>
		 *
		 * @throws ServletException if an error occurs
		 */
	public void init() throws ServletException {
		// Put your code here
	}

	@Override
	public void HaveClientConnectioned(Socket socket) {
		// TODO Auto-generated method stub
		System.out.println("have client link");
	}

	@Override
	public void HaveClientReading(Socket socket) {
		// TODO Auto-generated method stub
		System.out.println("client reading");
	}

	@Override
	public void HaveClientReadComplete(Socket socket, byte[] headBytes, byte[] contentBytes) {
		// TODO Auto-generated method stub
		try {
			String headstr = new String(headBytes,"utf-8");
			String contentstr = new String(contentBytes,"utf-8");
			System.out.println("client read complete:" +headstr+" "+contentstr);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void HaveClientReadTimeOut(Socket socket) {
		// TODO Auto-generated method stub
		System.out.println("ClientReadTimeOut");
	}

	@Override
	public void HaveClientDisConnection(Socket socket) {
		// TODO Auto-generated method stub
		System.out.println("ClientDisConnection");
	}

}
