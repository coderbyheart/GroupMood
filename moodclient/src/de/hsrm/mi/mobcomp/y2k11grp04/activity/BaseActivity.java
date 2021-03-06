package de.hsrm.mi.mobcomp.y2k11grp04.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.gui.FotoVoteDialog;

/**
 * Enthält gemeinsam verwendete Funktionen der {@link LaunchActivity} und der
 * {@link QuestionActivity}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class BaseActivity extends Activity {

	protected static final int ACITIVITY_RESULT_SELECT_PICTURE = 1;
	protected static final int ACITIVITY_RESULT_CAPTURE_PICTURE = 2;

	// Zeigt den Dialog zum anlegen eines Topics an
	protected FotoVoteDialog fotovoteDialog;

	// Uri zum ausgewählten oder gemachten Bild
	protected Uri captureImageTargetUri;

	// Datei des ausgewählten oder gemachten Bild
	protected File imageFile;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			if (requestCode == ACITIVITY_RESULT_SELECT_PICTURE) {
				Uri fileUri = intent.getData();
				imageFile = getFileFromMediaUri(fileUri);
				fotovoteDialog.photo.setImageURI(fileUri);
				fotovoteDialog.imageFile = imageFile;
				fotovoteDialog.validate();
			} else if (requestCode == ACITIVITY_RESULT_CAPTURE_PICTURE) {
				ContentResolver cr = getContentResolver();
				Bitmap bitmap;
				try {
					bitmap = android.provider.MediaStore.Images.Media
							.getBitmap(cr, captureImageTargetUri);
					fotovoteDialog.photo.setImageBitmap(bitmap);
					fotovoteDialog.imageFile = imageFile;
					fotovoteDialog.validate();
				} catch (Exception e) {
					Log.e(getClass().getCanonicalName(), e.toString());
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageFile != null) {
			outState.putString("imageFile", imageFile.getAbsolutePath());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey("imageFile")) {
			imageFile = new File(savedInstanceState.getString("imageFile"));
		}
	}

	/**
	 * Kümmert sich um das Verwenden der Kamera, legt das geschossene Bild auf
	 * der SD-Karte ab.
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	protected class PhotoCaptureListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			File cacheDir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/de.hsrm.mi.mobcomp.y2k11grp04/cache/");
			if (!cacheDir.exists()) {
				if (!cacheDir.mkdirs()) {
					Log.e(getClass().getCanonicalName(),
							"Failed to create directory: "
									+ cacheDir.toString());
					Toast.makeText(
							getApplicationContext(),
							getApplicationContext().getResources().getString(
									R.string.capture_storage_error),
							Toast.LENGTH_LONG).show();
					return;
				}
				File noMediaFile = new File(cacheDir.getAbsolutePath()
						+ "/.nomedia");
				try {
					noMediaFile.createNewFile();
				} catch (IOException e) {
					Log.e(getClass().getCanonicalName(),
							"Failed to create file "
									+ noMediaFile.getAbsolutePath());
				}
			}
			imageFile = new File(
					cacheDir.getAbsolutePath()
							+ "/capture-"
							+ new SimpleDateFormat("yyyyMMdd_HHmmss")
									.format(new Date()) + ".jpg");

			captureImageTargetUri = Uri.fromFile(imageFile);

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageTargetUri);

			startActivityForResult(Intent.createChooser(
					intent,
					getApplicationContext().getResources().getString(
							R.string.fotovote_photo_capture)),
					ACITIVITY_RESULT_CAPTURE_PICTURE);
		}
	}

	/**
	 * Wählt eine Fotos aus der Galerie aus.
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	protected final class GallerySelectListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(
							intent,
							getResources().getString(
									R.string.fotovote_photo_select)),
					ACITIVITY_RESULT_SELECT_PICTURE);
		}
	}

	/**
	 * Erzeugt einen Dateipfad aus einer Uri
	 * 
	 * @param fileUri
	 */
	protected File getFileFromMediaUri(Uri fileUri) {

		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(fileUri, proj, null, null, null);
		cursor.moveToFirst();
		File f = new File(cursor.getString(cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
		cursor.close();
		return f;
	}
}
