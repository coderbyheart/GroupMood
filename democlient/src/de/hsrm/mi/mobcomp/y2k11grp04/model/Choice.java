package de.hsrm.mi.mobcomp.y2k11grp04.model;

public class Choice implements Model {
	private Question question;
	private String name;
	private String creation_date;

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
	public Choice setQuestion(Question question) {
		this.question = question;
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
	public Choice setCreation_date(String creation_date) {
		this.creation_date = creation_date;
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
	public Choice setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
