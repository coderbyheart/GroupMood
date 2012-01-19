package de.hsrm.mi.mobcomp.y2k11grp04;

import java.util.HashMap;
import java.util.Map;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicGalleryAdapter;

public class AttendeeActivity extends ServiceActivity {

	private final int SCREEN_ORIENTATION_PORTRAIT = 1;
	protected ProgressBar loadingProgress;
	protected Meeting meeting;
	protected boolean meetingComplete = false;
	private View topicGallery;
	private Topic currentTopic;
	private Question currentQuestion;
	private TopicGalleryAdapter topicGalleryAdapter;
	private Map<Topic, View> topicViews = new HashMap<Topic, View>();
	private Map<Question, LinearLayout> questionActionViews = new HashMap<Question, LinearLayout>();
	private OnPageChangedListener swipeChangeListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		loadingProgress = (ProgressBar) findViewById(R.id.groupMood_progressBar);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		swipeChangeListener = new OnPageChangedListener() {
			@Override
			public void onPageChanged(int oldPage, int newPage) {
				questionActionViews.get(
						getCurrentTopic().getQuestions().get(oldPage))
						.setVisibility(View.GONE);
				currentQuestion = getCurrentTopic().getQuestions().get(newPage);
				questionActionViews.get(currentQuestion).setVisibility(
						View.VISIBLE);
			}
		};

		updateView();
	}

	/**
	 * Die Anzeige der Aktivity aktualisieren.
	 */
	protected void updateView() {
		TextView meetingName = (TextView) findViewById(R.id.groupMood_meetingName);
		meetingName.setText(meeting.getName());
		if (meetingComplete) {
			loadingProgress.setVisibility(View.GONE);
		} else {
			loadingProgress.setVisibility(View.VISIBLE);
		}

		topicGallery = findViewById(R.id.groupMood_gallery);
		topicGalleryAdapter = new TopicGalleryAdapter(meeting.getTopics());
		GalleryItemClickListener topicGalleryClickListener = new GalleryItemClickListener();

		if (topicGallery instanceof HorizontalListView) {
			HorizontalListView lv = ((HorizontalListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemClickListener(topicGalleryClickListener);
		} else {
			ListView lv = ((ListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemClickListener(topicGalleryClickListener);
		}

		updateTopic();
	}

	protected void updateTopic() {
		if (getResources().getConfiguration().orientation == SCREEN_ORIENTATION_PORTRAIT)
			portrait();
		else
			landscape();
	}

	protected int getLayout() {
		return R.layout.attendee;
	}

	private void landscape() {
		// Enthält alle Layouts zur Darstellung der Fragen eines Topics
		LinearLayout allTopicQuestionsLayout = (LinearLayout) findViewById(R.id.groupMood_allTopicQuestionsLayout);

		// Alle Question-Views aushängen
		allTopicQuestionsLayout.removeAllViews();

		Topic currentTopic = getCurrentTopic();
		if (currentTopic != null) {
			// Gibt es die View schon für das Topic?
			if (!topicViews.containsKey(currentTopic)) {
				Log.v(getClass().getCanonicalName(), "Erzeuge View für "
						+ currentTopic.getName());
				// Layout für das Topic erzeugen
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout topicQuestionsLayout = (LinearLayout) layoutInflater
						.inflate(R.layout.topic_questions,
								allTopicQuestionsLayout, false);
				// Name des Topics
				TextView topicName = (TextView) topicQuestionsLayout
						.findViewById(R.id.groupMood_topicName);
				topicName.setText(currentTopic.getName());

				// Swipe-View zum Durchblättern,
				// enthält die Namen der Fragen
				SwipeView mSwipeView = (SwipeView) topicQuestionsLayout
						.findViewById(R.id.groupMood_questionsSwipe);
				mSwipeView.setOnPageChangedListener(swipeChangeListener);

				// Layout mit den Antwort-Möglichkeiten zur Frage
				LinearLayout topicQuestionActionsLayout = (LinearLayout) topicQuestionsLayout
						.findViewById(R.id.groupMood_questionsActions);

				for (Question q : currentTopic.getQuestions()) {
					// Die Anzeige des Frage-Textes erfolgt in der SwipeView
					View questionView = createQuestionView(q);
					mSwipeView.addView(questionView);
					// Die Frage-Aktion wird in einer anderen View angzeigt,
					// damit man z.B. den SeekBar bedienen kann
					LinearLayout actionView = createQuestionAction(q);
					topicQuestionActionsLayout.addView(actionView);
					// Merken, um beim Swipen umschalten zu können
					questionActionViews.put(q, actionView);
				}

				// Nur Aktion der erste Fragen anzeigen
				int actionCount = topicQuestionActionsLayout.getChildCount();
				if (actionCount > 0) {
					for (int i = 1; i < actionCount; i++) {
						topicQuestionActionsLayout.getChildAt(i).setVisibility(
								View.GONE);
					}
				}

				// Pager für die SwipeView erst initialisieren, wenn alle Fragen
				// drin sind
				PageControl mPageControl = (PageControl) topicQuestionsLayout
						.findViewById(R.id.groupMood_questionsSwipePager);
				mSwipeView.setPageControl(mPageControl);

				// Fertige View merken
				topicViews.put(currentTopic, topicQuestionsLayout);
			}
			// Die View zum aktuellen Topic Anzeigen
			allTopicQuestionsLayout.addView(topicViews.get(currentTopic));
		}
	}

	private void portrait() {
		// FIXME: Hier die Detailansicht bei Topics mit Bild einbauen
		TextView t = (TextView) findViewById(R.id.groupMood_topicName);
		TextView q = (TextView) findViewById(R.id.groupMood_questionName);
		t.setText("");
		q.setText("");
		Topic c = getCurrentTopic();
		if (c != null) {
			t.setText(getCurrentTopic().getName());
			if (currentQuestion != null) {
				q.setText(currentQuestion.getName());
			}
		}
	}

	private LinearLayout createQuestionView(Question q) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) layoutInflater.inflate(
				R.layout.question_name, null);
		TextView questionText = (TextView) view
				.findViewById(R.id.groupMood_question_text);
		questionText.setText(q.getName());
		return view;
	}

	private LinearLayout createQuestionAction(Question q) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) layoutInflater.inflate(
				R.layout.question_action, null);
		Button b = (Button) view
				.findViewById(R.id.groupMood_questionActionButton);
		SeekBar s = (SeekBar) view
				.findViewById(R.id.groupMood_questionActionSeekBar);
		if (q.getType().equals(Question.TYPE_RANGE)) {
			view.removeView(b);
		} else {
			view.removeView(s);
		}
		return view;
	}

	private Topic getCurrentTopic() {
		if (currentTopic == null) {
			if (meeting.getTopics().size() > 0) {
				setCurrentTopic(meeting.getTopics().get(0));
			}
		}
		return currentTopic;
	}

	private void setCurrentTopic(Topic topic) {
		currentTopic = topic;
		currentQuestion = topic.getQuestions().size() > 0 ? topic
				.getQuestions().get(0) : null;
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
					meetingComplete = true;
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
		if (!meetingComplete)
			loadMeetingComplete(meeting);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (meeting != null) {
			outState.putParcelable(MoodServerService.KEY_MEETING_MODEL, meeting);
			outState.putParcelable(MoodServerService.KEY_TOPIC_MODEL,
					currentTopic);
			outState.putParcelable(MoodServerService.KEY_QUESTION_MODEL,
					currentQuestion);
		}
		outState.putBoolean("meetingComplete", meetingComplete);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(MoodServerService.KEY_MEETING_MODEL)) {
			meeting = savedInstanceState
					.getParcelable(MoodServerService.KEY_MEETING_MODEL);
		}
		if (savedInstanceState.containsKey(MoodServerService.KEY_TOPIC_MODEL)) {
			setCurrentTopic((Topic) savedInstanceState
					.getParcelable(MoodServerService.KEY_TOPIC_MODEL));
		}
		if (savedInstanceState
				.containsKey(MoodServerService.KEY_QUESTION_MODEL)) {
			currentQuestion = (Question) savedInstanceState
					.getParcelable(MoodServerService.KEY_QUESTION_MODEL);
		}
		if (savedInstanceState.containsKey("meetingComplete")) {
			meetingComplete = savedInstanceState.getBoolean("meetingComplete");
		}

		updateView();
	}

	private class GalleryItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> item, View parent, int position,
				long arg3) {
			setCurrentTopic(topicGalleryAdapter.getItem(position));
			updateTopic();
		}
	}
}