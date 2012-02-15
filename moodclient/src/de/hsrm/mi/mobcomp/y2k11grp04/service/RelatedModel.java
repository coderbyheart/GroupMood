package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedModel {
	public Class<? extends Model> model();

}
