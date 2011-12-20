package de.hsrm.mi.mobcomp.y2k11grp04;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Einstellungen für die App anzeigen
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
