package de.hsrm.mi.mobcomp.y2k11grp04;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;

public class AttendeeActivity extends ServiceActivity {

	protected ProgressBar loadingProgress;
	protected Meeting meeting;
	protected WebView webView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		loadingProgress = (ProgressBar) findViewById(R.id.progressBar);
		webView = (WebView) findViewById(R.id.webView1);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		updateWebView();
	}

	protected int getLayout() {
		return R.layout.attendee;
	}

	protected void updateWebView() {
		String summary = "";
		summary += "<h1>" + meeting.getName() + "</h1>";
		if (meeting.getTopics().size() > 0) {
			summary += "<h2>Topics</h2>";
			summary += "<ul>";
			for (Topic t : meeting.getTopics()) {
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
		}

		webView.loadData("<html><body>" + summary + "</body></html>",
				"text/html", null);
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		switch (message.what) {
		case MoodServerService.MSG_MEETING_COMPLETE_PROGRESS:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					loadingProgress.setMax(serviceMessage.arg2);
					loadingProgress.setProgress(serviceMessage.arg1);
					if (serviceMessage.arg1 == serviceMessage.arg2) {
						loadingProgress.setVisibility(View.GONE);
					} else {
						loadingProgress.setVisibility(View.VISIBLE);
					}
				}
			};
		case MoodServerService.MSG_MEETING_COMPLETE_RESULT:
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Bundle b = serviceMessage.getData();
					b.setClassLoader(getClassLoader());
					meeting = b
							.getParcelable(MoodServerService.KEY_MEETING_MODEL);
					updateWebView();
				}
			};
		default:
			return super.getServiceMessageRunnable(message);
		}
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		// Meeting vollständig laden
		loadMeetingComplete(meeting);
	}
}