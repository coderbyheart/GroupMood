package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

import android.os.Parcel;
import android.os.Parcelable;

public class Question extends BaseModel {
	public static final String TYPE_SINGLECHOICE = "singlechoice";
	public static final String TYPE_MULTIPLECHOICE = "multiplechoice";
	public static final String TYPE_RANGE = "range";
	public static final String MODE_SINGLE = "single";
	public static final String MODE_AVERAGE = "avg";

	private Topic topic;
	private String name;
	private String type;
	private String mode;
	private int avg;
	private int numAnswers;
	private List<QuestionOption> options = new ArrayList<QuestionOption>();

	public Question() {
	}

	public Question(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		name = in.readString();
		type = in.readString();
		mode = in.readString();
		avg = in.readInt();
		numAnswers = in.readInt();
		for (Parcelable q : in.readParcelableArray(QuestionOption.class
				.getClassLoader())) {
			options.add((QuestionOption) q);
			((QuestionOption) q).setQuestion(this);
		}
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(name);
		out.writeString(type);
		out.writeString(mode);
		out.writeInt(avg);
		out.writeInt(numAnswers);
		out.writeParcelableArray(
				options.toArray(new QuestionOption[options.size()]), 0);
	}

	public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
		public Question createFromParcel(Parcel in) {
			return new Question(in);
		}

		public Question[] newArray(int size) {
			return new Question[size];
		}
	};

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public List<QuestionOption> getOptions() {
		return options;
	}

	@Override
	public void setRelationItems(Relation relation,
			List<? extends StateModel> items) {
		if (relation.getModel() == QuestionOption.class) {
			options = new ArrayList<QuestionOption>();
			for (StateModel m : items) {
				options.add((QuestionOption) m);
				((QuestionOption) m).setQuestion(this);
			}
		} else {
			super.setRelationItems(relation, items);
		}
	}

	public int getAvg() {
		return avg;
	}

	public void setAvg(int avg) {
		this.avg = avg;
	}

	public int getNumAnswers() {
		return numAnswers;
	}

	public void setNumAnswers(int numAnswers) {
		this.numAnswers = numAnswers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + avg;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numAnswers;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Question other = (Question) obj;
		if (avg != other.avg)
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (numAnswers != other.numAnswers)
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
