# kkdownloader
安卓多线程下载



使用方法如下：


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
