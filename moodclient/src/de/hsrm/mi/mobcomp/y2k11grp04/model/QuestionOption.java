package de.hsrm.mi.mobcomp.y2k11grp04.model;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestionOption extends BaseModel {
	private Question question;
	private String key;
	private String value;

	public static final String OPTION_RANGE_LABEL_MIN_VALUE = "label_min_value";
	public static final String OPTION_RANGE_LABEL_MID_VALUE = "label_mid_value";
	public static final String OPTION_RANGE_LABEL_MAX_VALUE = "label_max_value";
	public static final String OPTION_RANGE_MIN_VALUE = "min_value";
	public static final String OPTION_RANGE_MAX_VALUE = "max_value";
	public static final String OPTION_MIN_CHOICES = "min_choices";
	public static final String OPTION_MAX_CHOICES = "max_choices";

	public QuestionOption() {
	}

	public QuestionOption(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		key = in.readString();
		value = in.readString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(key);
		out.writeString(value);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public static final Parcelable.Creator<QuestionOption> CREATOR = new Parcelable.Creator<QuestionOption>() {
		public QuestionOption createFromParcel(Parcel in) {
			return new QuestionOption(in);
		}

		public QuestionOption[] newArray(int size) {
			return new QuestionOption[size];
		}
	};

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result
				+ ((question == null) ? 0 : question.hashCode());
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
		QuestionOption other = (QuestionOption) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
