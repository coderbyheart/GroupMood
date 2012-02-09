package de.hsrm.mi.mobcomp.y2k11grp04.view;

import java.util.List;

import uk.co.jasonfry.android.tools.ui.SwipeView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

/**
 * @author Markus Tacker
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

		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		LinearLayout topicResultLayout = (LinearLayout) layoutInflater.inflate(R.layout.topic_result, parent, false);
		// Icon erzeugen
		((ViewGroup) topicResultLayout.findViewById(R.id.groupMood_topicResult_topic)).addView(createTopicView(parent,
				topic));
		// Fragen zum Durchblättern erzeugen
		SwipeView mSwipeView = (SwipeView) topicResultLayout.findViewById(R.id.groupMood_topicResult_questionsSwipe);

		int num = 0;
		for (Question q : topic.getQuestions()) {
			View questionView = QuestionView.create(layoutInflater, parent.getResources(), q, ++num);
			// Anzeige des Ergebnisses einblenden
			questionView.findViewById(R.id.groupMood_question_resultLayout).setVisibility(View.VISIBLE);
			mSwipeView.addView(questionView);
		}
		return topicResultLayout;
	}
}