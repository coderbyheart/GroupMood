package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiManager;
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
import de.hsrm.mi.mobcomp.y2k11grp04.persistence.DbAdapter;
import de.hsrm.mi.mobcomp.y2k11grp04.persistence.Provider;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

public class LaunchActivity extends ServiceActivity {
	private WifiStateReceiver wsr = new WifiStateReceiver();
	BaseModel currentMeeting;
	public static final int DIALOG_LOADING = 1;
	private Uri lastMeetingUri;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch);

		((Button) findViewById(R.id.groupMood_scan_button))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						if ("google_sdk".equals(Build.PRODUCT)) {
							// Geht auf Debug nicht
							startActivity(new Intent(Intent.ACTION_VIEW, Uri
									.parse("grpmd://login3.mi.hs-rm.de:8001/3")));
						} else {
							IntentIntegrator integrator = new IntentIntegrator(
									LaunchActivity.this);
							integrator.initiateScan();
						}
					}
				});

		// Warnung bei fehlendem Wifi anzeigen
		initWifiWarning();

		// Button mit letztem Meeting anzeigen, falls vorhanden
		Uri historyUrl = Provider.CONTENT_URI.buildUpon()
				.appendPath("meetinghistory").build();
		Cursor historyCursor = getContentResolver().query(historyUrl, null,
				null, null, null);
		if (historyCursor != null) {
			lastMeetingUri = Uri.parse(historyCursor
					.getString(DbAdapter.HISTORY_COL_MEETING_URL));
			Button historyButton = (Button) findViewById(R.id.groupMood_history_button);
			historyButton.setVisibility(View.VISIBLE);
			historyButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View button) {
					startActivity(new Intent(Intent.ACTION_VIEW, lastMeetingUri));
				}
			});
			historyCursor.close();
		}
	}

	/**
	 * Zeigt eine Warnung an, wenn kein Wifi vorhanden ist
	 */
	private void initWifiWarning() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		findViewById(R.id.groupMood_wifi_warning).setVisibility(
				wifiManager.isWifiEnabled() ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(wsr, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(wsr);
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

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING:
			return ProgressDialog.show(LaunchActivity.this, "", getResources()
					.getString(R.string.check_meeting), true);
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
							QuestionActivity.class);
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
				showDialog(DIALOG_LOADING);

				// Uri merken
				ContentValues mNewValues = new ContentValues();
				mNewValues.put(DbAdapter.HISTORY_KEY_MEETING_URL,
						groupMoodUri.toString());
				getContentResolver().insert(
						Provider.CONTENT_URI.buildUpon()
								.appendPath("meetinghistory").build(),
						mNewValues);

				// Uri umwandeln
				Builder u = groupMoodUri.buildUpon().scheme(
						groupMoodUri.toString().contains("+https") ? "https"
								: "http");
				u.path("/groupmood/meeting" + groupMoodUri.getPath());
				loadMeeting(u.build());
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

	/**
	 * Kümmert sich um das Anzeigen des Wifi-Warnings
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class WifiStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			NetworkInfo info = (NetworkInfo) intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				boolean wifiAvailable = info.getState() == NetworkInfo.State.CONNECTED
						|| info.getState() == NetworkInfo.State.CONNECTING;
				findViewById(R.id.groupMood_wifi_warning).setVisibility(
						wifiAvailable ? View.GONE : View.VISIBLE);
			}

		}
	}
}