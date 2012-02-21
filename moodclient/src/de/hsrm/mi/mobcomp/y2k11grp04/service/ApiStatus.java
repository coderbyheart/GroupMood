package de.hsrm.mi.mobcomp.y2k11grp04.service;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;

/**
 * Stateless Model, dass den Status der Antwort des Servers enthält.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class ApiStatus implements Model {
	public static final String STATUS_OK = "ok";
	private String message;
	private int code;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		ApiStatus other = (ApiStatus) obj;
		if (code != other.code)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
