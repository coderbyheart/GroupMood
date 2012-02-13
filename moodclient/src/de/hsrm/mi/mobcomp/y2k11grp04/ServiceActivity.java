package de.hsrm.mi.mobcomp.y2k11grp04;

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

abstract public class ServiceActivity extends MenuActivity {

	private boolean serviceBound = false;
	private Messenger messengerSend;
	private final Messenger messengerReceive = new Messenger(
			new IncomingHandler());
	private final ServiceConnection sConn = new ServiceConnection() {
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
	};

	public abstract class ServiceMessageRunnable implements Runnable {
		protected Message serviceMessage;

		public ServiceMessageRunnable(Message serviceMessage) {
			this.serviceMessage = serviceMessage;
		}
	}

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
				}
			};
		default:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Unhandled Message: " + serviceMessage.what,
							Toast.LENGTH_LONG).show();
				}
			};
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		connect();
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

	@Override
	public void onPause() {
		super.onPause();
		disconnect();
	}

	private void disconnect() {
		if (serviceBound) {
			getApplicationContext().unbindService(sConn);
			serviceBound = false;
		}
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

	protected void onConnect() {
	}

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
	 * @param groupMoodUri
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

	public boolean isServiceBound() {
		return serviceBound;
	}
}