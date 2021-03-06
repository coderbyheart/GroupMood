package de.hsrm.mi.mobcomp.y2k11grp04.activity;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

/**
 * Enthält die Funktionalität, die sich um die Kommunikation mit dem Service
 * kümmert
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
abstract public class ServiceActivity extends BaseActivity {

	private boolean serviceBound = false;
	private Messenger messengerSend;
	private final Messenger messengerReceive = new Messenger(
			new IncomingHandler());
	private final ServiceConnection sConn = new MoodServiceConnection();

	/**
	 * Ruft bei Verbindung {@link ServiceActivity#onConnect()} und bei
	 * Verbindungsabbau {@link ServiceActivity#onDisconnect()} auf.
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private final class MoodServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			messengerSend = new Messenger(service);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onConnect();
				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			messengerSend = null;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onDisconnect();
				}
			});
		}
	}

	/**
	 * Die übergeordneten Klassen definieren ihre Reaktion auf Messages mit
	 * dieser Klasse.
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	public abstract class ServiceMessageRunnable implements Runnable {
		protected Message serviceMessage;

		public ServiceMessageRunnable(Message serviceMessage) {
			this.serviceMessage = serviceMessage;
		}
	}

	/**
	 * Behandelt Nachrichten des Service
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			ServiceMessageRunnable smr = getServiceMessageRunnable(msg);
			if (smr == null) {
				super.handleMessage(msg);
			} else {
				runOnUiThread(smr);
			}
		}
	}

	/**
	 * In dieser Methode definieren übergeordnete Klassen ihre Reaktion auf
	 * Messages mit Hilfe von {@link ServiceMessageRunnable}.
	 * 
	 * @param message
	 */
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case MoodServerService.MSG_ERROR:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Toast.makeText(
							getApplicationContext(),
							serviceMessage.getData().getString(
									MoodServerService.KEY_ERROR_MESSAGE),
							Toast.LENGTH_LONG).show();
					Log.e(getClass().getCanonicalName(),
							"Service error: "
									+ serviceMessage
											.getData()
											.getString(
													MoodServerService.KEY_ERROR_MESSAGE));

				}
			};
		default:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Log.w(getClass().getCanonicalName(), "Unhandled Message: "
							+ serviceMessage.what);
				}
			};
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		disconnect();
	}

	private void connect() {
		if (!serviceBound) {
			Intent intent = new Intent(this, MoodServerService.class);
			// Es ist wichtig getApplicationContext().bindService() und
			// getApplicationContext().unbindService() zu verwenden.
			// Wird die Activiy als Inhalt eines Tabs verwendet kann sonst keine
			// Verbindung zum Service hergestellt werden
			serviceBound = getApplicationContext().bindService(intent, sConn,
					Context.BIND_AUTO_CREATE);
			if (!serviceBound) {
				Log.e(getClass().getCanonicalName(),
						"Konnte nicht zum Service verbinden.");
			}
		}
	}

	private void disconnect() {
		if (serviceBound) {
			getApplicationContext().unbindService(sConn);
			serviceBound = false;
		}
	}

	public boolean isServiceBound() {
		return serviceBound;
	}

	/**
	 * Sendet eine Nachricht
	 * 
	 * @param message
	 */
	protected void sendMessage(Message message) {
		message.replyTo = messengerReceive;
		try {
			messengerSend.send(message);
		} catch (RemoteException e) {
			Log.e(getClass().getCanonicalName(), "Sending message failed.");
		}
	}

	/**
	 * Wird von {@link ServiceActivity.MoodServiceConnection} aufgerufen.
	 */
	protected void onConnect() {
	}

	/**
	 * Wird von {@link ServiceActivity.MoodServiceConnection} aufgerufen.
	 */
	protected void onDisconnect() {
	}

	/**
	 * Lädt ein Meeting anhand einer Uri
	 * 
	 * @param groupMoodUri
	 */
	protected void loadMeeting(Uri groupMoodUri) {
		Message m = Message.obtain(null, MoodServerService.MSG_MEETING);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_MEETING_URI,
				groupMoodUri.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Lädt ein Meeting vollständig mit allen Daten
	 * 
	 * @param meeting
	 */
	protected void loadMeetingComplete(Meeting meeting) {
		Message m = Message
				.obtain(null, MoodServerService.MSG_MEETING_COMPLETE);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_MEETING_URI, meeting.getUri()
				.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Erzeugt eine Antwort zu einer Frage
	 */
	protected void createAnswer(Question q, String answer) {
		Message m = Message.obtain(null, MoodServerService.MSG_ANSWER);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_ANSWER, answer);
		data.putString(MoodServerService.KEY_QUESTION_URI, q.getUri()
				.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Erzeugt eine Antwort zu einer Frage, wobei die Antwort aus mehreren
	 * Werten besteht (Multiple Choice).
	 */
	protected void createAnswer(Question q, String[] answers) {
		Message m = Message.obtain(null, MoodServerService.MSG_ANSWER);
		Bundle data = new Bundle();
		data.putStringArray(MoodServerService.KEY_ANSWER, answers);
		data.putString(MoodServerService.KEY_QUESTION_URI, q.getUri()
				.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Bestellt Updates am Meeting ab
	 */
	protected void unsubscribeMeeting() {
		Log.d(getClass().getCanonicalName(), "Stoppe Meeting-Updates");
		Message m = Message.obtain(null,
				MoodServerService.MSG_MEETING_UNSUBSCRIBE);
		sendMessage(m);
	}

	/**
	 * Abonniert Updates am Meeting
	 */
	protected void subscribeMeeting(Meeting meeting) {
		Log.d(getClass().getCanonicalName(), "Starte Meeting-Updates");
		Message m = Message.obtain(null,
				MoodServerService.MSG_MEETING_SUBSCRIBE);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_MEETING_URI, meeting.getUri()
				.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Legt ein Meeting vom Typ Foto-Vote an
	 * 
	 * @param uri
	 * @param imageFile
	 */
	protected void createFotoVote(Uri uri, String name, File imageFile) {
		Message m = Message.obtain(null, MoodServerService.MSG_FOTOVOTE_CREATE);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_API_URI, uri.toString());
		data.putString(MoodServerService.KEY_MEETING_NAME, name);
		data.putString(MoodServerService.KEY_TOPIC_IMAGE, imageFile.toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Legt ein Topic zu einem Meeting an
	 * 
	 * @param meeting
	 * @param imageFile
	 */
	protected void createFotoVoteTopic(Meeting meeting, File imageFile) {
		Message m = Message.obtain(null,
				MoodServerService.MSG_FOTOVOTE_CREATE_TOPIC);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_MEETING_URI, meeting.getUri()
				.toString());
		data.putString(MoodServerService.KEY_TOPIC_IMAGE, imageFile.toString());
		m.setData(data);
		sendMessage(m);
	}
}