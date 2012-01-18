package de.hsrm.mi.mobcomp.y2k11grp04;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class ChairActivity extends AttendeeActivity {

	protected int getLayout() {
		return R.layout.chair;
	}

	@Override
	protected void updateWebView() {

		String summary = "<h1>" + meeting.getName() + "</h1>";
		for (Topic t : meeting.getTopics()) {
			summary += "<h2>" + t.getName() + "</h2>";
			summary += "<ul>";
			for (Question q : t.getQuestions()) {
				summary += "<li>" + q.getName();
				summary += "<br>Wertung: " + q.getAvg();
				summary += "<br>Antworten: " + q.getNumAnswers();
				summary += "</li>";
			}
			summary += "</ul>";
		}

		webView.loadData("<html><body>" + summary + "</body></html>",
				"text/html", null);
	}
}