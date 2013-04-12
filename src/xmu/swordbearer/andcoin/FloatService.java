package xmu.swordbearer.andcoin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xmu.swordbearer.andcoin.data.ProcessInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class FloatService extends Service {
	String LOG = "FloatActivity";

	String process_name_pre = "andcoin_process_name_";
	String andcoin_shareStr = "andcoin_share";
	String process_countStr = "andcoin_process_count";

	WindowManager wm = null;
	WindowManager.LayoutParams wmParams = null;
	FloatView floatView;

	private int runningProcessCount = 0;

	private float x, y;// 坐标
	private float startX, startY;

	private List<ProcessInfo> processInfos;

	private ActivityManager aManager;
	private static long totalMem;// 总的内存
	private long availMem;// 可用的内存
	// private int cpu;// CPU占用
	private int delayTime = 1000;

	// FloatView 的配置信息

	private Handler refreshHandler;
	private Runnable refreshRunnable;

	public void onCreate() {
		Log.e("FloatService", "创建Service");
		super.onCreate();
		aManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		init();
		initRefreshHandler();
		// youment
		MobclickAgent.onError(this);
	}

	private void init() {
		floatView = new FloatView(this);
		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wmParams = new WindowManager.LayoutParams();
		wmParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
				200, LayoutParams.TYPE_SYSTEM_ALERT,
				LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wm.addView(floatView, wmParams);
		// binder,作为外部调用此Service的接口
		floatView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY() - 25;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					System.out.println("Touch Me ");
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
				}
				return false;
			}
		});
		totalMem = getTotalMemory();
		Log.e(LOG, "系统总内存为 " + totalMem);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("FloatService", "启动 Service");
		super.onStart(intent, startId);
		this.getProcessNames();
	}

	private void initRefreshHandler() {
		refreshHandler = new Handler();
		refreshRunnable = new Runnable() {
			public void run() {
				updateData();
				refreshHandler.postDelayed(refreshRunnable, delayTime);
			}
		};
		refreshHandler.postDelayed(refreshRunnable, delayTime);
	}

	private void updateData() {
		getProcessInfos();
		getAvailMemory();
		refreshViews();
	}

	private void updateViewPosition() {
		wmParams.x = (int) (x - startX);
		wmParams.y = (int) (y - startY);
		wm.updateViewLayout(floatView, wmParams);
	}

	// 服务刚启动时，加载上次保存的进程Name
	private void getProcessNames() {
		SharedPreferences share = this.getSharedPreferences("andcoin_share",
				Context.MODE_PRIVATE);
		int process_count = share.getInt(process_countStr, 0);
		processInfos = new ArrayList<ProcessInfo>();

		List<RunningAppProcessInfo> runningAppProcessInfos = aManager
				.getRunningAppProcesses();
		runningProcessCount = runningAppProcessInfos.size();
		String temp = "";
		ProcessInfo pInfo;
		for (int i = 0; i < process_count; i++) {
			pInfo = new ProcessInfo();
			temp = share.getString(process_name_pre + i, "");
			// System.out.println("获得的进程为 " + temp);
			pInfo.setProcessName(temp);
			for (RunningAppProcessInfo inf : runningAppProcessInfos) {
				if (inf.processName.equals(temp)) {
					pInfo.setPid(inf.pid);
					System.out.println("获得的进程为 " + temp + " " + inf.pid);
					break;
				} else {
					pInfo.setPid(0);
				}
			}
			processInfos.add(pInfo);
		}
		Toast.makeText(this, "从SharedPre中得到的进程数有 " + process_count,
				Toast.LENGTH_SHORT).show();
	}

	public void test() {
		Toast.makeText(this, "FloatService Method", Toast.LENGTH_SHORT).show();
		Log.e(LOG, "调用Service中的方法");
	}

	private boolean isNewProcessStarted() {
		List<RunningAppProcessInfo> runningAppProcessInfos = aManager
				.getRunningAppProcesses();
		if (runningProcessCount != runningAppProcessInfos.size()) {
			return true;
		} else {
			return false;
		}
	}

	// 获得进程的内存使用情况
	private synchronized void getProcessInfos() {
		if (isNewProcessStarted()) {
			getProcessNames();
		}
		// 获得选择的进程的内存信息
		int size = processInfos.size();
		ProcessInfo pInfo;
		Debug.MemoryInfo[] infos = null;
		for (int i = 0; i < size; i++) {
			pInfo = processInfos.get(i);
			int[] processId = { pInfo.getPid() };
			infos = aManager.getProcessMemoryInfo(processId);
			pInfo.setMemSize(infos[0].getTotalPrivateDirty());
		}
	}

	// 获取系统可用内存
	private long getAvailMemory() {
		long temp;
		MemoryInfo outInfo = new MemoryInfo();
		aManager.getMemoryInfo(outInfo);
		temp = outInfo.availMem / 1024;
		availMem = temp;
		return temp;
	}

	public static long getTotalMemory() {
		long mTotal;
		// 系统内存
		String path = "/proc/meminfo";
		// 存储器内容
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				// 采集内存信息
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// beginIndex
		int begin = content.indexOf(':');
		// endIndex
		int end = content.indexOf('k');
		// 采集数量的内存
		content = content.substring(begin + 1, end).trim();
		// 转换为Int型
		mTotal = Integer.parseInt(content);
		return mTotal;
	}

	private void refreshViews() {
		List<String> dataList = new ArrayList<String>();
		for (ProcessInfo process : processInfos) {
			dataList.add(process.getProcessName() + "  " + process.getMemSize());
		}
		floatView.displayData(totalMem, availMem, dataList, Color.WHITE, 32);
	}

	@Override
	public void onDestroy() {
		wm.removeView(floatView);
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}
