package de.hsrm.mi.mobcomp.y2k11grp04.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;

public class FotoVoteCreateDialog extends FotoVoteDialog {

	public EditText serverName;
	public AutoCompleteTextView meetingName;
	private CreateDialogeValidator cdv;
	public Button meetingCreateButton;

	public FotoVoteCreateDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(context.getResources().getString(R.string.fotovote_create));
		setContentView(R.layout.fotovote_dialog);

		cdv = new CreateDialogeValidator();

		serverName = (EditText) findViewById(R.id.groupMood_fotovote_create_server);
		serverName.addTextChangedListener(cdv.textValidator);

		meetingName = (AutoCompleteTextView) findViewById(R.id.groupMood_fotovote_create_name);
		meetingName.addTextChangedListener(cdv.textValidator);

		photo = (ImageView) findViewById(R.id.groupMood_fotovote_create_photo_preview);

		captureButton = (Button) findViewById(R.id.groupMood_fotovote_create_photo_capture_button);

		galleryButton = (Button) findViewById(R.id.groupMood_fotovote_create_photo_select_button);

		meetingCreateButton = (Button) findViewById(R.id.groupMood_fotovote_create_button);

		cdv.validate();
	}

	public void validate() {
		cdv.validate();
	}

	private class CreateDialogeValidator {

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
			meetingCreateButton.setEnabled(meetingName != null
					&& meetingName.length() > 0
					&& serverName != null
					&& serverName.length() > 0
					&& URLUtil.isValidUrl(serverName.getEditableText()
							.toString()) && imageFile != null);
		}
	}

}
