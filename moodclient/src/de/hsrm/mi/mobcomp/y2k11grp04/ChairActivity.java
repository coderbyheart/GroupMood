package de.hsrm.mi.mobcomp.y2k11grp04;

import android.webkit.WebView;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class ChairActivity extends AttendeeActivity {

	protected int getLayout() {
		return R.layout.chair;
	}
	
	/**
	 * Die Anzeige der Aktivity aktualisieren.
	 */
	@Override
	protected void updateView()
	{
		updateDetailView();
	}

	@Override
	protected void updateDetailView() {
		WebView webView = (WebView) findViewById(R.id.groupMood_detailWebView);
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