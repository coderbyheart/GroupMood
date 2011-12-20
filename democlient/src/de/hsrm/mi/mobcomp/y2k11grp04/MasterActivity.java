package de.hsrm.mi.mobcomp.y2k11grp04;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.extra.ColoringTextWatcher;
import de.hsrm.mi.mobcomp.y2k11grp04.service.DemoServerService;

public class MasterActivity extends ServiceActivity {

	private TextView percentTextView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.master);
		percentTextView = (TextView) findViewById(R.id.percentTextView);
		new ColoringTextWatcher(percentTextView);
		percentTextView.setText("50 %");
	}

	@Override
	public void onConnect() {
		super.onConnect();
		Message m = Message.obtain(null,
				DemoServerService.MSG_MEETING_SUBSCRIBE);
		Bundle data = new Bundle();
		// TODO: Hart kodierte Meeting-ID
		data.putInt(DemoServerService.KEY_MEETING_ID, 1);
		m.setData(data);
		sendMessage(m);
	}

	@Override
	public void onPause() {
		super.onPause();
		Message m = Message.obtain(null,
				DemoServerService.MSG_MEETING_UNSUBSCRIBE);
		Bundle data = new Bundle();
		// TODO: Hart kodierte Meeting-ID
		data.putInt(DemoServerService.KEY_MEETING_ID, 1);
		m.setData(data);
		sendMessage(m);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case DemoServerService.MSG_MEETING_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					percentTextView.setText(""
							+ serviceMessage.getData().getInt(
									DemoServerService.KEY_MEETING_AVG_VOTE)
							+ "%");
				}
			};
		default:
			return null;
		}
	}
}