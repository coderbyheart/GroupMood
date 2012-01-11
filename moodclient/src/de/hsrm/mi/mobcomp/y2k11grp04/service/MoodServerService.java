package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.model.BaseModel;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;

public class MoodServerService extends Service {

	public static final int MSG_PAUSE = 1;
	public static final int MSG_PAUSE_RESULT = 2;
	public static final int MSG_RESUME = 3;
	public static final int MSG_RESUME_RESULT = 4;
	public static final int MSG_MEETING = 5;
	public static final int MSG_MEETING_RESULT = 6;
	public static final int MSG_MEETING_SUBSCRIBE = 7;
	public static final int MSG_MEETING_UNSUBSCRIBE = 8;

	public static final String KEY_MEETING_MODEL = "model.Meeting";
	public static final String KEY_MEETING_ID = "meeting.id";
	public static final String KEY_MEETING_URI = "meeting.uri";

	private final Messenger messenger = new Messenger(new IncomingHandler());
	private Timer timer;
	private Map<Messenger, Meeting> meetingSubscription = new HashMap<Messenger, Meeting>();

	/**
	 * Wir oft der Vote abgeschickt wird (ms)
	 */
	private int updateRate = 500;
	private MoodServerApi api;

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message request) {
			switch (request.what) {
			case MSG_MEETING:
				BaseModel meeting = getMeeting(Uri.parse(request.getData()
						.getString(KEY_MEETING_URI)));
				if (meeting != null)
					sendMeetingTo(meeting, request.replyTo);
				break;
			case MSG_MEETING_SUBSCRIBE:
				Meeting subscribeMeeting = getMeeting(Uri.parse(request
						.getData().getString(KEY_MEETING_URI)));
				if (subscribeMeeting != null) {
					meetingSubscription.put(request.replyTo, subscribeMeeting);
				}
				break;
			case MSG_MEETING_UNSUBSCRIBE:
				meetingSubscription.remove(request.replyTo);
				break;
			case MSG_PAUSE:
				sendMsg(request.replyTo,
						Message.obtain(null, MSG_PAUSE_RESULT, doPause(), 0));
				break;
			case MSG_RESUME:
				sendMsg(request.replyTo,
						Message.obtain(null, MSG_RESUME_RESULT, doResume(), 0));
				break;
			default:
				super.handleMessage(request);
			}
		}
	}

	/**
	 * Lädt das Meeting mit der ID meeting_id
	 * 
	 * @param uri
	 */
	private Meeting getMeeting(Uri uri) {
		try {
			return api.getMeeting(uri);
		} catch (ApiException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return null;
	}

	/**
	 * Sendet das meeting and den Empfänger
	 * 
	 * @param meeting
	 * @param rcpt
	 */
	private void sendMeetingTo(BaseModel meeting, Messenger rcpt) {
		Message info = Message.obtain(null, MSG_MEETING_RESULT);
		Bundle data = new Bundle();
		data.putParcelable(KEY_MEETING_MODEL, meeting);
		info.setData(data);
		sendMsg(rcpt, info);
	}

	@Override
	public void onCreate() {
		api = new MoodServerApi();
		startTimer();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	public void onDestroy() {
		stopTimer();
	}

	/**
	 * Startet den Timer
	 */
	private boolean startTimer() {
		if (timer != null)
			return false;
		Log.v(getClass().getCanonicalName(), "Starte Timer.");
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {

				// Alle registrierten Meeting-Watcher benachrichtigen
				for (Messenger messenger : meetingSubscription.keySet()) {
					BaseModel updatedMeeting = getMeeting(meetingSubscription
							.get(messenger).getUri());
					if (updatedMeeting != null)
						sendMeetingTo(updatedMeeting, messenger);
				}
			}
		}, updateRate, updateRate);
		return true;
	}

	/**
	 * Hält den Straf-Timer an
	 */
	private boolean stopTimer() {
		if (timer == null)
			return false;
		timer.cancel();
		timer = null;
		return true;
	}

	/**
	 * Methode zum Pausieren des Timers von Außen
	 */
	private int doResume() {
		return startTimer() ? 1 : 0;
	}

	/**
	 * Methode zum Pausieren des Timers von Außen
	 */
	private int doPause() {
		return stopTimer() ? 1 : 0;
	}

	protected void sendMsg(Messenger rcpt, Message response) {
		if (rcpt == null)
			return;
		try {
			rcpt.send(response);
		} catch (RemoteException e) {
			Log.v(getClass().getCanonicalName(), "Failed to send message.");
		}
	}

}
