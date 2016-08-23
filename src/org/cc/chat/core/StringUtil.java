package org.cc.chat.core;

/**
 * 处理字符的工具类
 * @author cc
 *
 */
public class StringUtil {

	/**
	 * 判断s1的开始是否是s2，忽略大小写，因为没有内置函数所以只好自己写了
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean startWithIgnoreCase(String s1,String s2){
		if(s1.length()<s2.length()) return false;
		
		s1=s1.toLowerCase();
		s2=s2.toLowerCase();
		for(int i=0,j=0;i<s1.length() && j<s2.length();i++,j++){
			if(s1.charAt(i)!=s2.charAt(j)) return false;
		}
		return true;
	}
	
	/**
	 * 读取字符串中的第一个词，以空格为分隔符
	 * @param s
	 * @return
	 */
	public static String firstWord(String s){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();i++){
			char c=s.charAt(i);
			if(c!=' ')sb.append(c);
			else return sb.toString();
		}
		return sb.toString();
	}
	
	/**
	 * 是否是字母或数字
	 * @param c
	 * @return
	 */
	public static boolean isLetterOrDigit(char c){
		if(c>='a' && c<='z') return true;
		if(c>='A' && c<='Z') return true;
		if(c>='0' && c<='9') return true;
		return false;
	}
	
}
