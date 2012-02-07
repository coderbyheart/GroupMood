package de.hsrm.mi.mobcomp.y2k11grp04;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.devsmart.android.ui.HorizontalListView;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;
import de.hsrm.mi.mobcomp.y2k11grp04.view.SeekBarState;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicGalleryAdapter;

public class QuestionActivity extends ServiceActivity { 

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
	private Map<SeekBar, Question> seekbarToQuestion = new HashMap<SeekBar, Question>();
	private Map<SeekBar, TextView> seekbarToCurrentValueTextView = new HashMap<SeekBar, TextView>();
	private Map<Integer, Integer> seekBarState = new HashMap<Integer, Integer>();
	private OnSeekBarChangeListener questionActionSeekBarListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		loadingProgress = (ProgressBar) findViewById(R.id.groupMood_progressBar);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		// Kümmert sich um das Wechseln der Fragen durch eine Swipe-Geste
		swipeChangeListener = new QuestionSwipeListener();

		// Kümmert sich um das Setzen der Antwort, falls diese einen
		// Slider verwendet
		questionActionSeekBarListener = new QuestionSeekBarListener();

		updateView();
	}

	/**
	 * Die Anzeige der Aktivity aktualisieren.
	 */
	protected void updateView() {
		topicGallery = findViewById(R.id.groupMood_gallery);
		topicGalleryAdapter = new TopicGalleryAdapter(meeting.getTopics());
		GalleryItemClickListener topicGalleryClickListener = new GalleryItemClickListener();

		if (topicGallery instanceof HorizontalListView) {
			HorizontalListView lv = ((HorizontalListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemClickListener(topicGalleryClickListener);
			lv.setOnItemLongClickListener(topicGalleryClickListener);
		} else {
			ListView lv = ((ListView) topicGallery);
			lv.setAdapter(topicGalleryAdapter);
			lv.setOnItemClickListener(topicGalleryClickListener);
			lv.setOnItemLongClickListener(topicGalleryClickListener);
		}

		updateTopic();
	}

	protected int getLayout() {
		return R.layout.attendee;
	}

	private void updateTopic() {
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

				int num = 0;
				for (Question q : currentTopic.getQuestions()) {
					// Die Anzeige des Frage-Textes erfolgt in der SwipeView
					View questionView = createQuestionView(q, ++num);
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

				// Zur aktuellen Frage springen
				if (currentQuestion != null) {
					int page = 0;
					for (Question q : currentTopic.getQuestions()) {
						if (currentQuestion.equals(q)) {
							mSwipeView.scrollToPage(page);
						}
						page++;
					}
				}
			}
			// Die View zum aktuellen Topic Anzeigen
			allTopicQuestionsLayout.addView(topicViews.get(currentTopic));
		}
	}

	private LinearLayout createQuestionView(Question q, Integer number) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) layoutInflater.inflate(
				R.layout.question_name, null);
		((TextView) view.findViewById(R.id.groupMood_question_text)).setText(q
				.getName());
		((TextView) view.findViewById(R.id.groupMood_question_number))
				.setText("" + number);
		((TextView) view.findViewById(R.id.groupMood_question_total))
				.setText(String.format(
						getResources().getString(R.string.question_total), q
								.getTopic().getQuestions().size()));
		return view;
	}

	private LinearLayout createQuestionAction(final Question q) {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) layoutInflater.inflate(
				R.layout.question_action, null);

		if (q.getType().equals(Question.TYPE_RANGE)) {
			view.removeView(view
					.findViewById(R.id.groupMood_questionActionButton));
			SeekBar s = (SeekBar) view
					.findViewById(R.id.groupMood_questionActionSeekBar);
			if (seekBarState.containsKey(q.getId())) {
				s.setProgress(seekBarState.get(q.getId()));
			}
			// Min/Max aus Question ziehen, da eine Seekbar IMMER bei 0 anfängt.
			// Also merken, wird dann später im SeekBarChangeListener verwendet
			seekbarToQuestion.put(s, q);
			// Labels
			TextView minValueLabel = (TextView) view
					.findViewById(R.id.groupMood_questionActionMinLabel);
			TextView midValueLabel = (TextView) view
					.findViewById(R.id.groupMood_questionActionMidLabel);
			TextView maxValueLabel = (TextView) view
					.findViewById(R.id.groupMood_questionActionMaxLabel);
			minValueLabel.setText(q.getOption(
					QuestionOption.OPTION_RANGE_LABEL_MIN_VALUE,
					"" + q.getMinOption()));
			midValueLabel.setText(q.getOption(
					QuestionOption.OPTION_RANGE_LABEL_MID_VALUE,
					"" + q.getValueAt(0.5)));
			maxValueLabel.setText(q.getOption(
					QuestionOption.OPTION_RANGE_LABEL_MAX_VALUE,
					"" + q.getMaxOption()));
			TextView currentValue = (TextView) view
					.findViewById(R.id.groupMood_questionActionCurrentValue);
			currentValue.setText("" + q.getValueAt(s.getProgress() / 100.0));
			seekbarToCurrentValueTextView.put(s, currentValue);
			// Listener
			s.setOnSeekBarChangeListener(questionActionSeekBarListener);
		} else {
			view.removeView(view
					.findViewById(R.id.groupMood_questionActionRangeLayout));

			final CharSequence[] questionOptionNames = new CharSequence[q
					.getChoices().size()];
			for (int i = 0; i < q.getChoices().size(); i++) {
				questionOptionNames[i] = q.getChoices().get(i).getName();
			}

			Button b = (Button) view
					.findViewById(R.id.groupMood_questionActionButton);

			b.setOnClickListener(new AnswerChoiceSelectListener(q,
					questionOptionNames));
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
			// Wird aufgerufen, wenn der Server Fortschritt beim Laden des
			// Meetings hat
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					loadingProgress.setMax(serviceMessage.arg2);
					loadingProgress.setProgress(serviceMessage.arg1);
					if (loadingProgress.getProgress() >= loadingProgress
							.getMax()) {
						loadingProgress.setVisibility(View.GONE);
					} else {
						loadingProgress.setVisibility(View.VISIBLE);
					}
				}
			};
		case MoodServerService.MSG_MEETING_COMPLETE_RESULT:
			// Wird aufgerufen, wenn das Meeting vollständig geladen ist
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					Bundle b = serviceMessage.getData();
					b.setClassLoader(getClassLoader());
					meeting = b
							.getParcelable(MoodServerService.KEY_MEETING_MODEL);
					meetingComplete = true;
					dismissDialog(DIALOG_LOADING);
					// If you are using onCreateDialog(int) to manage the state
					// of your dialogs, then every time your dialog is
					// dismissed, the state of the Dialog object is retained by
					// the Activity. If you decide that you will no longer need
					// this object or it's important that the state is cleared,
					// then you should call removeDialog(int). This will remove
					// any internal references to the object and if the dialog
					// is showing, it will dismiss it.
					removeDialog(DIALOG_LOADING);
					updateView();
				}
			};
		case MoodServerService.MSG_TOPIC_IMAGE_RESULT:
			// Wird aufgerufen, wenn der Service das Bild zu einem Topic geladen
			// hat
			return new ServiceMessageRunnable(message) {
				@Override
				public void run() {
					// Und Bild aus Datei setzen
					Integer topicId = serviceMessage.getData().getInt(
							MoodServerService.KEY_TOPIC_ID);
					for (Topic t : meeting.getTopics()) {
						if (t.getId() == topicId) {
							t.setImageFile(new File(serviceMessage.getData()
									.getString(
											MoodServerService.KEY_TOPIC_IMAGE)));
						}
					}
				}
			};
		default:
			return super.getServiceMessageRunnable(message);
		}
	}

	@Override
	protected void onConnect() {
		Log.v(getClass().getCanonicalName(), "onConnect");
		super.onConnect();
		// Meeting vollständig laden
		Log.v(getClass().getCanonicalName(), "meetingComplete in onConnect(): "
				+ (meetingComplete ? "JA" : "NEIN"));
		if (!meetingComplete) {
			showDialog(DIALOG_LOADING);
			loadMeetingComplete(meeting);
		}
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
		outState.putBoolean("LOADING_HIDDEN",
				loadingProgress.getVisibility() != View.VISIBLE);
		outState.putBoolean("meetingComplete", meetingComplete);

		// Zustand der Seekbars merken
		ArrayList<SeekBarState> seekBarStates = new ArrayList<SeekBarState>();
		for (Integer questionId : seekBarState.keySet()) {
			seekBarStates.add(new SeekBarState().setIdentifier(questionId)
					.setProgress(seekBarState.get(questionId)));
		}
		outState.putParcelableArrayList("seekbarStates", seekBarStates);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.v(getClass().getCanonicalName(), "onRestoreInstanceState");
		if (savedInstanceState.containsKey(MoodServerService.KEY_MEETING_MODEL)) {
			meeting = savedInstanceState
					.getParcelable(MoodServerService.KEY_MEETING_MODEL);
			setCurrentTopic((Topic) savedInstanceState
					.getParcelable(MoodServerService.KEY_TOPIC_MODEL));
			currentQuestion = (Question) savedInstanceState
					.getParcelable(MoodServerService.KEY_QUESTION_MODEL);
		}
		meetingComplete = savedInstanceState.getBoolean("meetingComplete");
		if (savedInstanceState.getBoolean("LOADING_HIDDEN"))
			loadingProgress.setVisibility(View.GONE);

		// Zustand der Seekbars laden
		ArrayList<SeekBarState> seekBarStates = savedInstanceState
				.getParcelableArrayList("seekbarStates");
		for (SeekBarState s : seekBarStates) {
			seekBarState.put(s.getIdentifier(), s.getProgress());
		}

		updateView();
	}

	public static final int DIALOG_LOADING = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.v(getClass().getCanonicalName(), "onCreateDialog");
		switch (id) {
		case DIALOG_LOADING:
			return ProgressDialog.show(QuestionActivity.this, "",
					getResources().getString(R.string.loading_meeting), true);
		default:
			return super.onCreateDialog(id);
		}

	}

	/**
	 * Kümmert sich um die Fragen-Auswahl bei Single- oder
	 * Multiple-Choice-Fragen
	 * 
	 * @author Coralie Reuter
	 */
	private final class AnswerChoiceSelectListener implements
			View.OnClickListener {
		private final Question q;
		private final CharSequence[] questionOptionNames;

		private AnswerChoiceSelectListener(Question q,
				CharSequence[] questionOptionNames) {
			this.q = q;
			this.questionOptionNames = questionOptionNames;
		}

		@Override
		public void onClick(View v) {
			Builder ad = new AlertDialog.Builder(QuestionActivity.this);
			ad.setIcon(R.drawable.alert_dialog_icon);
			ad.setTitle(q.getName());

			if (q.getMaxChoices().equals(1)) {
				ad.setSingleChoiceItems(questionOptionNames, 0,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
			} else {
				ad.setMultiChoiceItems(questionOptionNames, new boolean[] {
						false, true, false, true, false, false, false },
						new DialogInterface.OnMultiChoiceClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton, boolean isChecked) {

								/*
								 * User clicked on a check box do some stuff
								 */
							}
						});
			}
			ad.setPositiveButton(R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							/* User clicked Yes so do some stuff */
						}
					});
			ad.setNegativeButton(R.string.alert_dialog_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							/* User clicked No so do some stuff */
						}
					});
			ad.create();
			ad.show();

		}
	}

	/**
	 * Kümmert sich um Änderungen an Vote-Seekbars
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private final class QuestionSeekBarListener implements
			OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Question q = seekbarToQuestion.get(seekBar);
			// Aktuellen Wert anzeigen
			TextView currVal = seekbarToCurrentValueTextView.get(seekBar);
			currVal.setText("" + q.getValueAt(progress / 100.0));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// Frage
			Question q = seekbarToQuestion.get(seekBar);
			// Rating berechnen
			Integer value = q.getValueAt(seekBar.getProgress() / 100.0);
			// Speichern
			seekBarState.put(q.getId(), seekBar.getProgress());
			// Vote absetzen
			createAnswer(q, String.valueOf(value));
			Toast.makeText(
					QuestionActivity.this,
					String.format(
							getResources()
									.getString(R.string.question_answered), ""
									+ value), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Kümmert sich um das Wechseln der Fragen durch eine Swipe-Geste
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private final class QuestionSwipeListener implements OnPageChangedListener {
		@Override
		public void onPageChanged(int oldPage, int newPage) {
			questionActionViews.get(
					getCurrentTopic().getQuestions().get(oldPage))
					.setVisibility(View.GONE);
			currentQuestion = getCurrentTopic().getQuestions().get(newPage);
			questionActionViews.get(currentQuestion)
					.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Kümmert sich um Klicks in der Gallery
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class GalleryItemClickListener implements OnItemClickListener,
			OnItemLongClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			setCurrentTopic(topicGalleryAdapter.getItem(position));
			updateTopic();
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(topicGalleryAdapter.getItem(
					position).getImageFile()), "image/*");
			startActivity(intent);
			return true;
		}
	}
}