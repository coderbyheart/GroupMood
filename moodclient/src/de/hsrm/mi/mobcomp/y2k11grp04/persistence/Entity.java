package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import java.util.Date;

/**
 * Basisklasse für Entities in der Datenbank
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
abstract public class Entity {

	private int id;
	private Date creationDate;

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
