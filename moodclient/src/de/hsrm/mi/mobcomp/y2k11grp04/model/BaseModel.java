package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

abstract public class BaseModel implements StateModel, Parcelable {

	private int id;
	private Date creationDate;
	private Uri uri;
	private List<Relation> relations = new ArrayList<Relation>();

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		if (creationDate != null) {
			out.writeLong(creationDate.getTime());
		} else {
			out.writeLong(0);
		}
		out.writeString(uri.toString());
	}

	protected void readFromParcel(Parcel in) {
		id = in.readInt();
		long dateTime = in.readLong();
		if (dateTime > 0) {
			creationDate = new Date(dateTime);
		}
		uri = Uri.parse(in.readString());
	}

	public int describeContents() {
		return 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public List<Relation> getRelations() {
		return relations;
	}

	@Override
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	@Override
	public void setRelationItems(Relation relation,
			List<? extends StateModel> items) {
		Log.w(getClass().getCanonicalName(), "Unhandled setRelationItems() on "
				+ getClass().getCanonicalName() + " and related objects "
				+ relation.getModel().getCanonicalName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + id;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		BaseModel other = (BaseModel) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (id != other.id)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}
