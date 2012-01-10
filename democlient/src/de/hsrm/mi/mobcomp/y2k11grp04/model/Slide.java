package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.net.URL;

import android.net.Uri;

public class Slide implements Model {

	private Meeting meeting;
	private int number;
	private Uri imageUri;
	private URL imageUrl;
	private int vote;

	public Uri getImage() {
		return imageUri;
	}

	public Slide setImage(Uri image) {
		this.imageUri = image;
		return this;
	}

	public Meeting getMeeting() {
		return meeting;
	}

	public Slide setMeeting(Meeting meeting) {
		this.meeting = meeting;
		return this;
	}

	public int getNumber() {
		return number;
	}

	public Slide setNumber(int number) {
		this.number = number;
		return this;
	}

	@Override
	public String getContext() {
		return "slide";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meeting == null) ? 0 : meeting.hashCode());
		result = prime * result + number;
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
		Slide other = (Slide) obj;
		if (meeting == null) {
			if (other.meeting != null)
				return false;
		} else if (!meeting.equals(other.meeting))
			return false;
		if (number != other.number)
			return false;
		return true;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}

	public URL getImageUrl() {
		return imageUrl;
	}

	public Slide setImageUrl(URL imageUrl) {
		this.imageUrl = imageUrl;
		return this;
	}

}
