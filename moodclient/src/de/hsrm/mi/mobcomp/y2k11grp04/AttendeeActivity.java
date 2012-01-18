package de.hsrm.mi.mobcomp.y2k11grp04;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicGalleryAdapter;

public class AttendeeActivity extends ServiceActivity {

	private final int SCREEN_ORIENTATION_PORTRAIT = 1;
	protected ProgressBar loadingProgress;
	protected Meeting meeting;
	private View gallery;
	private Topic currentTopic;
	private Question currentQuestion;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		loadingProgress = (ProgressBar) findViewById(R.id.progressBar);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		updateView();
	}
	
	/**
	 * Die Anzeige der Aktivity aktualisieren.
	 */
	protected void updateView()
	{
		TextView meetingName = (TextView)findViewById(R.id.meetingName);
		meetingName.setText(meeting.getName());
		if (getResources().getConfiguration().orientation == SCREEN_ORIENTATION_PORTRAIT)
			portrait();
		else
			landscape();
	}

	protected int getLayout() {
		return R.layout.attendee;
	}

	private void landscape() {
		gallery = findViewById(R.id.gallery);
		((ListView) gallery).setAdapter(new TopicGalleryAdapter(meeting
				.getTopics()));

		PageControl mPageControl = (PageControl) findViewById(R.id.page_control);
		SwipeView mSwipeView = (SwipeView) findViewById(R.id.swipe_view);
		mSwipeView.setPageControl(mPageControl);

		Topic currentTopic = getCurrentTopic();
		if (currentTopic != null) {
			for (Question q : currentTopic.getQuestions()) {
				FrameLayout questionView = new FrameLayout(this);
				mSwipeView.addView(questionView);
				TextView questionText = (TextView) createSwipeTextView();
				questionText.setText(q.getName());
				questionView.addView(questionText);
			}
		}
	}

	private void portrait() {
		gallery = findViewById(R.id.gallery);
		((HorizontalListView) gallery).setAdapter(new TopicGalleryAdapter(
				meeting.getTopics()));
		updateDetailView();
	}

	private View createSwipeTextView() {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.question, null);
		TextView tv = (TextView) view.findViewById(R.id.tv_question);
		tv.setTextSize(16);
		tv.setSingleLine(false);
		return view;
	}

	private Topic getCurrentTopic() {
		if (currentTopic == null) {
			if (meeting.getTopics().size() > 0) {
				currentTopic = meeting.getTopics().get(0);
			}
		}
		return currentTopic;
	}

	/**
	 * Aktualisiert die Detailansicht
	 */
	protected void updateDetailView() {
		WebView webView = (WebView) findViewById(R.id.detailWebView);
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
					updateView();
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