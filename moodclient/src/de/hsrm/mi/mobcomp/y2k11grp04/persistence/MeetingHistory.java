package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import android.net.Uri;

/**
 * Speichert die URL eines {@link Meeting Meetings}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class MeetingHistory extends Entity {
	private Uri meetingUri;

	public Uri getMeetingUri() {
		return meetingUri;
	}

	public void setMeetingUri(Uri meetingUri) {
		this.meetingUri = meetingUri;
	}
}
