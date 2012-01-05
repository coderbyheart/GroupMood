package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

public class GroupMoodActivity extends ServiceActivity {
	Meeting m;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.scan_button))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						if ("google_sdk".equals(Build.PRODUCT)) {
							// Geht auf Debug nicht
							startActivity(new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("groupmood://10.0.2.2:8000/demoserver/meeting/1")));
						} else {
							IntentIntegrator integrator = new IntentIntegrator(
									GroupMoodActivity.this);
							integrator.initiateScan();
						}
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanResult != null) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult
					.getContents())));
		}
	}

	public static final int DIALOG_LOADING = 1;
	public static final int DIALOG_MEETING = 2;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING:
			Dialog d = new ProgressDialog(this);
			d.setTitle(R.string.open_meeting);
			return d;
		case DIALOG_MEETING:
			Dialog d2 = new Dialog(this);
			d2.setTitle(m.getName() + " " + m.getUri().toString());
			d2.setCancelable(true);
			return d2;
		default:
			return super.onCreateDialog(id);
		}

	}

	private void loadMeeting(Uri groupMoodUri) {
		Message m = Message.obtain(null, MoodServerService.MSG_MEETING);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_MEETING_URI,
				groupMoodUri.toString());
		m.setData(data);
		sendMessage(m);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case MoodServerService.MSG_MEETING_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					removeDialog(DIALOG_LOADING);
					m = new Meeting();
					m.setName(serviceMessage.getData().getString(
							MoodServerService.KEY_MEETING_NAME));
					m.setId(serviceMessage.getData().getInt(
							MoodServerService.KEY_MEETING_NAME));
					m.setUri(Uri.parse(serviceMessage.getData().getString(
							MoodServerService.KEY_MEETING_URI)));
					showDialog(DIALOG_MEETING);
				}
			};
		default:
			return null;
		}
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		Uri groupMoodUri = getIntent().getData();
		if (groupMoodUri != null) {
			showDialog(DIALOG_LOADING);
			Log.v(getClass().getCanonicalName(), groupMoodUri.toString());
			loadMeeting(groupMoodUri);
		}
	}
}