package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import android.net.Uri;

public class MeetingHistory extends Entity {
	private Uri meetingUri;

	public Uri getMeetingUri() {
		return meetingUri;
	}

	public void setMeetingUri(Uri meetingUri) {
		this.meetingUri = meetingUri;
	}
}
