package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.List;

import android.net.Uri;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

public interface StateModel extends Model {
	Uri getUri();
	void setUri(Uri uri);
	List<Relation> getRelations();
	void setRelations(List<Relation> relations);
	void setRelationItems(Relation relation, List<? extends StateModel> items);
}

