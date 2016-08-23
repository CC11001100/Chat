package org.cc.chat.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

/**
 * 服务器端
 * @author cc
 *
 */
public class ChatServer implements CommandRepo {
	
	//MAIN IN HERE !
	public static void main(String[] args) {
		
		ChatServer chatServer=new ChatServer();
		chatServer.launch();
		
	}
	
	//存储连接上来的客户端
	private List<Client> clientSets=new ArrayList<Client>();
	//服务器是否在run状态
	private boolean started=false;
	
	//启动服务器,等待客户端连接
	public void launch(){
		ServerSocket serverSocket=null;
		try {
			
			serverSocket=new ServerSocket(8888);
			started=true;
			
			//只要服务器在开启状态，就不断的接收客户端的连接请求
			while(started){
				Socket socket=serverSocket.accept();
				Client client=new Client(socket);
				clientSets.add(client);
				new Thread(client).start();
System.out.println("一个客户端上线了...");				
			}
			
		} catch (BindException e) {
			JOptionPane.showMessageDialog(null,"端口8888已经被占用","警告",JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				started=false;
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String names[]=new String[]{"乔峰","虚竹","段誉","星宿老怪","无崖子","慕容复","张无忌","谢逊","小鱼儿","花无缺","令狐冲","郭靖","杨过"};
	//随机生成一个名字
	public String randName(){
		return names[new Random().nextInt(names.length)]+new Random().nextInt(1000000);
	}
	
	//检查是否重名
	public boolean isDuplication(String name){
		for(int i=0;i<clientSets.size();i++){
			if(name.equalsIgnoreCase(clientSets.get(i).nickname)) return true;
		}
		return false;
	}
	
	//That class to presentation a client
	private class Client implements Runnable {
		
		//昵称
		private String nickname;
		//聊天小尾巴
		private String tail;
		
		
		private Socket socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		//此客户端是否连接到服务器，未连接的应当得到清理
		private boolean connected;
		
		//此客户端最后发送消息的 时间
		private long lastSendMessageTime;
		
		public Client(Socket socket) {
			try {
				
				connected=true;
				this.socket=socket;
				dis=new DataInputStream(socket.getInputStream());
				dos=new DataOutputStream(socket.getOutputStream());
				
				lastSendMessageTime=new Date().getTime();
				
				tail="";
				nickname=randName();
				while(isDuplication(nickname)){
					nickname=randName();
				}
				
				receiveMessage(COMMAND_NICKNAME+" "+nickname);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while(connected){
					
					String content=dis.readUTF();
					
					Date now=new Date();
					
					//判断信息发送的是不是过于频繁，为了避免2B刷屏
					if(now.getTime()-lastSendMessageTime<500){
						StringBuffer message=new StringBuffer();
						message.append("\n系统提示：\n").append("【").append(content)
								.append("】发送失败，原因：发送信息过于频繁。").append("\n\n");
						receiveMessage(message.toString());
						continue;
					}
					
					//更新最后一次的消息发送时间
					lastSendMessageTime=now.getTime();
					
					//格式化一下
					StringBuffer message=new StringBuffer();
					//判定客户端发送的指令
					if(StringUtil.startWithIgnoreCase(content,COMMAND_NICKNAME)){
						
						String command=content;
						
						//修改昵称指令
						content=content.substring(COMMAND_NICKNAME.length()).trim();
						
						//检查是否重名
						if(isDuplication(content)){
							//重名了
							message.append("系统消息：修改失败，昵称").append(content).append("已经存在\n");
							receiveMessage(message.toString());
							continue;
						}
						
						message.append("系统消息:").append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(now))
						.append("\n").append("【").append(nickname).append("】").append("修改昵称为").append("【")
						.append(content).append("】").append("\n");
						
						this.nickname=content;
						
						receiveMessage(command);
						
					}else if(StringUtil.startWithIgnoreCase(content,COMMAND_TAIL)){
						
						//修改聊天小尾巴
						tail=content.substring(COMMAND_TAIL.length()).trim();
						message.append("系统消息：").append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(now))
						.append("\n").append("小尾巴修改成功").append("\n");
						
						//修改小尾巴的事情只通知本人修改成功了即可
						this.receiveMessage(message.toString());
						continue;
						
					}else if(StringUtil.startWithIgnoreCase(content,COMMAND_TO)){
						//发送悄悄话
						content=content.substring(COMMAND_TO.length()).trim();
						String toWho=StringUtil.firstWord(content);
						String mesg=content.substring(toWho.length());
						
						//不做检查，允许对自己发悄悄话，人类已经够寂寞的了...
						
						//找到目标，发送消息
						StringBuffer receiveContent=new StringBuffer();
						receiveContent.append(nickname).append("：").append(mesg).append("\n");
						boolean sendOk=false;
						for(int i=0;i<clientSets.size();i++){
							Client c=clientSets.get(i);
							if(toWho.equals(c.nickname)){
								c.receiveMessage(receiveContent.toString());
								sendOk=true;
								break;
							}
						}
						
						//给客户端反馈
						StringBuffer sb=new StringBuffer();
						sb.append("系统消息：发送悄悄话[").append(mesg).append("]给").append("[").append(toWho).append("]");
						if(sendOk){
							receiveMessage(sb.append("成功\n").toString());
						}else{
							receiveMessage(sb.append("失败\n").toString());
						}
						
						continue;
					}else {
						//普通消息
						message.append(nickname).append(" ").
						append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())).append(" ").append(tail).
						append("\n").append(content).append("\n");
					}
					
					
					//发送给所有的客户端，迭代器会锁定，因为没有写操作，不必进行锁定，所以不宜使用迭代器
//					for(Client c:clientSets){
//						c.receiveMessage(s);
//					}
					for(int i=clientSets.size()-1;i>=0;i--){
						Client c=clientSets.get(i);
						if(c.connected){
							c.receiveMessage(message.toString());
						}else{
							//因为要移除元素，所以只好从后向前遍历这样删除的时候就不会影响到下标啦
							clientSets.remove(i);
						}
					}
				}
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.println("一个客户端下线了...");
				disconnection();
			}finally{
				disconnection();
			}
		}
		
		//客户端接收信息
		public void receiveMessage(String message){
			try {
				dos.writeUTF(message);
			} catch (IOException e) {
				e.printStackTrace();
				disconnection();
			}
		}
		
		//断开连接，清理资源
		public void disconnection(){
			try {
				connected=false;
				dis.close();
				dos.close();
				socket.close();
				clientSets.remove(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
