package de.hsrm.mi.mobcomp.y2k11grp04.model;

public class QuestionOption {

	private Question question;
	private String key;
	private String value;
	private String creation_date;
	private int id;

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @param question
	 *            the question to set
	 * @return
	 */
	public QuestionOption setQuestion(Question question) {
		this.question = question;
		return this;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 * @return
	 */
	public QuestionOption setKey(String key) {
		this.key = key;
		return this;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 * @return
	 */
	public QuestionOption setValue(String value) {
		this.value = value;
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
	public QuestionOption setCreation_date(String creation_date) {
		this.creation_date = creation_date;
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
	public QuestionOption setId(int id) {
		this.id = id;
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
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result
				+ ((question == null) ? 0 : question.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		QuestionOption other = (QuestionOption) obj;
		if (creation_date == null) {
			if (other.creation_date != null)
				return false;
		} else if (!creation_date.equals(other.creation_date))
			return false;
		if (id != other.id)
			return false;
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
