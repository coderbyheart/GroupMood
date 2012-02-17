package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

public class Meeting extends BaseModel {
	private String name;
	private String flags;
	private int numTopics;
	private List<Topic> topics = new ArrayList<Topic>();
	public static final String FLAG_FOTOVOTE = "fotovote";

	public Meeting() {
	}

	public Meeting(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		name = in.readString();
		numTopics = in.readInt();
		for (Parcelable t : in
				.readParcelableArray(Topic.class.getClassLoader())) {
			topics.add((Topic) t);
			((Topic) t).setMeeting(this);
		}
		this.setFlags(in.readString());
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(name);
		out.writeInt(numTopics);
		out.writeParcelableArray(topics.toArray(new Topic[topics.size()]), 0);
		out.writeString(this.getFlags());
	}

	public static final Parcelable.Creator<Meeting> CREATOR = new Parcelable.Creator<Meeting>() {
		public Meeting createFromParcel(Parcel in) {
			return new Meeting(in);
		}

		public Meeting[] newArray(int size) {
			return new Meeting[size];
		}
	};

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

	public int getNumTopics() {
		return numTopics;
	}

	public void setNumTopics(int numTopics) {
		this.numTopics = numTopics;
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public boolean hasFlag(String flag) {
		if (!flags.contains("|"))
			return flags.equals(flag);
		return Arrays.asList(TextUtils.split(flags, "|")).contains(flag);
	}

	@Override
	public void setRelationItems(Relation relation,
			List<? extends StateModel> items) {
		if (relation.getModel() == Topic.class) {
			topics = new ArrayList<Topic>();
			for (StateModel m : items) {
				topics.add((Topic) m);
				((Topic) m).setMeeting(this);
			}
		} else {
			super.setRelationItems(relation, items);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((flags == null) ? 0 : flags.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Meeting other = (Meeting) obj;
		if (flags == null) {
			if (other.flags != null)
				return false;
		} else if (!flags.equals(other.flags))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}