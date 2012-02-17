package de.hsrm.mi.mobcomp.y2k11grp04;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Coralie Reuter <coralie.reuter@hrcom.de>
 * @author Markus Tacker <m@tacker.org>
 * 
 */
public abstract class MenuActivity extends BaseActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

}
