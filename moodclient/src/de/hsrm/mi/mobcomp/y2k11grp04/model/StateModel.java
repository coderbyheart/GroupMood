package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.util.List;

import android.net.Uri;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

/**
 * Interface für Models, die serverseitig gespeichert werden.
 * 
 * Jedes Entitiy kann über eine eindeutige URL identifiziert werden, z.B.
 * http://192.168.1.37:8000/groupmood/meeting/1
 * 
 * Über {@link Relation Relationen} werden beziehungen zu anderen Models
 * definiert.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public interface StateModel extends Model {
	Uri getUri();

	void setUri(Uri uri);

	List<Relation> getRelations();

	void setRelations(List<Relation> relations);

	void setRelationItems(Relation relation, List<? extends StateModel> items);
}
