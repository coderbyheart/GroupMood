package de.hsrm.mi.mobcomp.y2k11grp04.gui;

import java.util.List;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.model.AnswerAverage;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

/**
 * Adapter für die ListView in der die Ergebnisse angezeigt werden.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class TopicResultAdapter extends TopicGalleryAdapter {

	/**
	 * @param topics
	 */
	public TopicResultAdapter(List<Topic> topics) {
		super(topics);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Topic topic = getItem(position);
		if (topic == null)
			return null;

		LayoutInflater layoutInflater = LayoutInflater
				.from(parent.getContext());
		LinearLayout topicResultLayout = (LinearLayout) layoutInflater.inflate(
				R.layout.topic_result, parent, false);

		Resources res = parent.getResources();
		// Icon erzeugen
		((ViewGroup) topicResultLayout
				.findViewById(R.id.groupMood_topicResult_topic))
				.addView(createTopicView(parent, topic));
		// Fragen zum Durchblättern erzeugen
		LinearLayout questionsLayout = (LinearLayout) topicResultLayout
				.findViewById(R.id.groupMood_topicResult_questions);
		for (Question q : topic.getQuestions()) {
			LinearLayout questionView = (LinearLayout) layoutInflater.inflate(
					R.layout.question_result, null);
			((TextView) questionView
					.findViewById(R.id.groupMood_questionResult_text))
					.setText(q.getName());

			ViewGroup answersLayout = (ViewGroup) questionView
					.findViewById(R.id.groupMood_questionResult_answers);

			if (q.getType().equals(Question.TYPE_RANGE)) {
				float progress = Integer.valueOf(q.getAvg() - q.getMinOption())
						.floatValue()
						/ Integer.valueOf(q.getMaxOption() - q.getMinOption())
								.floatValue();
				View rangeAnswerView = createAnswerView(layoutInflater, res,
						String.format(res.getString(R.string.label_result_avg),
								q.getAvg()), (int) (100 * progress),
						q.getNumAnswers());
				// Labels
				TextView minValueLabel = (TextView) rangeAnswerView
						.findViewById(R.id.groupMood_question_resultMinLabel);
				TextView midValueLabel = (TextView) rangeAnswerView
						.findViewById(R.id.groupMood_question_resultMidLabel);
				TextView maxValueLabel = (TextView) rangeAnswerView
						.findViewById(R.id.groupMood_question_resultMaxLabel);
				minValueLabel.setText(q.getOption(
						QuestionOption.OPTION_RANGE_LABEL_MIN_VALUE,
						"" + q.getMinOption()));
				midValueLabel.setText(q.getOption(
						QuestionOption.OPTION_RANGE_LABEL_MID_VALUE,
						"" + q.getValueAt(0.5)));
				maxValueLabel.setText(q.getOption(
						QuestionOption.OPTION_RANGE_LABEL_MAX_VALUE,
						"" + q.getMaxOption()));

				answersLayout.addView(rangeAnswerView);
			} else {
				for (AnswerAverage aa : q.getAverageAnswers()) {
					View choiceAnswerView = createAnswerView(layoutInflater,
							res, aa.getAnswer(), aa.getAverage(),
							aa.getNumVotes());
					choiceAnswerView.findViewById(
							R.id.groupMood_question_resultLabel_layout)
							.setVisibility(View.GONE);
					answersLayout.addView(choiceAnswerView);
				}
			}

			questionsLayout.addView(questionView);
		}

		if (topic.getQuestions().size() == 0) {
			topicResultLayout.setVisibility(View.GONE);
			topicResultLayout.removeAllViews();
		}

		return topicResultLayout;
	}

	public View createAnswerView(LayoutInflater layoutInflater, Resources res,
			String answer, int progress, int numVotes) {
		LinearLayout answerView = (LinearLayout) layoutInflater.inflate(
				R.layout.question_result_answer, null);

		ProgressBar p = (ProgressBar) answerView
				.findViewById(R.id.groupMood_question_resultProgressbar);
		p.setProgress(progress);

		((TextView) answerView
				.findViewById(R.id.groupMood_question_resultLabel))
				.setText(answer);

		((TextView) answerView
				.findViewById(R.id.groupMood_question_resultNumVotes))
				.setText(res.getQuantityString(R.plurals.label_result_numvotes,
						numVotes, numVotes));

		return answerView;
	}
}