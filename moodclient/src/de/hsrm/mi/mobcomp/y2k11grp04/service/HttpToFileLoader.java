package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.util.Log;

/**
 * Lädt eine Datei via HTTP herunter und speichert sie in einer lokalen Datei
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class HttpToFileLoader implements Callable<File> {

	private Uri remoteFile;
	private File localFile;

	public HttpToFileLoader(Uri uri, File imageFile) {
		this.remoteFile = uri;
		this.localFile = imageFile;
	}

	/**
	 * Lädt das Bild heruntern. Anschließend wird das onComplete-Runnable
	 * ausgeführt
	 */
	@Override
	public File call() throws Exception {
		fetchImage(remoteFile);
		return localFile;
	}

	/**
	 * @todo Wir gehen davon aus, dass external Storage verfügbar ist
	 * @param source
	 * @throws IOException
	 */
	public void fetchImage(Uri source) throws IOException {
		Log.d(getClass().getCanonicalName(),
				"Lade URL " + remoteFile.toString() + " nach "
						+ localFile.toString());
		// Note: In API Level < 11 muss man sich das Verzeichnis noch händisch
		// zusammensetzen
		File cacheDir = localFile.getParentFile();
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs())
				throw new IOException("Failed to create: "
						+ cacheDir.getAbsolutePath());
			File noMediaFile = new File(cacheDir.getAbsolutePath() + "/.nomedia");
			noMediaFile.createNewFile();
		}
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
				new FileOutputStream(localFile));

		int readBytes = 0;
		byte[] sBuffer = new byte[512];
		while ((readBytes = inputStream.read(sBuffer)) != -1) {
			buf.write(sBuffer, 0, readBytes);
		}
		// Datei schließen nicht vergessen
		buf.close();
	}
}
