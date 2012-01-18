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
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class MoodServerService extends Service {

	public static final int MSG_PAUSE = 1;
	public static final int MSG_PAUSE_RESULT = 2;
	public static final int MSG_RESUME = 3;
	public static final int MSG_RESUME_RESULT = 4;
	public static final int MSG_MEETING = 5;
	public static final int MSG_MEETING_RESULT = 6;
	public static final int MSG_MEETING_SUBSCRIBE = 7;
	public static final int MSG_MEETING_UNSUBSCRIBE = 8;
	public static final int MSG_MEETING_COMPLETE = 9;
	public static final int MSG_MEETING_COMPLETE_RESULT = 10;
	public static final int MSG_MEETING_COMPLETE_PROGRESS = 11;
	public static final int MSG_ERROR = 99;

	public static final String KEY_MEETING_MODEL = "model.Meeting";
	public static final String KEY_MEETING_ID = "meeting.id";
	public static final String KEY_MEETING_URI = "meeting.uri";
	public static final String KEY_ERROR_MESSAGE = "error.message";

	public static final String KEY_TOPIC_MODEL = "model.Topic";
	public static final String KEY_QUESTION_MODEL = "model.Question";

	private final Messenger messenger = new Messenger(new IncomingHandler());
	private Timer timer;
	private Map<Messenger, Meeting> meetingSubscription = new HashMap<Messenger, Meeting>();

	/**
	 * Wie oft der Vote abgeschickt wird (ms)
	 */
	private int updateRate = 500;
	private MoodServerApi api;

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message request) {
			try {
				switch (request.what) {
				case MSG_MEETING:
					fetchMeeting(request);
					break;
				case MSG_MEETING_COMPLETE:
					fetchMeetingComplete(request);
					break;
				case MSG_MEETING_SUBSCRIBE:
					subscribeMeeting(request);
					break;
				case MSG_MEETING_UNSUBSCRIBE:
					unsubscribeMeeting(request);
					break;
				case MSG_PAUSE:
					doPause(request);
					break;
				case MSG_RESUME:
					doResume(request);
					break;
				default:
					super.handleMessage(request);
				}
			} catch (ApiException e) {
				sendError(request.replyTo, e.getMessage());
			}
		}
	}

	/**
	 * Lädt ein Meeting
	 * 
	 * @param request
	 * @throws ApiException
	 */
	public void fetchMeeting(Message request) throws ApiException {
		Meeting meeting = api.getMeeting(Uri.parse(request.getData().getString(
				KEY_MEETING_URI)));
		sendMeetingTo(meeting, request.replyTo, MSG_MEETING_RESULT);
	}

	/**
	 * @param request
	 * @throws ApiException
	 * @todo TODO: Hier muss noch das Laden des Meetings in Stücken
	 *       implementiert werden.
	 */
	public void fetchMeetingComplete(Message request) throws ApiException {
		sendMeetingProgress(request.replyTo, 1, 3);
		Meeting meeting = api.getMeetingRecursive(Uri.parse(request.getData()
				.getString(KEY_MEETING_URI)));
		sendMeetingProgress(request.replyTo, 2, 3);
		sendMeetingTo(meeting, request.replyTo, MSG_MEETING_COMPLETE_RESULT);
		sendMeetingProgress(request.replyTo, 3, 3);
	}

	/**
	 * Sendet Infos mit dem Fortschritt des Ladens eines Meetings an die
	 * Activity, die es angefordert hat
	 * 
	 * @param replyTo
	 * @param progress
	 * @param max
	 */
	private void sendMeetingProgress(Messenger replyTo, int progress, int max) {
		Message info = Message.obtain(null, MSG_MEETING_COMPLETE_PROGRESS);
		info.arg1 = progress;
		info.arg2 = max;
		sendMsg(replyTo, info);
	}

	/**
	 * Meldet sich von Änderungsbenachrichtigungen ab
	 * 
	 * @param request
	 */
	public void unsubscribeMeeting(Message request) {
		meetingSubscription.remove(request.replyTo);
	}

	/**
	 * Abonniert Änderungen am Meeting
	 * 
	 * @param request
	 * @throws ApiException
	 */
	public void subscribeMeeting(Message request) throws ApiException {
		Meeting subscribeMeeting = api.getMeeting(Uri.parse(request.getData()
				.getString(KEY_MEETING_URI)));
		if (subscribeMeeting != null) {
			meetingSubscription.put(request.replyTo, subscribeMeeting);
		}
	}

	/**
	 * Sendet das meeting and den Empfänger
	 * 
	 * @param meeting
	 * @param rcpt
	 */
	private void sendMeetingTo(BaseModel meeting, Messenger rcpt, int type) {
		Message info = Message.obtain(null, type);
		Bundle data = new Bundle();
		data.putParcelable(KEY_MEETING_MODEL, meeting);
		info.setData(data);
		sendMsg(rcpt, info);
	}

	@Override
	public void onCreate() {
		api = new MoodServerApi();
		api.registerModel(Meeting.class,
				Uri.parse("http://groupmood.net/jsonld/meeting"));
		api.registerModel(Topic.class,
				Uri.parse("http://groupmood.net/jsonld/topic"));
		api.registerModel(Question.class,
				Uri.parse("http://groupmood.net/jsonld/question"));
		api.registerModel(QuestionOption.class,
				Uri.parse("http://groupmood.net/jsonld/questionoption"));
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
					BaseModel updatedMeeting;
					try {
						updatedMeeting = api.getMeeting(meetingSubscription
								.get(messenger).getUri());
						if (updatedMeeting != null)
							sendMeetingTo(updatedMeeting, messenger,
									MSG_MEETING_RESULT);
					} catch (ApiException e) {
						sendError(messenger, e.getMessage());
					}
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
	 * 
	 * @param request
	 */
	private void doResume(Message request) {
		boolean timerResult = startTimer();
		sendMsg(request.replyTo,
				Message.obtain(null, MSG_RESUME_RESULT, timerResult ? 1 : 0, 0));
	}

	/**
	 * Methode zum Pausieren des Timers von Außen
	 * 
	 * @param request
	 */
	private void doPause(Message request) {
		boolean stopResult = stopTimer();
		sendMsg(request.replyTo,
				Message.obtain(null, MSG_PAUSE_RESULT, stopResult ? 1 : 0, 0));
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

	private void sendError(Messenger rcpt, String message) {
		Message errorMsg = Message.obtain(null, MSG_ERROR);
		Bundle data = new Bundle();
		data.putString(KEY_ERROR_MESSAGE, message);
		errorMsg.setData(data);
		sendMsg(rcpt, errorMsg);
	}
}
