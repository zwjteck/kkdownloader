package kkdownloader;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;

/**
 * Created by zhangkai on 16/1/13.
 */
public class DownloadThread extends Thread {
    public DownloadInfo downloadInfo;
   
    public  Date   startTime;
    public  String speed;     //下载速度
    private int    downbyte;  //已下载字节
    
    public boolean cancel; //是否取消下载
    public boolean stop;   //是否停止下载
    public Object  synTag; //同步tag
    
    public static final String STORE_PATH = "/xixidownload/";
    
    public DownloadThread(){
    	startTime = new Date(System.currentTimeMillis());
    	speed = "";
    	synTag = new Object();
    }
    
    public DownloadThread(DownloadInfo downloadInfo){
    	this();
        this.downloadInfo = downloadInfo;
        if(downloadInfo.url == null){
        	downloadInfo.url = "";
        }
    }
    
    public DownloadThread(String url){
    	this();
        this.downloadInfo =new DownloadInfo();
        this.downloadInfo.name = url.substring(url.lastIndexOf("/") + 1, url.length());
        this.downloadInfo.url = url;
        if(downloadInfo.url == null){
        	downloadInfo.url = "";
        }
    }
    
    @Override
    public void run() {
        downLoadFile();
    }
    
    //文件否下载结束
    public boolean isDownloadEnd(){
    	return fileExist();
    }

    //唤醒线程
    public void resumeThread(){
    	synchronized (synTag) {
    		this.stop = false;
    		synTag.notify();
		}
    }
    
    //获得存储路径
    private String getStorePath(){
    	 final String dirName = Environment.getExternalStorageDirectory() + STORE_PATH;
         File tmpFile = new File(dirName);
         if (!tmpFile.exists()) {
             tmpFile.mkdir();
         }
         return dirName;
    }
    
    //创建文件
    public File createFile(){
    	 final String fileName = downloadInfo.name;
         final String dirName = getStorePath();
         File file = new File(dirName + fileName);
         return file;
    }
    
    
    //检测文件是否成功下载
    public boolean fileExist(){
    	 final File file = createFile();
    	 return fileExist(file);
    }
    
    public boolean fileExist(File file){
    	if(file.exists() && file.length() == downloadInfo.totalLength){
        	downloadInfo.alreadyDownloadLength = downloadInfo.totalLength; //校验
   		 	return true;
    	}
    	return false;
    }
    
    //下载文件
    private File downLoadFile() {	
    	stop = false;
    	cancel = false;
    	speed = "";
    	downbyte = 0;
        final File file = createFile();
        try {
            URL url = new URL(downloadInfo.url);
            try {
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                downloadInfo.totalLength = conn.getContentLength();
                if(fileExist()){
                	return file;
                }
                downloadInfo.alreadyDownloadLength = 0;
                startTime = new Date(System.currentTimeMillis());
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(file);
                byte[] bs = new byte[1024];
                int len;
                synchronized (synTag) {
                	while ((len = is.read(bs)) != -1) {
                		if(cancel){ 
                			return null; 
                		}
                		if(stop){
                			synTag.wait();
                		}
                		downloadInfo.alreadyDownloadLength += len;
                		os.write(bs, 0, len);
                	}
                }
                conn.disconnect();
                conn = null;
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return file;
    }
    
    //获取下载速度
    public void getNetSpeed(){
    	if(stop){
    		return;
    	}
    	if(cancel){
    		return;
    	}
    	if(isDownloadEnd()){
    		speed = "";
    	} else {
    		Date currTime = new Date(System.currentTimeMillis());
    		float diff = (currTime.getTime() - startTime.getTime()) / 1000.f;
    		startTime = currTime;
    		
    		float fspeed =  downloadInfo.sizeMB(downloadInfo.alreadyDownloadLength - downbyte) / diff;
    		downbyte = downloadInfo.alreadyDownloadLength;
    		if(fspeed <= 0){
    			return;
    		}
    		speed = downloadInfo.fdecimal(fspeed) + "kb/s" ;
    	}
    }
}
