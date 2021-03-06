package de.hsrm.mi.mobcomp.y2k11grp04.gui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;

/**
 * Hält Referenzen auf die Elemente des Dialoges um ein Topic zu einem
 * FotoVote-Meeting an zu legen und kümmert sich um das Aktivieren bzw.
 * Deaktivieren des Buttons.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class FotoVoteTopicCreateDialog extends FotoVoteDialog {

	TopicCreateDialogeValidator cdv;
	public Button topicCreateButton;

	public FotoVoteTopicCreateDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(context.getResources()
				.getString(R.string.fotovote_createtopic));
		setContentView(R.layout.fotovote_topic_dialog);

		cdv = new TopicCreateDialogeValidator();

		photo = (ImageView) findViewById(R.id.groupMood_fotovote_createtopic_photo_preview);

		captureButton = (Button) findViewById(R.id.groupMood_fotovote_createtopic_photo_capture_button);

		galleryButton = (Button) findViewById(R.id.groupMood_fotovote_createtopic_photo_select_button);

		topicCreateButton = (Button) findViewById(R.id.groupMood_fotovote_createtopic_button);

		cdv.validate();
	}

	public void validate() {
		cdv.validate();
	}

	class TopicCreateDialogeValidator {
		TextWatcher textValidator = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				validate();
			}
		};

		public void validate() {
			topicCreateButton.setEnabled(imageFile != null);
		}
	}

}
