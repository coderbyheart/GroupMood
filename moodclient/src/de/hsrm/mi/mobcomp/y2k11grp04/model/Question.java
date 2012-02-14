package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

public class Question extends BaseModel {
	public static final String TYPE_CHOICE = "choice";
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
	private List<Choice> choices = new ArrayList<Choice>();
	private List<AnswerAverage> answerAverages = new ArrayList<AnswerAverage>();

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
		for (Parcelable c : in.readParcelableArray(Choice.class
				.getClassLoader())) {
			choices.add((Choice) c);
			((Choice) c).setQuestion(this);
		}
		for (Parcelable c : in.readParcelableArray(AnswerAverage.class
				.getClassLoader())) {
			answerAverages.add((AnswerAverage) c);
			((AnswerAverage) c).setQuestion(this);
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
		out.writeParcelableArray(choices.toArray(new Choice[choices.size()]), 0);
		out.writeParcelableArray(answerAverages
				.toArray(new AnswerAverage[answerAverages.size()]), 0);
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

	public boolean isRangeType() {
		return getType().equals(TYPE_RANGE);
	}

	public boolean isChoiceType() {
		return getType().equals(TYPE_CHOICE);
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

	public List<Choice> getChoices() {
		return choices;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}

	public List<AnswerAverage> getAnswerAverages() {
		// TODO: Serverseitig implementieren
		ArrayList<AnswerAverage> aavg = new ArrayList<AnswerAverage>();

		AnswerAverage a1 = new AnswerAverage();
		a1.setAnswer("Rot");
		a1.setAverage(25);
		a1.setNumVotes(3);

		AnswerAverage a2 = new AnswerAverage();
		a2.setAnswer("Gelb");
		a2.setAverage(50);
		a2.setNumVotes(5);

		AnswerAverage a3 = new AnswerAverage();
		a3.setAnswer("Grün");
		a3.setAverage(75);
		a3.setNumVotes(7);

		aavg.add(a3);
		aavg.add(a2);
		aavg.add(a1);

		return aavg;
		// return answerAverages;
	}

	public void setAnswerAverages(List<AnswerAverage> answerAverages) {
		this.answerAverages = answerAverages;
	}

	/**
	 * Gibt die Option mit dem Namen name zurück, existiert diese Option nicht,
	 * wird defaultValue zurück gegeben
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getOption(String name, String defaultValue) {
		for (QuestionOption o : getOptions()) {
			if (o.getKey().equals(name))
				return o.getValue();
		}
		Log.w(getClass().getCanonicalName(), "No " + name
				+ " option on question " + getUri());
		Log.d(getClass().getCanonicalName(), "Available options are:");
		for (QuestionOption o : getOptions()) {
			Log.d(getClass().getCanonicalName(),
					o.getKey() + ": " + o.getValue() + " (" + o.getUri() + ")");
		}
		return defaultValue;
	}

	/**
	 * Gibt die Option mit dem Namen name zurück, existiert diese Option nicht,
	 * wird null zurück gegeben
	 * 
	 * @param name
	 * @return
	 */
	public String getOption(String name) {
		return getOption(name, null);
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
		} else if (relation.getModel() == Choice.class) {
			choices = new ArrayList<Choice>();
			for (StateModel m : items) {
				choices.add((Choice) m);
				((Choice) m).setQuestion(this);
			}
		} else if (relation.getModel() == AnswerAverage.class) {
			answerAverages = new ArrayList<AnswerAverage>();
			for (StateModel m : items) {
				answerAverages.add((AnswerAverage) m);
				((AnswerAverage) m).setQuestion(this);
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

	/**
	 * Gibt den Min-Wert zurück, falls vorhanden
	 * 
	 * @return
	 */
	public Integer getMinChoices() {
		return Integer.parseInt(getOption(QuestionOption.OPTION_MIN_CHOICES));
	}

	/**
	 * Gibt Max-Wert zurück, falls vorhanden
	 * 
	 * @return
	 */
	public Integer getMaxChoices() {
		return Integer.parseInt(getOption(QuestionOption.OPTION_MAX_CHOICES));
	}

	/**
	 * Gibt den Min-Wert zurück, falls vorhanden
	 * 
	 * @return
	 */
	public Integer getMinOption() {
		return Integer
				.parseInt(getOption(QuestionOption.OPTION_RANGE_MIN_VALUE));
	}

	/**
	 * Gibt Max-Wert zurück, falls vorhanden
	 * 
	 * @return
	 */
	public Integer getMaxOption() {
		return Integer
				.parseInt(getOption(QuestionOption.OPTION_RANGE_MAX_VALUE));
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
		result = prime * result + ((choices == null) ? 0 : choices.hashCode());
		result = prime * result
				+ ((answerAverages == null) ? 0 : answerAverages.hashCode());
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
		if (choices == null) {
			if (other.choices != null)
				return false;
		} else if (!choices.equals(other.choices))
			return false;
		if (answerAverages == null) {
			if (other.answerAverages != null)
				return false;
		} else if (!answerAverages.equals(other.answerAverages))
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

	public Integer getValueAt(double d) {
		if (d < 0 || d > 1)
			throw new InvalidParameterException(
					"progress must be between 0 and 1");
		return (int) (getMinOption() + (getMaxOption() - getMinOption()) * d);
	}
}
