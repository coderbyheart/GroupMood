package de.hsrm.mi.mobcomp.y2k11grp04.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Meeting extends BaseModel implements StateModel {
	private String name;
	private Uri uri;
	private int numTopics;

	public Meeting() {
	}

	public Meeting(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		name = in.readString();
		uri = Uri.parse(in.readString());
		numTopics = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(name);
		out.writeString(uri.toString());
		out.writeInt(numTopics);
	}

	public static final Parcelable.Creator<Meeting> CREATOR = new Parcelable.Creator<Meeting>() {
		public Meeting createFromParcel(Parcel in) {
			return new Meeting(in);
		}

		public Meeting[] newArray(int size) {
			return new Meeting[size];
		}
	};

	@Override
	public String getContext() {
		return "meeting";
	}

	public Meeting(int id, String name) {
		this.setId(id);
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public int describeContents() {
		return 0;
	}

	public int getNumTopics() {
		return numTopics;
	}

	public void setNumTopics(int numTopics) {
		this.numTopics = numTopics;
	}
}