package de.hsrm.mi.mobcomp.y2k11grp04;

import de.hsrm.mi.mobcomp.y2k11grp04.service.DemoServerService;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends MenuActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView tasks = (ListView) findViewById(R.id.taskListView);
		tasks.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.tasks)));
		tasks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					startActivity(new Intent(getApplicationContext(),
							ClientActivity.class));
					break;
				case 1:
					startActivity(new Intent(getApplicationContext(),
							MasterActivity.class));
					break;
				}
			}
		});
	}

	public void onResume() {
		super.onResume();
		String apiUrl = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(DemoServerService.PREFS_KEY_API_URL, null);

		// Keine Einstellung vorhanden? Dann Konfigurieren.
		if (apiUrl == null || apiUrl.length() <= 0
				|| !URLUtil.isValidUrl(apiUrl)) {
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			Toast.makeText(getApplicationContext(), R.string.please_configure,
					Toast.LENGTH_LONG).show();
			return;
		}
	}
}