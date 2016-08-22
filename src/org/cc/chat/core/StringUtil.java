package org.cc.chat.core;

/**
 * 处理字符的工具类
 * @author cc
 *
 */
public class StringUtil {

	/**
	 * 判断两个字符串的开始是否相同，忽略大小写，因为没有内置函数所以只好自己写了
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean startWithIgnoreCase(String s1,String s2){
		s1=s1.toLowerCase();
		s2=s2.toLowerCase();
		for(int i=0,j=0;i<s1.length() && j<s2.length();i++,j++){
			if(s1.charAt(i)!=s2.charAt(j)) return false;
		}
		return true;
	}
	
}
