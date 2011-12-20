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
import android.preference.PreferenceManager;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Vote;

public class DemoServerService extends Service {

	public static final int MSG_VOTE = 1;
	public static final int MSG_VOTE_RESULT = 2;
	public static final int MSG_PAUSE = 3;
	public static final int MSG_PAUSE_RESULT = 4;
	public static final int MSG_RESUME = 5;
	public static final int MSG_RESUME_RESULT = 6;
	public static final int MSG_MEETING = 7;
	public static final int MSG_MEETING_RESULT = 8;
	public static final int MSG_MEETING_SUBSCRIBE = 9;
	public static final int MSG_MEETING_UNSUBSCRIBE = 10;

	public static final String KEY_MEETING_ID = "meeting.id";
	public static final String KEY_MEETING_AVG_VOTE = "meeting.avgVote";
	public static final String KEY_MEETING_NUM_VOTES = "meeting.numVotes";
	public static final String KEY_VOTE_VOTE = "vote.vote";

	public static final String PREFS_KEY_API_URL = "api_url";

	private final Messenger messenger = new Messenger(new IncomingHandler());
	private Timer timer;
	private Map<Messenger, Vote> voteMap = new HashMap<Messenger, Vote>();
	private Map<Messenger, Meeting> meetingSubscription = new HashMap<Messenger, Meeting>();

	/**
	 * Wir oft der Vote abgeschickt wird (ms)
	 */
	private int updateRate = 500;
	private DemoServerApi api;

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message request) {
			switch (request.what) {
			case MSG_VOTE:
				// TODO: verify meeting
				Vote vote = new Vote(new Meeting(request.getData().getInt(
						KEY_MEETING_ID), "Demo-Meeting"), request.getData()
						.getInt(KEY_VOTE_VOTE));
				voteMap.put(request.replyTo, vote);
				break;
			case MSG_MEETING:
				Meeting meeting = getMeeting(request.getData().getInt(
						DemoServerService.KEY_MEETING_ID));
				if (meeting != null)
					sendMeetingTo(meeting, request.replyTo);
				break;
			case MSG_MEETING_SUBSCRIBE:
				Meeting subscribeMeeting = getMeeting(request.getData().getInt(
						DemoServerService.KEY_MEETING_ID));
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
	 * @param meeting_id
	 */
	private Meeting getMeeting(int meeting_id) {
		try {
			return api.getMeeting(meeting_id);
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
	private void sendMeetingTo(Meeting meeting, Messenger rcpt) {
		Message info = Message.obtain(null, MSG_MEETING_RESULT);
		Bundle data = new Bundle();
		data.putInt(DemoServerService.KEY_MEETING_ID, meeting.getId());
		data.putInt(DemoServerService.KEY_MEETING_AVG_VOTE,
				meeting.getAvgVote());
		data.putInt(DemoServerService.KEY_MEETING_NUM_VOTES,
				meeting.getNumVotes());
		info.setData(data);
		sendMsg(rcpt, info);
	}

	@Override
	public void onCreate() {
		String apiUrl = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(PREFS_KEY_API_URL, null);
		api = new DemoServerApi(Uri.parse(apiUrl));
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

				// Alle eingegangenen Votes verschicken
				HashMap<Messenger, Vote> voteMapCopy = new HashMap<Messenger, Vote>(
						voteMap);
				voteMap.clear();

				for (Messenger rcpt : voteMapCopy.keySet()) {
					Vote vote = voteMapCopy.get(rcpt);
					try {
						Meeting meeting = api.addVote(vote.getMeeting(), vote);
						Message info = Message.obtain(null, MSG_VOTE_RESULT);
						Bundle data = new Bundle();
						data.putInt(DemoServerService.KEY_MEETING_ID,
								meeting.getId());
						data.putInt(DemoServerService.KEY_MEETING_AVG_VOTE,
								meeting.getAvgVote());
						data.putInt(DemoServerService.KEY_MEETING_NUM_VOTES,
								meeting.getNumVotes());
						info.setData(data);
						sendMsg(rcpt, info);
					} catch (ApiException e) {
						Log.e(getClass().getCanonicalName(), e.getMessage());
					}
				}

				// Alle registrierten Meeting-Watcher benachrichtigen
				for (Messenger messenger : meetingSubscription.keySet()) {
					Meeting updatedMeeting = getMeeting(meetingSubscription
							.get(messenger).getId());
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
