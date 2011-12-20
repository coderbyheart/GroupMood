package de.hsrm.mi.mobcomp.y2k11grp04.extra;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class ColoringTextWatcher implements TextWatcher {

	private TextView textView;
	private TextColoring textColoring = new TextColoring();

	public ColoringTextWatcher(TextView textView) {
		this.textView = textView;
		textView.addTextChangedListener(this);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		this.textView.setTextColor(textColoring
				.getColorForPercent(integerFromPercent(s)));
	}

	private int integerFromPercent(CharSequence text) {
		String intPart = "";
		for (int i = 0; i < text.length(); i++) {
			try {
				Integer.parseInt("" + text.charAt(i));
				intPart += text.charAt(i);
			} catch (NumberFormatException e) {
			}
		}
		return Integer.parseInt(intPart);
	}
}