package de.hsrm.mi.mobcomp.y2k11grp04;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.y2k11grp04.extra.ColoringTextWatcher;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.service.DemoServerService;

/**
 * @author Coralie Reuter <coralie.reuter@hrcom.de>
 * @author Markus Tacker <m@tacker.org>
 */
public class MasterRatingActivity extends ServiceActivity implements SensorEventListener {
	private final Integer defaultVote = 50;
	private final Meeting meeting = new Meeting(1, "Demo-Meeting");

	long lastChange = 0;

	private TextView percentTextView;
	private TextView avgVoteTextView;
	private TextView numVotesTextView;

	private SensorManager sensorManager;
	private Sensor sensor;

	private SeekBar seekBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.master_rating);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		percentTextView = (TextView) findViewById(R.id.percentTextView);
		avgVoteTextView = (TextView) findViewById(R.id.avgVoteTextView);
		numVotesTextView = (TextView) findViewById(R.id.numVotesTextView);

		seekBar = (SeekBar) findViewById(R.id.moodSeekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(getClass().getCanonicalName(), "Neue Mood:" + seekBar.getProgress());
				sendVoteMessage();
				sensorManager.registerListener(MasterRatingActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				sensorManager.unregisterListener(MasterRatingActivity.this);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				percentTextView.setText("" + progress + "%");
			}
		});

		new ColoringTextWatcher(percentTextView);
		new ColoringTextWatcher(avgVoteTextView);

		seekBar.setProgress(defaultVote);
		percentTextView.setText(defaultVote + " %");
		avgVoteTextView.setText(defaultVote + " %");
	}

	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case DemoServerService.MSG_VOTE_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Toast.makeText(MasterRatingActivity.this, "Vote bestätigt", Toast.LENGTH_LONG).show();
					avgVoteTextView.setText(""
							+ serviceMessage.getData().getInt(DemoServerService.KEY_MEETING_AVG_VOTE) + "%");
					numVotesTextView.setText(""
							+ serviceMessage.getData().getInt(DemoServerService.KEY_MEETING_NUM_VOTES));
				}
			};
		default:
			return null;
		}
	}

	/**
	 * Bereitet die "Vote"-Nachricht vor
	 */
	public void sendVoteMessage() {
		Log.v(getClass().getCanonicalName(), "Neue Mood:" + seekBar.getProgress());
		Message m = Message.obtain(null, DemoServerService.MSG_VOTE);
		Bundle data = new Bundle();
		data.putInt(DemoServerService.KEY_MEETING_ID, meeting.getId());
		data.putInt(DemoServerService.KEY_VOTE_VOTE, seekBar.getProgress());
		m.setData(data);
		sendMessage(m);

		// Activity nach senden des Wertes schließen.
		finish();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				handleMotion(event.values[0] * -1);
			else
				handleMotion(event.values[1]);
		}
	}

	/**
	 * Verarbeitet Achsen-Bewegungen
	 */
	private void handleMotion(float dif) {
		long curTime = System.currentTimeMillis();

		// Schwelle für Erkennung
		if (dif < -1 || dif > 1) {
			Log.v(getClass().getCanonicalName(), "Achsen-Bewegung: " + dif);
			seekBar.setProgress(seekBar.getProgress() + Math.round(dif));
			// Systemzeit merken
			lastChange = System.currentTimeMillis();
		}

		// Falls aktuelle Systemzeit und letzte Änderung X Millisekunden
		// auseinander liegen..
		if (lastChange != 0 && (curTime - lastChange > 1000)) {
			sendVoteMessage();
			Log.v(getClass().getCanonicalName(), "Bewegung erkannt; neuer Wert gesendet..");
			lastChange = 0;
		}
	}

	/**
	 * @param v
	 */
	public void changeScreenOrientation(View v) {
		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}