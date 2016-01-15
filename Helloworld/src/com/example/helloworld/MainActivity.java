package com.example.helloworld;

import java.util.List;

import kkdownloader.DownloadManager;
import kkdownloader.DownloadThread;
import roboguice.activity.RoboActivity;
import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;


@ContentView(R.layout.activity_main)
public class MainActivity extends Activity {
	
    
	ListView listView;
	Button button;
	
	class TestAdapter extends BaseAdapter {
        
		List<DownloadThread> dataSource;

		class ViewHolder {
			Button button1;
			Button button2;
			Button button3;
			TextView textView1;
			TextView textView2;
		}
		
		public TestAdapter() {
			
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dataSource != null ? dataSource.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return dataSource != null ? dataSource.get(position): null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder viewHolder;
			if(convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.array_list, null);
			    viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			    viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
			    viewHolder.button1 = (Button) convertView.findViewById(R.id.button1);
			    viewHolder.button2 = (Button) convertView.findViewById(R.id.button2);
			    viewHolder.button3 = (Button) convertView.findViewById(R.id.button3);
			    convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			DownloadThread downloadThread = dataSource.get(position);
			
			viewHolder.textView1.setText(downloadThread.downloadInfo.name);
			viewHolder.textView2.setText(downloadThread.downloadInfo.spercent() + "               " + downloadThread.speed);
			
			
			viewHolder.button1.setTag(downloadThread);
			
			//暂停 继续下载
			viewHolder.button1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DownloadThread downloadThread = (DownloadThread)v.getTag();
					if(!downloadThread.stop){
						((Button)v).setText("继续");
						DownloadManager.getInstance().stopDownload(downloadThread);
					} else {
						((Button)v).setText("暂停");
						DownloadManager.getInstance().resumeDownload(downloadThread);
					}
				}	
			});
			
			viewHolder.button2.setTag(downloadThread);
			
			//取消下载
			viewHolder.button2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DownloadThread downloadThread = (DownloadThread)v.getTag();
					DownloadManager.getInstance().cancelDownload(downloadThread);
					TestAdapter.this.dataSource = DownloadManager.getInstance().getDownloadThreads();
					TestAdapter.this.notifyDataSetChanged();
				}
			});
			
			viewHolder.button3.setTag(downloadThread);
			
			//重新下载
			viewHolder.button3.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DownloadThread downloadThread = (DownloadThread)v.getTag();
					DownloadManager.getInstance().reStartDownload(downloadThread);
					
				}	
			});
			return convertView;
		}	
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//0.
		listView = (ListView)findViewById(R.id.listView1);
		final TestAdapter testAdapter = new TestAdapter();
		
		//1.获取DownloadManager实例 建议在application中初始化
		final DownloadManager downloadManager = DownloadManager.getInstance(this);
		
		//1)a设置数据源为downloadManager.getDownloadThreads();
		testAdapter.dataSource = downloadManager.getDownloadThreads();
		listView.setAdapter(testAdapter);
		
		
		//2.更新ui 可以调用多次
		downloadManager.refreshUI(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				testAdapter.dataSource = downloadManager.getDownloadThreads();
				testAdapter.notifyDataSetChanged();
			}
		});
		
		//3.添加下载
		downloadManager.startDownload("http://apk500.bce.baidu-mgame.com/game/149000/149833/20151209174425_oem_5001424.apk");
		downloadManager.startDownload("http://apk500.bce.baidu-mgame.com/game/903000/903696/20160113104703_oem_5001424.apk");
		downloadManager.startDownload("http://xz.i8543.net/371926/apk/huanledoudizhu.apk");
		downloadManager.startDownload("http://ayx1.cr173.com//lxfcq.apk");
		downloadManager.startDownload("https://dn-anfanw.qbox.me/hjtqdkn_afk0113.apk");
		downloadManager.startDownload("https://dn-anfanw.qbox.me/jlgj_afk1109.apk");
		downloadManager.startDownload("https://dn-anfanw.qbox.me/ssjx_afk1230.apk");
		downloadManager.startDownload("https://dn-anfanw.qbox.me/xjwqz_afk1231.apk");
		
		//4.停止ui刷新
		//downloadManager.setStopRefreshUI(true);		
	}
	

	
}
