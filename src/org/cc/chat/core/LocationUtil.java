package org.cc.chat.core;

import java.awt.Point;
import java.awt.Toolkit;

/**
 * 计算位置的工具类
 * @author cc
 *
 */
public class LocationUtil {

	/**
	 * 传入窗口的宽度和高度，计算若窗口处于屏幕正中位置的话左上角坐标为多少
	 * @param width
	 * @param height
	 */
	public static Point getCenterLocation(int width,int height){
		
		int w=Toolkit.getDefaultToolkit().getScreenSize().width;
		int h=Toolkit.getDefaultToolkit().getScreenSize().height;
		
		return new Point(w/2-width/2,h/2-height/2);
	}
	
}
