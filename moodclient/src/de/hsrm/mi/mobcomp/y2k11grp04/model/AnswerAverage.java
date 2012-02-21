package de.hsrm.mi.mobcomp.y2k11grp04.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Beschreibt die zusammengefassten Ergebnisse aller {@link Answer Antworten} einer {@link Question Frage}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class AnswerAverage implements Model, Parcelable {
	private Question question;
	private int average;
	private String answer;
	private int numVotes;

	public AnswerAverage() {
	}

	public AnswerAverage(Parcel in) {
		readFromParcel(in);
	}

	protected void readFromParcel(Parcel in) {
		average = in.readInt();
		numVotes = in.readInt();
		answer = in.readString();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(average);
		out.writeInt(numVotes);
		out.writeString(answer);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public static final Parcelable.Creator<AnswerAverage> CREATOR = new Parcelable.Creator<AnswerAverage>() {
		public AnswerAverage createFromParcel(Parcel in) {
			return new AnswerAverage(in);
		}

		public AnswerAverage[] newArray(int size) {
			return new AnswerAverage[size];
		}
	};

	public int getAverage() {
		return average;
	}

	public void setAverage(int average) {
		this.average = average;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String value) {
		this.answer = value;
	}

	public int getNumVotes() {
		return numVotes;
	}

	public void setNumVotes(int numVotes) {
		this.numVotes = numVotes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
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
		AnswerAverage other = (AnswerAverage) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
