package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;

/**
 * Annotation um zu beschreiben, welches Model ein Listen-Setter eines anderen
 * Models akzeptiert. Verwendet z.B. in
 * {@link Question#setAverageAnswers(java.util.List)}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedModel {
	public Class<? extends Model> model();

}
