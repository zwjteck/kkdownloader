package kkdownloader;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;


/**
 * Created by zhangkai on 16/1/13.
 */
public class DownloadManager {
	 private final static int POOL_SIZE = 10;
	 private List<DownloadThread> downloadThreads;
	 private ExecutorService fixedThreadPool;

     private static DBHelper dbHelper;
     
     /**********DownloadManager实现单例模式    开始**********/
     private static class DownloadManagerHolder {    
    	 private static final DownloadManager INSTANCE = new DownloadManager();    
     }
     
     private DownloadManager(){
    	 downloadThreads = new ArrayList<DownloadThread>();
    	 fixedThreadPool = Executors.newFixedThreadPool(POOL_SIZE);
    	 if(dbHelper != null){
    		List<DownloadInfo> downloadInfos = dbHelper.queryDownloadInfos();
    		for (DownloadInfo downloadInfo : downloadInfos) {
    			DownloadThread downloadThread = new DownloadThread(downloadInfo);
    			downloadThreads.add(downloadThread);
    			if(!downloadThread.isDownloadEnd()){
    				fixedThreadPool.execute(downloadThread);
    			}
			}
    	 }
     }  
     
     public static final DownloadManager getInstance() {    
         return DownloadManagerHolder.INSTANCE;    
     }
     
     public static final DownloadManager getInstance(Context context){
    	 dbHelper = DBHelper.getInstance(context);
         return DownloadManagerHolder.INSTANCE;     
     }
     /**********DownloadManager实现单例模式    结束**********/
     
     //获取下载任务信息
     public List<DownloadThread> getDownloadThreads(){
    	 return downloadThreads;
     }
     
     //开始下载任务
     public void startDownload(String url){
    	 DownloadThread downloadThread = new DownloadThread(url);
    	 startDownload(downloadThread);
     }
     
     public void startDownload(DownloadInfo downloadInfo){
    	 DownloadThread downloadThread = new DownloadThread(downloadInfo);
    	 startDownload(downloadThread);
     }
     
     public void startDownload(DownloadThread downloadThread){
        if(!checkDownloadInfoIsExist(downloadThread.downloadInfo)){
        	dbHelper.insertDownloadInfo(downloadThread.downloadInfo);
            downloadThreads.add(downloadThread);
        }
        if(!downloadThread.isDownloadEnd()){
        	fixedThreadPool.execute(downloadThread);
        }
     }
     
     //暂停下载
     public void stopDownload(DownloadThread downloadThread){
    	  downloadThread.stop = true;
     }
     
     //唤醒下载
     public void resumeDownload(DownloadThread downloadThread){
    	 downloadThread.resumeThread();
     }
     
    //重新下载
     public void reStartDownload(DownloadThread downloadThread){
    	 downloadThread.downloadInfo.alreadyDownloadLength = 0;
    	 fixedThreadPool.execute(downloadThread);
     }
     
     //取消下载 - 删除
     public void cancelDownload(DownloadThread downloadThread){
    	 for(int i = 0 ; i < downloadThreads.size() ; i++){
    		 DownloadThread dThread = downloadThreads.get(i);
    		 if(dThread.getId() == downloadThread.getId()){
    		 	 downloadThreads.remove(i);
    		 }
    	 }
    	 dbHelper.removeDownloadInfo(downloadThread.downloadInfo);
    	 downloadThread.cancel = true;
     }
    
     //获取下载速度
     public void getDownloadSpeeds(){
    	 for (DownloadThread downloadThread : DownloadManager.getInstance().downloadThreads) {
			downloadThread.getNetSpeed();
    	 }
     }
     
     
     /**********UI更新管理  开始*****************/
     //DownloadManager只能在主线程实例化
     
     //主线程handler 
     private static final Handler handler = new Handler();
     private static final int TIME = 2000;
     
     //停止刷新ui
     private boolean isStopRefreshUI = false;
     public void setStopRefreshUI(boolean stop){
    	 isStopRefreshUI = stop;
     }
     
     //刷新ui
     public void refreshUI(final Runnable runnable){
    	 Runnable run = new Runnable() {  
    	        @Override  
    	        public void run() {  
    	            try {
    	            	if(isStopRefreshUI){
    	            		return;
    	            	}
    	            	if(uiNeedToRefresh()){
    	            		getDownloadSpeeds();
    	            		runnable.run();	
    	            	}
    	            	dbHelper.updateDownloadInfos();   
    	            	handler.postDelayed(this, TIME);
    	            } catch (Exception e) {  
    	                // TODO Auto-generated catch block  
    	                e.printStackTrace();  
    	                System.out.println("exception...");  
    	            }  
    	        }  
    	 };
    	 handler.postDelayed(run, TIME);  
     }
     
     //是否需要更新ui
     private int total;
     public boolean uiNeedToRefresh(){
    	 int tmpTotal = 0; 
    	 for (DownloadThread downloadThread : downloadThreads) {
 			DownloadInfo dInfo = downloadThread.downloadInfo;
 			tmpTotal += dInfo.alreadyDownloadLength;
 		 }
    	 if(total != tmpTotal){
    		 return true;
    	 }
    	 total = tmpTotal;
    	 return false;
     }
     /**********UI更新管理  结束*****************/
 
     //检测下载信息是否存在
     private boolean checkDownloadInfoIsExist(DownloadInfo downloadInfo){
    	 boolean exist = false;
    	 for (DownloadThread downloadThread : downloadThreads) {
			DownloadInfo dInfo = downloadThread.downloadInfo;
			if(dInfo.url.equals(downloadInfo.url)){
				exist = true;
				break;
			}
		}
    	return exist;
     }
     
     //下载已完成列表
     public List<DownloadThread> getDownloaded(){
    	 List<DownloadThread> end = new ArrayList<DownloadThread>();
    	 for(DownloadThread downloadThread : downloadThreads){
    		 if(downloadThread.isDownloadEnd()){
    			 end.add(downloadThread);
    		 }
    	 }
    	 return end;
     }
     
    //下载未完成列表
     public List<DownloadThread> getDownloading(){
    	 List<DownloadThread> ing = new ArrayList<DownloadThread>();
    	 for(DownloadThread downloadThread : downloadThreads){
    		 if(!downloadThread.isDownloadEnd()){
    			 ing.add(downloadThread);
    		 }
    	 }
    	 return ing;
     }
     
     //sqlite数据管理类
     static class DBHelper extends SQLiteOpenHelper {
    	 private static final String DB_NAME    = "download.db";
    	 private static final String TABLE_NAME = "downloadInfo";
    	 private static final int version = 1;
    	 
    	 private static  DBHelper INSTANCE;    
         public static final DBHelper getInstance(Context context) {  
        	 if(INSTANCE == null ){
        		 INSTANCE = new DBHelper(context);
        	 }
            return INSTANCE;    
         }
    	 
         public DBHelper(Context context) {
     		super(context, DB_NAME, null, version);
     	}

     	@Override
     	public void onCreate(SQLiteDatabase db) {
    		 db.execSQL("DROP TABLE IF EXISTS "  + TABLE_NAME);
    		 db.execSQL("CREATE TABLE " + TABLE_NAME +" (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, name TEXT, totalLength INTEGER, alreadyDownloadLength INTEGER,icon BLOB)"); 
     	}

     	@Override
     	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

     	}
         
    	public void insertDownloadInfo(DownloadInfo downloadInfo){
    		 List<DownloadInfo> downloadInfos = queryDownloadInfos();
    		 for(DownloadInfo doInfo : downloadInfos){
    			 if(doInfo.url.equals(downloadInfo.url)){
    				 return;
    			 }
    		 }
    		 SQLiteDatabase db = this.getWritableDatabase();
    		 ContentValues cValues = new ContentValues();
    		 cValues.put("url", downloadInfo.url);
    		 cValues.put("name", downloadInfo.name);
    		 cValues.put("totalLength", downloadInfo.totalLength);
    		 cValues.put("alreadyDownloadLength", downloadInfo.alreadyDownloadLength);
    		 if(downloadInfo.icon != null){
    			 ByteArrayOutputStream baOs = new ByteArrayOutputStream(); 
    			 downloadInfo.icon.compress(Bitmap.CompressFormat.PNG, 100, baOs);  
    			 cValues.put("icon",  baOs.toByteArray());
    		 }
    		 db.insert(TABLE_NAME, null, cValues);
    		 db.close();
    	 }
    	 
    	 public void removeDownloadInfo(DownloadInfo downloadInfo){
    		 SQLiteDatabase db = this.getWritableDatabase();
    		 String whereClause = "url=?";
    		 String[] whereArgs = {downloadInfo.url};
    		 db.delete(TABLE_NAME, whereClause, whereArgs);  
    		 db.close();
    	 }
    	 
    	 public void updateDownloadInfo(DownloadInfo downloadInfo){
    		 SQLiteDatabase db = this.getWritableDatabase();
    		 ContentValues cValues = new ContentValues();
    		 cValues.put("totalLength", downloadInfo.totalLength);
    		 cValues.put("alreadyDownloadLength", downloadInfo.alreadyDownloadLength);
    		 String whereClause = "url=?";
    		 String[] whereArgs = {downloadInfo.url};
    		 db.update(TABLE_NAME, cValues , whereClause, whereArgs);  
    	 }
    	 
    	 public void updateDownloadInfos(){
    		 SQLiteDatabase db = this.getWritableDatabase();
    		 for (DownloadThread downloadThread : DownloadManager.getInstance().downloadThreads) {
    				  DownloadInfo dInfo = downloadThread.downloadInfo;
    				  updateDownloadInfo(dInfo);
    		 }
    		 db.close();
    	 }
    	 
    	 public List<DownloadInfo> queryDownloadInfos(){
    		 SQLiteDatabase db = this.getWritableDatabase();
    		 List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
    		 Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
    		 for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
    		 {
    			 DownloadInfo downloadInfo = new DownloadInfo();
    			 downloadInfo.url = cursor.getString(1);
    			 downloadInfo.name = cursor.getString(2);
    			 downloadInfo.totalLength = cursor.getInt(3);
    			 downloadInfo.alreadyDownloadLength = cursor.getInt(4);
    			 byte[] in = cursor.getBlob(5); 
    			 if(in != null){
    				 downloadInfo.icon = BitmapFactory.decodeByteArray(in, 0, in.length);
    			 }
    			 downloadInfos.add(downloadInfo);
    		 }
    		 cursor.close();
    		 db.close();
    		 return downloadInfos;
    	 }
     }
  
}
