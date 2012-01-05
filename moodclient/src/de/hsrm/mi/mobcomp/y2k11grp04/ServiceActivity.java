package de.hsrm.mi.mobcomp.y2k11grp04;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

abstract public class ServiceActivity extends MenuActivity {

	private boolean serviceBound = false;
	private Messenger messengerSend;
	private final Messenger messengerReceive = new Messenger(
			new IncomingHandler());
	private final ServiceConnection sConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(getClass().getCanonicalName(), "... verbunden.");
			messengerSend = new Messenger(service);
			onConnect();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			messengerSend = null;
			onDisconnect();
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
				smr.run();
			}
		}
	}

	abstract protected ServiceMessageRunnable getServiceMessageRunnable(
			Message message);

	@Override
	public void onResume() {
		super.onResume();
		connect();
	}

	private void connect() {
		if (!serviceBound) {
			Log.v(getClass().getCanonicalName(), "Verbinde mit Service ...");
			Intent intent = new Intent(this, MoodServerService.class);
			serviceBound = bindService(intent, sConn, Context.BIND_AUTO_CREATE);
			if (serviceBound) {
				Log.v(getClass().getCanonicalName(), "Verbunden");
			} else {
				Log.e(getClass().getCanonicalName(), "Nicht Verbunden");
			}
		}
	}

	@Override
	public void onPause() {
		Log.v(getClass().getCanonicalName(), "onPause()");
		super.onPause();
		disconnect();
	}

	private void disconnect() {
		if (serviceBound) {
			Log.v(getClass().getCanonicalName(), "Trenne vom Service ...");
			unbindService(sConn);
			serviceBound = false;
			Log.v(getClass().getCanonicalName(), "Getrennt.");
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
			Log.v(getClass().getCanonicalName(), "Sending message failed.");
		}
	}

	protected void onConnect() {
	}

	protected void onDisconnect() {
	}
}