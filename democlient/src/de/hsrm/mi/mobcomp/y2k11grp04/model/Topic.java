package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Coralie Reuter
 * 
 */
public class Topic implements Model {
	private Meeting meeting;
	private int id;
	private String creation_date;
	private URL image;
	private String name;
	private List<Question> questions = new ArrayList<Question>();

	public void addQuestion(Question q) {
		questions.add(q);
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
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((meeting == null) ? 0 : meeting.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Topic other = (Topic) obj;
		if (creation_date == null) {
			if (other.creation_date != null)
				return false;
		} else if (!creation_date.equals(other.creation_date))
			return false;
		if (id != other.id)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (meeting == null) {
			if (other.meeting != null)
				return false;
		} else if (!meeting.equals(other.meeting))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Meeting getMeeting() {
		return meeting;
	}

	public Topic setMeeting(Meeting meeting) {
		this.meeting = meeting;
		return this;
	}

	public int getId() {
		return id;
	}

	public Topic setId(int id) {
		this.id = id;
		return this;

	}

	public String getCreation_date() {
		return creation_date;
	}

	public Topic setCreation_date(String creation_date) {
		this.creation_date = creation_date;
		return this;
	}

	public URL getImage() {
		return image;
	}

	public Topic setImage(URL image) {
		this.image = image;
		return this;
	}

	public String getName() {
		return name;
	}

	public Topic setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return the questions
	 */
	public List<Question> getQuestions() {
		return questions;
	}

	/**
	 * @param questions
	 *            the questions to set
	 * @return
	 */
	public Topic setQuestions(List<Question> questions) {
		this.questions = questions;
		return this;
	}

	@Override
	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

}