package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Datenbank-Adapter zum Merken der letzten Meetings.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class DbAdapter {
	private static final String DB_FILENAME = "db.db";
	private static final int DB_VERSION = 1;
	private SQLiteDatabase db;
	private DBHelper dbHelper;

	// Tabelle für Blätter
	private static final String HISTORY_TABLE = "meetinghistory";
	public static final int HISTORY_COL_ID = 0;
	public static final int HISTORY_COL_MEETING_URL = 1;
	public static final int HISTORY_COL_CREATION_TIME = 2;
	public static final String HISTORY_KEY_ID = "_id";
	public static final String HISTORY_KEY_MEETING_URL = "meetingUrl";
	public static final String HISTORY_KEY_CREATION_TIME = "creationTime";
	private static final String HISTORY_SQL_CREATE = "CREATE TABLE "
			+ HISTORY_TABLE + "(" + HISTORY_KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
			+ HISTORY_KEY_MEETING_URL + " TEXT NOT NULL" + ", "
			+ HISTORY_KEY_CREATION_TIME + " INTEGER NOT NULL" + ")";

	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(HISTORY_SQL_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
			onCreate(db);
		}

	}

	public DbAdapter(Context context) {
		dbHelper = new DBHelper(context, DB_FILENAME, null, DB_VERSION);
	}

	public DbAdapter open() throws SQLException {
		if (db == null) {
			try {
				db = dbHelper.getWritableDatabase();
			} catch (SQLException e) {
				db = dbHelper.getReadableDatabase();
			}
		}
		return this;
	}

	public DbAdapter close() {
		if (db != null) {
			db.close();
			db = null;
		}
		return this;
	}

	protected SQLiteDatabase getDb() {
		open();
		return db;
	}

	/**
	 * Liefert eine Liste mit Einträgen
	 */
	public Cursor getMeetingHistorysCursor() {
		return getMeetingHistorysCursor(new String[] { HISTORY_KEY_ID,
				HISTORY_KEY_MEETING_URL, HISTORY_KEY_CREATION_TIME });
	}

	/**
	 * Liefert eine Liste mit allen Einträgen, aber nur bestimmten Spalten
	 * 
	 * @param cols
	 */
	public Cursor getMeetingHistorysCursor(String[] cols) {
		Cursor result = getDb().query(HISTORY_TABLE, cols, null, null, null,
				null, HISTORY_KEY_CREATION_TIME + " DESC");
		result.moveToFirst();
		if (result.isAfterLast()) {
			return null;
		}
		return result;
	}

	/**
	 * Speichert ein Blatt
	 * 
	 * @param item
	 */
	public MeetingHistory persist(MeetingHistory item) {
		ContentValues itemValues = new ContentValues();
		itemValues
				.put(HISTORY_KEY_MEETING_URL, item.getMeetingUri().toString());
		if (item.getId() > 0) {
			itemValues.put(HISTORY_KEY_ID, item.getId());
			itemValues.put(HISTORY_KEY_CREATION_TIME, item.getCreationDate()
					.getTime());
		} else {
			itemValues.put(HISTORY_KEY_CREATION_TIME, new Date().getTime());
		}
		return (MeetingHistory) persist(item, itemValues, HISTORY_TABLE,
				HISTORY_KEY_ID);
	}

	/**
	 * Allgemeine Methode zum speichern von Entities
	 * 
	 * @param item
	 * @param itemValues
	 * @param table
	 * @param idKey
	 * @return
	 */
	private Entity persist(Entity item, ContentValues itemValues, String table,
			String idKey) {
		if (item.getId() > 0) {
			db.update(table, itemValues, idKey + "=" + item.getId(), null);
		} else {
			int id = (int) db.insert(table, null, itemValues);
			item.setId(id);
		}
		return item;
	}
}
