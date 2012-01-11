package de.hsrm.mi.mobcomp.y2k11grp04.functions;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class SlideRatAct {

	private void bsp() {
		Meeting m = new Meeting();
		m.setName("Vorlesung");
		
		Topic t = new Topic();
		t.setName("Wie bewerten sie dieses Meeting?");
		t.setMeeting(m);
		m.addTopic(t);

		Question q = new Question();
		q.setName("Geschwindigkeit");
		q.setTopic(t);

		Question q2 = new Question();
		q2.setName("Aussehen");
		q2.setTopic(t);

		QuestionOption minval = new QuestionOption();
		minval.setKey("min_value");
		minval.setValue("0");
		minval.setQuestion(q);
		
		QuestionOption maxval = new QuestionOption();
		maxval.setKey("max_value");
		maxval.setValue("0");
		maxval.setQuestion(q);

	}
}
