package de.hsrm.mi.mobcomp.y2k11grp04.gui;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;

public abstract class FotoVoteDialog extends Dialog {

	public ImageView photo;
	public Button captureButton;
	public Button galleryButton;
	public File imageFile;

	public FotoVoteDialog(Context context) {
		super(context);
	}

	public abstract void validate();
}
