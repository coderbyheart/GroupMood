package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.ArrayList;
import java.util.List;

public class Question implements Model {

	private Topic topic;
	private int id;
	private String name;
	private String type;
	private String modus;
	private String creation_date;
	private List<QuestionOption> questionOptions = new ArrayList<QuestionOption>();
	private List<Choice> choices = new ArrayList<Choice>();

	public void addChoice(Choice c) {
		choices.add(c);
	}

	@Override
	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addQuestionOption(QuestionOption qo) {
		questionOptions.add(qo);
	}

	/**
	 * @return the topic
	 */
	public Topic getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            the topic to set
	 * @return
	 */
	public Question setTopic(Topic topic) {
		this.topic = topic;
		return this;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 * @return
	 */
	public Question setId(int id) {
		this.id = id;
		return this;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 * @return
	 */
	public Question setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 * @return
	 */
	public Question setType(String type) {
		this.type = type;
		return this;
	}

	/**
	 * @return the modus
	 */
	public String getModus() {
		return modus;
	}

	/**
	 * @param modus
	 *            the modus to set
	 * @return
	 */
	public Question setModus(String modus) {
		this.modus = modus;
		return this;
	}

	/**
	 * @return the creation_date
	 */
	public String getCreation_date() {
		return creation_date;
	}

	/**
	 * @param creation_date
	 *            the creation_date to set
	 * @return
	 */
	public Question setCreation_date(String creation_date) {
		this.creation_date = creation_date;
		return this;
	}

	/**
	 * @return the questionOptions
	 */
	public List<QuestionOption> getQuestionOptions() {
		return questionOptions;
	}

	/**
	 * @param questionOptions
	 *            the questionOptions to set
	 * @return
	 */
	public Question setQuestionOptions(List<QuestionOption> questionOptions) {
		this.questionOptions = questionOptions;
		return this;
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
		result = prime * result
				+ ((creation_date == null) ? 0 : creation_date.hashCode());
		result = prime * result + id;
		result = prime * result + ((modus == null) ? 0 : modus.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Question other = (Question) obj;
		if (creation_date == null) {
			if (other.creation_date != null)
				return false;
		} else if (!creation_date.equals(other.creation_date))
			return false;
		if (id != other.id)
			return false;
		if (modus == null) {
			if (other.modus != null)
				return false;
		} else if (!modus.equals(other.modus))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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

	/**
	 * @return the choices
	 */
	public List<Choice> getChoices() {
		return choices;
	}

	/**
	 * @param choices the choices to set
	 */
	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
}
