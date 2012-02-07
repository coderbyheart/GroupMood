package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabbed);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, QuestionActivity.class);
		intent.putExtras(getIntent().getExtras());

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("question")
				.setIndicator(res.getString(R.string.tab_questions),
						res.getDrawable(R.drawable.ic_tab_checkmark))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, CommentActivity.class);
		intent.putExtras(getIntent().getExtras());
		spec = tabHost
				.newTabSpec("comment")
				.setIndicator(res.getString(R.string.tab_comments),
						res.getDrawable(R.drawable.ic_tab_bubble))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ResultActivity.class);
		intent.putExtras(getIntent().getExtras());
		spec = tabHost
				.newTabSpec("result")
				.setIndicator(res.getString(R.string.tab_result),
						res.getDrawable(R.drawable.ic_tab_chart))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
}