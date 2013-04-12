package xmu.swordbearer.andcoin;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class SelectActivity extends ListActivity {

	private TextView textview_title;
	private Button btn_ok;
	private Button btn_close;
	private Button btn_setup;

	List<ApplicationInfo> appInfos = null;

	String process_name_pre = "andcoin_process_name_";
	String andcoin_shareStr = "andcoin_share";
	String process_countStr = "andcoin_process_count";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select);

		textview_title = (TextView) findViewById(R.id.textview_title);
		btn_ok = (Button) findViewById(R.id.btn_confirm);
		btn_close = (Button) findViewById(R.id.btn_close);
		btn_setup = (Button) findViewById(R.id.btn_setup);

		List<String> appNames = getApplicationNameList();
		this.setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, appNames));

		textview_title.setText("请选择要查看的程序 (" + getListAdapter().getCount()
				+ ")");
		startFloatService();
		initListener();

		MobclickAgent.onError(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	private void initListener() {
		btn_ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getSelectedProcessList();
				startFloatService();
			}
		});
		btn_close.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopFloatService();
			}
		});
		btn_setup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(SelectActivity.this, SetupActivity.class);
				startActivity(intent);
			}
		});
	}

	// 启动并且绑定服务
	private void startFloatService() {
		Intent intent = new Intent();
		intent.setClass(SelectActivity.this, FloatService.class);
		startService(intent);
		showToast("启动服务 ");
	}

	private void stopFloatService() {
		Intent serviceStop = new Intent();
		serviceStop.setClass(SelectActivity.this, FloatService.class);
		stopService(serviceStop);
		showToast("停止服务");
	}

	/*
	 */
	private void getSelectedProcessList() {
		long[] processesIds = getListView().getCheckItemIds();
		int count = processesIds.length;
		if (count == 0) {
			showToast("请选择程序");
			return;
		}
		if (count > 5) {
			showToast("最多选择5个程序");
			return;
		}
		SharedPreferences share = this.getSharedPreferences(andcoin_shareStr,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = share.edit();
		editor.clear();
		editor.putInt(process_countStr, count);
		String processNameStr = null;
		for (int i = 0; i < count; i++) {
			processNameStr = appInfos.get((int) processesIds[i]).processName;
			editor.putString(process_name_pre + i, processNameStr);
		}
		Toast.makeText(this, "保存的进程数有 " + count + " 个", Toast.LENGTH_SHORT)
				.show();
		editor.commit();
		finish();
	}

	// 获得所有程序的名称
	private List<String> getApplicationNameList() {
		List<String> processesName = new ArrayList<String>();
		PackageManager pm = getApplication().getPackageManager();
		appInfos = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (ApplicationInfo info : appInfos) {
			processesName.add(info.loadLabel(pm).toString());
		}
		return processesName;
	}

	private void showToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
}
