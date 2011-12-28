package de.hsrm.mi.mobcomp.y2k11grp04;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

/**
 * @author Coralie Reuter <coralie.reuter@hrcom.de>
 * @author Markus Tacker <m@tacker.org>
 */
public class ClientActivity extends ServiceActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		TextView tv = (TextView) findViewById(R.id.tv);
		tv.setText("BLAAAA");

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hsrm.mi.mobcomp.y2k11grp04.ServiceActivity#getServiceMessageRunnable
	 * (android.os.Message)
	 */
	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		// TODO Auto-generated method stub
		return null;
	}
}