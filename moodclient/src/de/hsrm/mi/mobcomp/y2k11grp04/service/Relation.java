package de.hsrm.mi.mobcomp.y2k11grp04.service;

import android.net.Uri;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;

public class Relation implements Model {
	private Uri relatedcontext;
	private Class<? extends Model> model;
	private boolean list;
	private Uri href;

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public Uri getHref() {
		return href;
	}

	public void setHref(Uri href) {
		this.href = href;
	}

	public Uri getRelatedcontext() {
		return relatedcontext;
	}

	public void setRelatedcontext(Uri relatedcontext) {
		this.relatedcontext = relatedcontext;
	}

	public Class<? extends Model> getModel() {
		return model;
	}

	public void setModel(Class<? extends Model> model) {
		this.model = model;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result + (list ? 1231 : 1237);
		result = prime * result
				+ ((relatedcontext == null) ? 0 : relatedcontext.hashCode());
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
		Relation other = (Relation) obj;
		if (href == null) {
			if (other.href != null)
				return false;
		} else if (!href.equals(other.href))
			return false;
		if (list != other.list)
			return false;
		if (relatedcontext == null) {
			if (other.relatedcontext != null)
				return false;
		} else if (!relatedcontext.equals(other.relatedcontext))
			return false;
		return true;
	}
}
