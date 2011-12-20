package de.hsrm.mi.mobcomp.y2k11grp04.model;

public class Vote implements Model {

	private Meeting meeting;
	private int vote;

	public Vote(Meeting meeting, int vote) {
		this.setMeeting(meeting);
		this.setVote(vote);
	}

	public Meeting getMeeting() {
		return meeting;
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meeting == null) ? 0 : meeting.hashCode());
		result = prime * result + vote;
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
		Vote other = (Vote) obj;
		if (meeting == null) {
			if (other.meeting != null)
				return false;
		} else if (!meeting.equals(other.meeting))
			return false;
		if (vote != other.vote)
			return false;
		return true;
	}

	@Override
	public String getContext() {
		return "vote";
	}

}
