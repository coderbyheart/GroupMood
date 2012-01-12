package de.hsrm.mi.mobcomp.y2k11grp04;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

public class AttendeeActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attendee);

		WebView w = (WebView) findViewById(R.id.webView1);
		String summary = "";

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		Meeting m = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);
		summary += "<h1>" + m.getName() + "</h1>";
		summary += "<h2>Topics</h2>";
		summary += "<ul>";
		for (Topic t : m.getTopics()) {
			summary += "<li>" + t.getName();
			summary += "<br>Questions:";
			summary += "<ul>";
			for (Question q : t.getQuestions()) {
				summary += "<li>" + q.getName();
				summary += "<br>Type: " + q.getType();
				summary += "<br>Mode: " + q.getMode();
				summary += "<br>Average: " + q.getAvg();
				summary += "<br>Options:";
				summary += "<ul>";
				for (QuestionOption o : q.getOptions()) {
					summary += "<li>" + o.getKey() + " = " + o.getValue()
							+ "</li>";
				}
				summary += "</ul>";
				summary += "</li>";
			}
			summary += "</ul>";
			summary += "</li>";
		}
		summary += "</ul>";

		w.loadData("<html><body>" + summary + "</body></html>", "text/html",
				null);
	}
}