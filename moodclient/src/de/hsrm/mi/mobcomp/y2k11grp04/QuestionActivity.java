package de.hsrm.mi.mobcomp.y2k11grp04;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.jasonfry.android.tools.ui.SwipeView;
import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.devsmart.android.ui.HorizontalListView;

import de.hsrm.mi.mobcomp.y2k11grp04.model.Comment;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
import de.hsrm.mi.mobcomp.y2k11grp04.service.MoodServerService;
import de.hsrm.mi.mobcomp.y2k11grp04.view.ListViewHelper;
import de.hsrm.mi.mobcomp.y2k11grp04.view.QuestionView;
import de.hsrm.mi.mobcomp.y2k11grp04.view.SeekBarState;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicGalleryAdapter;
import de.hsrm.mi.mobcomp.y2k11grp04.view.TopicResultAdapter;

public class QuestionActivity extends ServiceActivity {

	public static final int DIALOG_LOADING = 1;

	protected ProgressBar loadingProgress;
	protected Meeting meeting;
	protected boolean meetingComplete = false;
	private View topicGallery;
	private Topic currentTopic;
	private Question currentQuestion;
	private TopicGalleryAdapter topicGalleryAdapter;
	private Map<Topic, View> topicViews = new HashMap<Topic, View>();
	private Map<Question, LinearLayout> questionActionViews = new HashMap<Question, LinearLayout>();
	private Map<SeekBar, Question> seekbarToQuestion = new HashMap<SeekBar, Question>();
	private Map<SeekBar, TextView> seekbarToCurrentValueTextView = new HashMap<SeekBar, TextView>();
	private Map<Integer, Integer> seekBarState = new HashMap<Integer, Integer>();
	private Map<Button, ListView> questionState = new HashMap<Button, ListView>();
	private Map<Button, Question> buttonToQuestion = new HashMap<Button, Question>();
	private Map<ListView, Button> choicesToButton = new HashMap<ListView, Button>();

	private Button questionsButton;
	private Button commentsButton;
	private Button resultsButton;

	private Button actionBarActiveButton;

	// Kümmert sich um das Setzen der Antwort, falls diese einen
	// Slider verwendet
	private OnSeekBarChangeListener questionActionSeekBarListener = new QuestionSeekBarListener();

	// Kümmert sich um das Anlegen von neuen Kommentaren
	private NewCommentClickListener nccl = new NewCommentClickListener();

	// Kümmert sich um Klicks auf den Button für Auswahl-Fragen
	private ChoiceButtonClickListener cbcl = new ChoiceButtonClickListener();

	// Kümmert sich um das Wechseln der Fragen durch eine Swipe-Geste
	private OnPageChangedListener swipeChangeListener = new QuestionSwipeListener();

	// Kümmert sich darum, den Button zum Abgeben der Antwort zu aktivieren bzw.
	// zu deaktivieren je nach dem wieviele Choices ausgewählt wurden.
	private ChoiceSelectListener csl = new ChoiceSelectListener();

	// Stellt Hilfsfunktionen für ListViews zur Verfügung
	private ListViewHelper listViewHelper = new ListViewHelper();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		initActionBar();

		loadingProgress = (ProgressBar) findViewById(R.id.groupMood_progressBar);

		Bundle b = getIntent().getExtras();
		b.setClassLoader(getClassLoader());
		meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);

		updateView();
	}

	/**
	 * Initialisiert die ActionBar
	 */
	private void initActionBar() {
		((Button) findViewById(R.id.groupMood_actionbar_logo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getApplicationContext(),
								LaunchActivity.class));
					}
				});
		ActionBarClickListener abcl = new ActionBarClickListener();
		questionsButton = (Button) findViewById(R.id.groupMood_actionbar_button_questions);
		commentsButton = (Button) findViewById(R.id.groupMood_actionbar_button_comments);
		resultsButton = (Button) findViewById(R.id.groupMood_actionbar_button_results);
		questionsButton.setOnClickListener(abcl);
		commentsButton.setOnClickListener(abcl);
		resultsButton.setOnClickListener(abcl);
		actionBarActiveButton = questionsButton;
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

		// Ergebnisse ausblenden
		TopicResultAdapter topicResultAdapter = new TopicResultAdapter(
				meeting.getTopics());
		ListView resultView = (ListView) findViewById(R.id.groupMood_topicResult);
		resultView.setVisibility(View.GONE);
		resultView.setAdapter(topicResultAdapter);
		resultView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					// TODO: Fire SwipeView Action.UP(event)
					Log.d("QuestionActivity",
							"OnScrollListener ScrollStateTouchScroll");
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.d("QuestionActivity", "OnScrollListener ONSCROLL");

			}
		});
		updateTopic();
	}

	protected int getLayout() {
		return R.layout.question;
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
					View questionView = QuestionView.create(layoutInflater,
							getResources(), q, ++num);
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

				// Comments ausblenden
				topicQuestionsLayout.findViewById(R.id.groupMood_topicComments)
						.setVisibility(View.GONE);
				((Button) topicQuestionsLayout
						.findViewById(R.id.groupMood_newcomment_button))
						.setOnClickListener(nccl);

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

	private LinearLayout createQuestionAction(Question q) {
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
		} else { // Choice-Frage
			// Layout für Range wird nicht gebraucht
			view.removeView(view
					.findViewById(R.id.groupMood_questionActionRangeLayout));

			// List-View für Choices anlegen
			ListView lv = new ListView(this);
			lv.setItemsCanFocus(false);
			ArrayList<String> questionOptionNames = new ArrayList<String>();
			for (int i = 0; i < q.getChoices().size(); i++) {
				questionOptionNames.add(q.getChoices().get(i).getName());
			}

			// Question-Type Single-Choice
			if (q.getMaxChoices().equals(1)) {
				lv.setAdapter(new ArrayAdapter<String>(QuestionActivity.this,
						android.R.layout.simple_list_item_single_choice,
						questionOptionNames));
				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			}
			// Question-Type Multiple-Choice
			else {
				lv.setAdapter(new ArrayAdapter<String>(QuestionActivity.this,
						android.R.layout.simple_list_item_multiple_choice,
						questionOptionNames));
				lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			}
			view.addView(lv);
			lv.setOnItemSelectedListener(csl);
			lv.setOnItemClickListener(csl);

			// Button dazu
			Button b = (Button) view
					.findViewById(R.id.groupMood_questionActionButton);
			b.setOnClickListener(cbcl);

			// ListView zum Button merken
			questionState.put(b, lv);
			choicesToButton.put(lv, b);

			// Button zur Frage merken
			buttonToQuestion.put(b, q);
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
			return new MeetingProgressHandler(message);
		case MoodServerService.MSG_MEETING_COMPLETE_RESULT:
			return new MeetingCompleteHandler(message);
		case MoodServerService.MSG_TOPIC_IMAGE_RESULT:
			return new TopicImageHandler(message);
		case MoodServerService.MSG_TOPIC_COMMENTS_RESULT:
			return new TopicCommentsHandler(message);
		default:
			return super.getServiceMessageRunnable(message);
		}
	}

	@Override
	protected void onConnect() {
		Log.v(getClass().getCanonicalName(), "onConnect");
		super.onConnect();
		// Meeting vollständig laden
		if (!meetingComplete) {
			showDialog(DIALOG_LOADING);
			loadMeetingComplete(meeting);
		} else {
			loadComments();
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

		// Aktive view merken
		outState.putInt("actionBarActiveButton", actionBarActiveButton.getId());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
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

		// Aktive view laden
		setActionBarActiveButton((Button) findViewById(savedInstanceState
				.getInt("actionBarActiveButton")));
	}

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
	 * Kümmert sich darum, den Button zum Abgeben der Antwort zu aktivieren bzw.
	 * zu deaktivieren je nach dem wieviele Choices ausgewählt wurden.
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class ChoiceSelectListener implements OnItemSelectedListener,
			OnItemClickListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Button b = choicesToButton.get(parent);
			Question q = buttonToQuestion.get(b);
			ListView lv = (ListView) parent;
			if (lv.getChoiceMode() == ListView.CHOICE_MODE_SINGLE) {
				b.setEnabled(true);
			} else {
				ArrayList<String> selectedValues = listViewHelper
						.getSelectedItems(lv);
				b.setEnabled(selectedValues.size() >= q.getMinChoices()
						&& selectedValues.size() <= q.getMaxChoices());
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			choicesToButton.get(parent).setEnabled(false);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			onItemSelected(parent, view, position, id);
		}
	}

	/**
	 * Kümmert sich um Klicks auf den Button für Auswahl-Fragen.
	 * 
	 * @author Coralie Reuter <coralie.reuter@hrcom.de>
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class ChoiceButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Button b = (Button) v;
			ListView lv = questionState.get(b);
			Question q = buttonToQuestion.get(b);
			ArrayList<String> selectedValues = listViewHelper
					.getSelectedItems(lv);

			if (q.getMaxChoices().equals(1)) {
				createAnswer(q, selectedValues.get(0));
			} else {
				createAnswer(
						q,
						selectedValues.toArray(new String[selectedValues.size()]));
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn der Service die Kommentare zu einem Topic geladen
	 * hat
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class TopicCommentsHandler extends ServiceMessageRunnable {
		private TopicCommentsHandler(Message serviceMessage) {
			super(serviceMessage);
		}

		@Override
		public void run() {
			Bundle b = serviceMessage.getData();
			b.setClassLoader(getClassLoader());
			ArrayList<Comment> comments = b
					.getParcelableArrayList(MoodServerService.KEY_COMMENT_MODEL);
			// Finde topic
			Uri topicUri = Uri.parse(b
					.getString(MoodServerService.KEY_TOPIC_URI));
			Topic theTopic = null;

			for (Topic t : meeting.getTopics()) {
				if (t.getUri().equals(topicUri))
					theTopic = t;
			}
			if (theTopic == null) {
				Log.e(getClass().getCanonicalName(), "Topic for comments uri "
						+ topicUri.toString() + " not found.");
				return;
			}

			// Loading ausblenden
			topicViews.get(theTopic)
					.findViewById(R.id.groupMood_comments_loading)
					.setVisibility(View.GONE);

			// Kommentare rendern
			LinearLayout commentsList = (LinearLayout) topicViews.get(theTopic)
					.findViewById(R.id.groupMood_comments_list);
			LinearLayout noComments = (LinearLayout) topicViews.get(theTopic)
					.findViewById(R.id.groupMood_comments_nocomments);

			if (comments.size() > 0) {
				noComments.setVisibility(View.GONE);
				commentsList.setVisibility(View.VISIBLE);

				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				commentsList.removeAllViews();
				for (Comment comment : comments) {
					ViewGroup view = (ViewGroup) layoutInflater.inflate(
							R.layout.comment_item, commentsList, false);
					((TextView) view
							.findViewById(R.id.groupMood_comment_user_text))
							.setText(comment.getUser());
					((TextView) view
							.findViewById(R.id.groupMood_comment_time_text))
							.setText(DateUtils.getRelativeDateTimeString(
									getApplicationContext(), comment
											.getCreationDate().getTime(),
									DateUtils.MINUTE_IN_MILLIS,
									DateUtils.WEEK_IN_MILLIS, 0));
					((TextView) view.findViewById(R.id.groupMood_comment_text))
							.setText(comment.getComment());
					commentsList.addView(view);
				}
			} else {
				commentsList.setVisibility(View.GONE);
				noComments.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn der Service das Bild zu einem Topic geladen hat
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class TopicImageHandler extends ServiceMessageRunnable {
		private TopicImageHandler(Message serviceMessage) {
			super(serviceMessage);
		}

		@Override
		public void run() {
			// Und Bild aus Datei setzen
			Integer topicId = serviceMessage.getData().getInt(
					MoodServerService.KEY_TOPIC_ID);
			for (Topic t : meeting.getTopics()) {
				if (t.getId() == topicId) {
					t.setImageFile(new File(serviceMessage.getData().getString(
							MoodServerService.KEY_TOPIC_IMAGE)));
				}
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn das Meeting vollständig geladen ist
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class MeetingCompleteHandler extends ServiceMessageRunnable {
		private MeetingCompleteHandler(Message serviceMessage) {
			super(serviceMessage);
		}

		@Override
		public void run() {
			Bundle b = serviceMessage.getData();
			b.setClassLoader(getClassLoader());
			meeting = b.getParcelable(MoodServerService.KEY_MEETING_MODEL);
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
	}

	/**
	 * Wird aufgerufen, wenn der Server Fortschritt beim Laden des Meetings hat
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class MeetingProgressHandler extends ServiceMessageRunnable {
		private MeetingProgressHandler(Message serviceMessage) {
			super(serviceMessage);
		}

		@Override
		public void run() {
			loadingProgress.setMax(serviceMessage.arg2);
			loadingProgress.setProgress(serviceMessage.arg1);
			if (loadingProgress.getProgress() >= loadingProgress.getMax()) {
				loadingProgress.setVisibility(View.GONE);
			} else {
				loadingProgress.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Kümmert sich um das Erstellen von neuen Kommentaren
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class NewCommentClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			ViewGroup parent = (ViewGroup) v.getParent();
			EditText commentText = (EditText) parent
					.findViewById(R.id.groupMood_newcomment_text);
			addComment(commentText.getText().toString());
			commentText.setText("");
			// Keyboard ausblendend
			InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * Kümmert sich um die Clicks auf die Icons in der ActionBar
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class ActionBarClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			setActionBarActiveButton((Button) v);
		}
	}

	/**
	 * Setzt den aktuell aktiven Button in der ActionBar
	 * 
	 * @param b
	 */
	public void setActionBarActiveButton(Button b) {
		Resources res = getResources();
		actionBarActiveButton = b;
		// Die View des aktuellen Topics holen
		View topicView = topicViews.get(getCurrentTopic());

		topicView.findViewById(R.id.groupMood_topicComments).setVisibility(
				View.GONE);
		topicView.findViewById(R.id.groupMood_questionsSwipe).setVisibility(
				View.GONE);
		topicView.findViewById(R.id.groupMood_questionsActions).setVisibility(
				View.GONE);
		findViewById(R.id.groupMood_topicResult).setVisibility(View.GONE);
		findViewById(R.id.groupMood_topicFramesLayout).setVisibility(View.GONE);

		// Question-Icon
		if (actionBarActiveButton.equals(questionsButton)) {
			questionsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_checkmark_white_tab));
			topicView.findViewById(R.id.groupMood_questionsSwipe)
					.setVisibility(View.VISIBLE);
			topicView.findViewById(R.id.groupMood_questionsActions)
					.setVisibility(View.VISIBLE);
			findViewById(R.id.groupMood_topicFramesLayout).setVisibility(
					View.VISIBLE);
		} else {
			questionsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_tab_checkmark));
		}
		// Comments-Icon
		if (actionBarActiveButton.equals(commentsButton)) {
			commentsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_bubble_white_tab));
			topicView.findViewById(R.id.groupMood_topicComments).setVisibility(
					View.VISIBLE);
			findViewById(R.id.groupMood_topicFramesLayout).setVisibility(
					View.VISIBLE);
			if (isServiceBound())
				loadComments();
		} else {
			commentsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_tab_bubble));
		}
		// Results-Icon
		if (actionBarActiveButton.equals(resultsButton)) {
			resultsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_chart_white_tab));
			findViewById(R.id.groupMood_topicResult)
					.setVisibility(View.VISIBLE);
		} else {
			resultsButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.ic_tab_chart));
		}
	}

	/**
	 * Lädt / aktualisiert die Kommentare des aktuellen Topics
	 */
	private void loadComments() {
		topicViews.get(getCurrentTopic())
				.findViewById(R.id.groupMood_comments_loading)
				.setVisibility(View.VISIBLE);
		Message m = Message.obtain(null, MoodServerService.MSG_TOPIC_COMMENTS);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_TOPIC_URI, getCurrentTopic()
				.getUri().toString());
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Erzeugt ein neues Kommentar
	 */
	protected void addComment(String comment) {
		topicViews.get(getCurrentTopic())
				.findViewById(R.id.groupMood_comments_loading)
				.setVisibility(View.VISIBLE);
		Message m = Message.obtain(null, MoodServerService.MSG_TOPIC_COMMENT);
		Bundle data = new Bundle();
		data.putString(MoodServerService.KEY_TOPIC_URI, getCurrentTopic()
				.getUri().toString());
		data.putString(MoodServerService.KEY_COMMENT_COMMENT, comment);
		m.setData(data);
		sendMessage(m);
	}

	/**
	 * Kümmert sich um Änderungen an Vote-Seekbars
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class QuestionSeekBarListener implements OnSeekBarChangeListener {
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
		}
	}

	/**
	 * Kümmert sich um das Wechseln der Fragen durch eine Swipe-Geste
	 * 
	 * @author Markus Tacker <m@coderbyheart.de>
	 */
	private class QuestionSwipeListener implements OnPageChangedListener {
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