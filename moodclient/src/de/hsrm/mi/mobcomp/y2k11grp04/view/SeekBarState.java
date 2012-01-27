package de.hsrm.mi.mobcomp.y2k11grp04.view;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Speichert den Zustand eines Seekbars
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class SeekBarState implements Parcelable {
	private Integer identifier;
	private Integer progress;

	public SeekBarState() {
	}

	public SeekBarState(Parcel in) {
		readFromParcel(in);
	}

	protected void readFromParcel(Parcel in) {
		identifier = in.readInt();
		progress = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(identifier);
		out.writeInt(progress);
	}

	public static final Parcelable.Creator<SeekBarState> CREATOR = new Parcelable.Creator<SeekBarState>() {
		public SeekBarState createFromParcel(Parcel in) {
			return new SeekBarState(in);
		}

		public SeekBarState[] newArray(int size) {
			return new SeekBarState[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public Integer getIdentifier() {
		return identifier;
	}

	public SeekBarState setIdentifier(Integer identifier) {
		this.identifier = identifier;
		return this;
	}

	public Integer getProgress() {
		return progress;
	}

	public SeekBarState setProgress(Integer progress) {
		this.progress = progress;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result
				+ ((progress == null) ? 0 : progress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeekBarState other = (SeekBarState) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (progress == null) {
			if (other.progress != null)
				return false;
		} else if (!progress.equals(other.progress))
			return false;
		return true;
	}
}
