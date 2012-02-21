package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Content-Provider für die {@link MeetingHistory Meeting-History-Datenbank}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class Provider extends ContentProvider {

	private static final String AUTHORITY = "de.hsrm.mi.mobcomp.y2k11grp04.provider.content";
	private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
	private static final int HISTORY_LIST = 1;
	private static final UriMatcher uriMatcher;
	private DbAdapter dbc;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		/**
		 * Liste mit allen Einträgen
		 */
		uriMatcher.addURI(AUTHORITY, "meetinghistory", HISTORY_LIST);
	}
	private static final String VND_HISTORY_DIR = "vnd.android.cursor.dir/vnc.de.hsrm.mi.mobcomp.y2k11grp04.provider.content.history";

	/**
	 * Wird beim Erzeugen aufgerufen
	 */
	@Override
	public boolean onCreate() {
		dbc = new DbAdapter(getContext());
		dbc.open();
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case HISTORY_LIST:
			return VND_HISTORY_DIR;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	/**
	 * Legt einen neuen Eintrag an
	 */
	@Override
	public Uri insert(Uri uri, ContentValues cv) {
		switch (uriMatcher.match(uri)) {
		case HISTORY_LIST:
			MeetingHistory meetingHistory = new MeetingHistory();
			meetingHistory.setMeetingUri(Uri.parse(cv
					.getAsString(DbAdapter.HISTORY_KEY_MEETING_URL)));
			dbc.persist(meetingHistory);
			return CONTENT_URI.buildUpon().appendPath("sheet")
					.appendPath("" + meetingHistory.getId()).build();
		default:
			return null;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (uriMatcher.match(uri)) {
		case HISTORY_LIST:
			return dbc.getMeetingHistorysCursor();
		default:
			return null;
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}
}
