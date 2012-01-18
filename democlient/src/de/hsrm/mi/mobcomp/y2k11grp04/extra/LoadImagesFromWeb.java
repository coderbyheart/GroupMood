package de.hsrm.mi.mobcomp.y2k11grp04.extra;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;

public class LoadImagesFromWeb implements Runnable {

	private ArrayList<URL> urls;
	private int meetingId;

	public LoadImagesFromWeb(ArrayList<URL> urls, int meetingId) {
		this.urls = urls;
	}

	/**
	 * Lädt die Infos zu den verschiedenen Größen eines flickr-Photos herunter
	 * und speichert das Quadratische Thumbnail. Anschließend wird das
	 * onComplete-Runnable ausgeführt
	 */
	@Override
	public void run() {
		for (URL u : urls)
			try {
				fetchImage(u, meetingId);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * @todo Not covered here: Check if external storage is available, see
	 *       http://developer.android.com/guide/topics/data/data-storage.html#
	 *       filesExternal
	 * @param source
	 * @throws IOException
	 */
	public File fetchImage(URL source, int id) throws IOException {
		// Note: In API Level < 11 muss man sich das Verzeichnis noch händisch
		// zusammensetzen
		File cacheDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/de.hsrm.mi.mobcomp.y2k11grp04.groupmood/cache/meeting/");
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs())
				throw new IOException("Failed to create: "
						+ cacheDir.getAbsolutePath());
		}
		// Pfad zur Cache-Datei
		File imageFile = new File(cacheDir.getAbsolutePath() + "/topic" + id
				+ ".jpg");
		// Note: Hier könnte man prüfen, ob es die Cache-Datei schon gibt, zum
		// Demonstrationszwecken wird dies aber explizit nicht gemacht
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(source.toString());
		HttpResponse response = client.execute(request);
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != 200) {
			throw new IOException("Invalid response from server: "
					+ status.toString());
		}
		HttpEntity entity = response.getEntity();
		InputStream inputStream = entity.getContent();
		// Datei zum Schreiben öffnen
		BufferedOutputStream buf = new BufferedOutputStream(
				new FileOutputStream(imageFile));

		int readBytes = 0;
		byte[] sBuffer = new byte[512];
		while ((readBytes = inputStream.read(sBuffer)) != -1) {
			buf.write(sBuffer, 0, readBytes);
		}
		// Datei schließen nicht vergessen
		buf.close();
		return imageFile;

	}
}
