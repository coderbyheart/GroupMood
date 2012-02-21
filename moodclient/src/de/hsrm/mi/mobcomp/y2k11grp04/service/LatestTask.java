package de.hsrm.mi.mobcomp.y2k11grp04.service;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

/**
 * Mithilfe dieser Klasse wird verhindert, dass mehrmals die gleiche Aufgabe
 * eines Empfängers nacheinander ausgeführt wird.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class LatestTask {
	private int what;
	private Messenger replyTo;
	private Bundle data;

	public LatestTask(Message request) {
		setWhat(request.what);
		setReplyTo(request.replyTo);
		setData((Bundle) request.getData().clone());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		if (getReplyTo() == null) {
			result += getReplyTo().hashCode();
		}
		result += getWhat();
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
		LatestTask other = (LatestTask) obj;
		if (getReplyTo() == null) {
			if (other.getReplyTo() != null) {
				return false;
			}
		} else {
			if (!getReplyTo().equals(other.getReplyTo())) {
				return false;
			}
		}
		if (getWhat() != other.getWhat()) {
			return false;
		}
		return true;
	}

	public int getWhat() {
		return what;
	}

	public void setWhat(int what) {
		this.what = what;
	}

	public Messenger getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(Messenger replyTo) {
		this.replyTo = replyTo;
	}

	public Bundle getData() {
		return data;
	}

	public void setData(Bundle data) {
		this.data = data;
	}
}
