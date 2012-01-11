package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.widget.SeekBar;

/**
 * @author Coralie Reuter
 * 
 */
public class ImageModel implements Model {
	private static final int NUMBER_OF_SEEKBARS = 5;
	private String name;
	private URL url;
	private URI uri;
	private ArrayList<SeekBar> seekBars;

	/**
	 * @return the numberOfSeekbars
	 */
	public int getNumberOfSeekbars() {
		return NUMBER_OF_SEEKBARS;
	}

	/**
	 * @return the seekBars
	 */
	public ArrayList<SeekBar> getSeekBars() {
		return seekBars;
	}

	/**
	 * @param seekbar
	 * @param context
	 * @return
	 */
	public ImageModel setSeekBars(SeekBar seekbar, Context context) {
		this.seekBars = new ArrayList<SeekBar>();

		for (int i = 0; i < NUMBER_OF_SEEKBARS; i++) {
			this.seekBars.add(seekbar);
		}
		return this;
	}

	@Override
	public String getContext() {
		return "image";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((seekBars == null) ? 0 : seekBars.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageModel other = (ImageModel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (seekBars == null) {
			if (other.seekBars != null)
				return false;
		} else if (!seekBars.equals(other.seekBars))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	/**
	 * @return name of the image
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @return
	 */
	public ImageModel setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return url of the image
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url
	 * @return
	 */
	public ImageModel setUrl(URL url) {
		this.url = url;
		return this;
	}

	/**
	 * @return uri of the image
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @param uri
	 * @return
	 */
	public ImageModel setUri(URI uri) {
		this.uri = uri;
		return this;
	}
}
