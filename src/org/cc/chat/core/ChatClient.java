package org.cc.chat.core;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 客户端
 * @author cc
 *
 */
public class ChatClient extends JFrame implements CommandRepo {

	public static void main(String[] args) {
		
		
		ChatClient client=new ChatClient();
		
	}
	
	public ChatClient() {
		
		connect();
		launchFrame();
		
	}
	
	//输入框
	private TextField textField;
	//显示框
	private TextArea textArea;
	
	//初始化GUI
	public void launchFrame(){
		
		int w=444; int h=500;
		
		setLocation(LocationUtil.getCenterLocation(w,h));
		setSize(w,h);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setTitle("局域网聊天系统  - 随机昵称");
		
		setLayout(new BorderLayout());
		
		textField=new TextField();
		textArea=new TextArea();
		
		textField.addActionListener(new InputListener());
		
		textArea.setFont(new Font("Courier New",Font.PLAIN,13));
		textArea.setEditable(false);
		textArea.setColumns(10);
		
		add(textArea,BorderLayout.CENTER);
		add(textField,BorderLayout.SOUTH);
		
		try {
			BufferedImage icon=ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/cc/chat/resource/chat_icon.png"));
			setIconImage(icon);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		pack();
		
		setVisible(true);

		textField.requestFocus();
		
		//启动一个进程监听服务器发来的消息回送
		new Thread(new ReceiveListener()).start();
		
	}
	
	private Socket socket;  
	private DataOutputStream dos;
	private DataInputStream dis;
	
	//连接到服务器
	public void connect(){
		try {
			connected=true;
			socket=new Socket("127.0.0.1",8888);
			dos=new DataOutputStream(socket.getOutputStream());
			dis=new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,"不能连接到服务器","网络错误",JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}
	
	//清理连接..
	public void disconnect(){
		try {
			connected=false;
			dos.close();
			dis.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//接收信息，显示出来
	public void receiveMessage(String message){
		
		//检测服务器回送消息
		if(StringUtil.startWithIgnoreCase(message,COMMAND_NICKNAME)){
			//修改昵称
			String nickname=message.replace(COMMAND_NICKNAME,"").trim();
			setTitle("局域网聊天系统  - "+nickname);
			return ;
		}
		
		
		//对内容加了自动换行
		int count=0;
		StringBuilder formatedMessage=new StringBuilder();
		for(int i=0;i<message.length();i++){
			char c=message.charAt(i);
			formatedMessage.append(c);
			count= (c=='\n'?0:count+(StringUtil.isLetterOrDigit(c)?1:2));
			if(count>=getWidth()/8){
				formatedMessage.append('\n');
				count=0;
			}
		}
		textArea.append(formatedMessage.toString());
	}
	
	//发送信息
	public void sendMessage(String message){
		try {
			dos.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	//用于监听用户的输入事件
	private class InputListener implements ActionListener {

		//上次发送信息的时间
		private long lastSendMessageTime;
		
		public InputListener() {
			lastSendMessageTime=new Date().getTime();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(new Date().getTime()-lastSendMessageTime<500){
				StringBuilder message=new StringBuilder();
				message.append("\n温馨提示:\n").append("发送信息过于频繁会降低友善度...").append("\n\n");
				receiveMessage(message.toString());
				return ;
			}
			
			String inputContent=textField.getText().trim();
//			String message=System.getProperty("user.name")+" "+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
//											.format(new Date())+"\n"+inputContent+"\n";
			
			textField.setText("");
			
			if(inputContent.isEmpty()){
				return;
			}else if(inputContent.length()>200){
				StringBuilder message=new StringBuilder();
				message.append("\n系统提示：\n").append("信息过长，最多200字。").append("\n\n");;
				receiveMessage(message.toString());
				textField.setText(inputContent);
				return ;
			}else if(StringUtil.startWithIgnoreCase(inputContent,COMMAND_NICKNAME)){
				
			}
			
			lastSendMessageTime=new Date().getTime();
			sendMessage(inputContent);
		}
		
	}
	
	//是否已经连接到服务器
	private boolean connected;
	
	//监听服务器的消息回送
	private class ReceiveListener implements Runnable{

		@Override
		public void run() {
			try {
				while(connected){
					String message=dis.readUTF();
					receiveMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					connected=false;
					dos.close();
					dis.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
