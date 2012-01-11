package de.hsrm.mi.mobcomp.y2k11grp04.model;

import android.os.Parcel;
import android.os.Parcelable;

abstract public class BaseModel implements Parcelable {

	protected int id;
	protected String creationDate;

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(creationDate);
	}

	protected void readFromParcel(Parcel in) {
		id = in.readInt();
		creationDate = in.readString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

}
