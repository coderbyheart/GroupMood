package de.hsrm.mi.mobcomp.y2k11grp04;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
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
public class ClientActivity extends ServiceActivity implements
		SensorEventListener {
	private TextView percentTextView;
	private Integer defaultVote = 50;
	private Meeting meeting = new Meeting(1, "Demo-Meeting");
	private TextView avgVoteTextView;
	private TextView numVotesTextView;

	private SensorManager sensorManager;
	private Sensor sensor;
	long lastChange = 0;

	private SeekBar seekBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		percentTextView = (TextView) findViewById(R.id.percentTextView);
		seekBar = (SeekBar) findViewById(R.id.moodSeekBar);
		avgVoteTextView = (TextView) findViewById(R.id.avgVoteTextView);
		numVotesTextView = (TextView) findViewById(R.id.numVotesTextView);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(getClass().getCanonicalName(),
						"Neue Mood:" + seekBar.getProgress());
				sendVoteMessage();
				sensorManager.registerListener(ClientActivity.this, sensor,
						SensorManager.SENSOR_DELAY_NORMAL);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				sensorManager.unregisterListener(ClientActivity.this);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
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
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case DemoServerService.MSG_VOTE_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Toast.makeText(ClientActivity.this, "Vote bestätigt",
							Toast.LENGTH_LONG).show();
					avgVoteTextView.setText(""
							+ serviceMessage.getData().getInt(
									DemoServerService.KEY_MEETING_AVG_VOTE)
							+ "%");
					numVotesTextView.setText(""
							+ serviceMessage.getData().getInt(
									DemoServerService.KEY_MEETING_NUM_VOTES));
				}
			};
		default:
			return null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	/**
	 * Bereitet die "Vote"-Nachricht vor
	 */
	public void sendVoteMessage() {
		Log.v(getClass().getCanonicalName(),
				"Neue Mood:" + seekBar.getProgress());
		Message m = Message.obtain(null, DemoServerService.MSG_VOTE);
		Bundle data = new Bundle();
		data.putInt(DemoServerService.KEY_MEETING_ID, meeting.getId());
		data.putInt(DemoServerService.KEY_VOTE_VOTE, seekBar.getProgress());
		m.setData(data);
		sendMessage(m);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			handleXMotion(event.values[0] * -1);
		}
	}

	/**
	 * Verarbeitet X-Achsen-Bewegungen
	 * */
	private void handleXMotion(float dif) {
		long curTime = System.currentTimeMillis();

		// Schwelle für Erkennung
		if (dif < -1 || dif > 1) {
			Log.v(getClass().getCanonicalName(), "X-Achsen-Bewegung: " + dif);
			seekBar.setProgress(seekBar.getProgress() + Math.round(dif));
			// Systemzeit merken
			lastChange = System.currentTimeMillis();
		}

		// Falls aktuelle Systemzeit und letzte Änderung X Millisekunden
		// auseinander liegen..
		if (lastChange != 0 && (curTime - lastChange > 1000)) {
			sendVoteMessage();
			Log.v(getClass().getCanonicalName(),
					"Bewegung erkannt; neuer Wert gesendet..");
			lastChange = 0;
		}
	}
}