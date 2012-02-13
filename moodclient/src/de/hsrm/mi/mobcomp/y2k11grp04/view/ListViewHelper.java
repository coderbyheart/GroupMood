package de.hsrm.mi.mobcomp.y2k11grp04.view;

import java.util.ArrayList;

import android.widget.ListView;

/**
 * Hilfsfunktionen für ListViews.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class ListViewHelper {

	/**
	 * Gibt die Liste mit ausgewählten Einträgen zurück
	 * 
	 * @param lv
	 */
	public ArrayList<String> getSelectedItems(ListView lv) {
		ArrayList<String> selectedValues = new ArrayList<String>();
		for (int i = 0; i < lv.getCount(); i++) {
			if (lv.isItemChecked(i)) {
				selectedValues.add(lv.getItemAtPosition(i).toString());
			}
		}
		return selectedValues;
	}
	
}
