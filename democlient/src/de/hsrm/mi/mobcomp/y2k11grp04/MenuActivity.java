package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Coralie Reuter <coralie.reuter@hrcom.de>
 * @author Markus Tacker <m@tacker.org>
 * 
 */
public abstract class MenuActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
			return true;
		case R.id.menu_overall_rating:
			startActivity(new Intent(getApplicationContext(), MasterRatingActivity.class));
			return true;
		}
		return false;
	}

}
