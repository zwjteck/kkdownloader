package	kkdownloader;

import java.io.File;
import java.text.DecimalFormat;

import android.graphics.Bitmap;

/**
 * Created by zhangkai on 16/1/13.
 */
public class DownloadInfo {
	
    public String url;                   //下载路径
    public String name;                  //文件名
    public Bitmap icon;                  //图片
    public int    totalLength;           //文件总大小
    public int    alreadyDownloadLength; //已经下载
    
    public static String END = "完成";
    public static String NaN = ".0";
    
    //百分比
    public float fpercent(){
    	return alreadyDownloadLength*1.0f/totalLength * 100;
    }
    
    public int ipercent(){
    	return (int)(fpercent());
    }
    
    public String spercent(){
    	if(fpercent() >= 100){
    		return END;
    	}
    	return fdecimal(fpercent()) + "%";
    }
    
    //获取mb
    public float sizeMB(int size){
    	return size / 1024.0f;
    }
    
    //精度转换
    public String fdecimal(float fpercent){
    	DecimalFormat decimalFormat = new DecimalFormat("0.0");
    	if(decimalFormat.format(fpercent).contains("NaN")){
    		return NaN;
    	}
    	return decimalFormat.format(fpercent);
    }
}
