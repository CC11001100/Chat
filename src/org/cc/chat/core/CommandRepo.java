package org.cc.chat.core;

/**
 * 将一些支持的指令抽取出来放在这里以便于管理维护啥的
 * @author cc
 *
 */
public interface CommandRepo {

	/**修改昵称的命令**/
	public static String COMMAND_NICKNAME=":nickname";
	
	/**修改聊天小尾巴的命令**/
	public static String COMMAND_TAIL=":tail";
	
	/**发送悄悄话的命令**/
	public static String COMMAND_TO=":to";
	
}
