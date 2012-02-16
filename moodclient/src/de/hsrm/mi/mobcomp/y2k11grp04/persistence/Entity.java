package de.hsrm.mi.mobcomp.y2k11grp04.persistence;

import java.util.Date;

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
