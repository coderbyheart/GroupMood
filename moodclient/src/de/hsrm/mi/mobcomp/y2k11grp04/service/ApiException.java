package de.hsrm.mi.mobcomp.y2k11grp04.service;

/**
 * Exeption für alle Fehler, die in der API passieren.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class ApiException extends Exception {
	public ApiException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -4190154176971196624L;
}
