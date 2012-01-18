package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.hsrm.mi.mobcomp.y2k11grp04.model.BaseModel;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

public class GroupMoodActivity extends ServiceActivity {
	BaseModel currentMeeting;

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
									Uri.parse("groupmood.attendee://10.0.2.2:8000/groupmood/meeting/1")));
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
		if (scanResult != null && scanResult.getContents() != null) {
			try {
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(scanResult.getContents())));
			} catch (ActivityNotFoundException e) {
				Toast.makeText(getApplicationContext(),
						R.string.toast_no_groupmood_qrcode, Toast.LENGTH_LONG)
						.show();
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.toast_no_scan,
					Toast.LENGTH_LONG).show();
		}
	}

	public static final int DIALOG_LOADING = 1;
	private boolean chairRequested = false;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING:
			Dialog d = new ProgressDialog(this);
			d.setTitle(R.string.open_meeting);
			return d;
		default:
			return super.onCreateDialog(id);
		}

	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case MoodServerService.MSG_MEETING_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					removeDialog(DIALOG_LOADING);
					// The remote service is a separate os process.
					// Therefore, the current classloader has to be used by
					// the unparcelling process.
					Bundle b = serviceMessage.getData();
					b.setClassLoader(getClassLoader());
					currentMeeting = b
							.getParcelable(MoodServerService.KEY_MEETING_MODEL);
					// Starte Activity entsprechend dem vorher ausgewählten
					// Schema.
					Intent next = new Intent(getApplicationContext(),
							chairRequested ? ChairActivity.class
									: AttendeeActivity.class);
					next.putExtras(b);
					startActivity(next);
					finish();
				}
			};
		case MoodServerService.MSG_ERROR:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					removeDialog(DIALOG_LOADING);
					Toast.makeText(
							getApplicationContext(),
							serviceMessage.getData().getString(
									MoodServerService.KEY_ERROR_MESSAGE),
							Toast.LENGTH_LONG).show();
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
			if (currentMeeting == null) {
				chairRequested = groupMoodUri.toString().contains("chair");
				showDialog(DIALOG_LOADING);
				loadMeeting(groupMoodUri);
			}
		}
	}

	/**
	 * Wenn die Activity beendet wird, merken wir uns wichtige Objekte, damit
	 * wird diese nicht mehr neu laden müssen.
	 * 
	 * Das kann z.B. wichtig sein, wenn ein Dialog angezeigt wird, und der
	 * Nutzer das Device dreht, dann kann das OS die komplette VM zerstören,
	 * startet die Activity neu und zeigt DIREKT den Dialog an - jetzt gibt es
	 * das Objekt nicht mehr und müsste neu geladen werden.
	 * 
	 * Statt dessen kann man auf dem InstanceState Daten ablegen, die dann mit
	 * onRestoreInstanceState() wieder ausgelesen werden können.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (currentMeeting != null) {
			outState.putParcelable(MoodServerService.KEY_MEETING_MODEL,
					currentMeeting);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(MoodServerService.KEY_MEETING_MODEL)) {
			currentMeeting = savedInstanceState
					.getParcelable(MoodServerService.KEY_MEETING_MODEL);
		}
	}
}