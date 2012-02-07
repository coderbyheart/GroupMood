package de.hsrm.mi.mobcomp.y2k11grp04.view;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;

public class QuestionView {

	public static LinearLayout create(LayoutInflater layoutInflater, Resources res, Question q, Integer number) {
		// LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// getResources()
		LinearLayout view = (LinearLayout) layoutInflater.inflate(
				R.layout.question_name, null);
		((TextView) view.findViewById(R.id.groupMood_question_text)).setText(q
				.getName());
		((TextView) view.findViewById(R.id.groupMood_question_number))
				.setText("" + number);
		((TextView) view.findViewById(R.id.groupMood_question_total))
				.setText(String.format(
						res.getString(R.string.question_total), q
								.getTopic().getQuestions().size()));
		return view;
	}
}
