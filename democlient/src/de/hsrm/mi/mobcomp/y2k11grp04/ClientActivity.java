package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.functions.MasterRatingActivity;
import de.hsrm.mi.mobcomp.y2k11grp04.functions.TopicRatingActivity;

/**
 * @author Coralie Reuter <coralie.reuter@hrcom.de>
 * @author Markus Tacker <m@tacker.org>
 */
public class ClientActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView txt = (TextView) findViewById(R.id.tv_select);
		txt.setText(R.string.client_intro);

		ListView clientTasks = (ListView) findViewById(R.id.taskListView);
		clientTasks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources()
				.getStringArray(R.array.client_tasks)));
		clientTasks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					startActivity(new Intent(getApplicationContext(), TopicRatingActivity.class));
					break;
				case 1:
					startActivity(new Intent(getApplicationContext(), MasterRatingActivity.class));
					break;
				}
			}
		});
	}
}