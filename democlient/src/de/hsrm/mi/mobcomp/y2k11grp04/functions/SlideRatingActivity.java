/**
 * 
 */
package de.hsrm.mi.mobcomp.y2k11grp04.functions;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.ServiceActivity.ServiceMessageRunnable;
import de.hsrm.mi.mobcomp.y2k11grp04.extra.ColoringTextWatcher;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;

/**
 * @author Coralie Reuter
 * 
 */
public class SlideRatingActivity extends Activity implements ViewFactory {
	private final ArrayList<Integer> slideIDs = new ArrayList<Integer>();
	private final Meeting meeting = new Meeting(1, "Demo-Meeting");

	private ImageSwitcher iswitch;
	private TextView percentTextView;
	private final int defaultVote = 50;
	private SeekBar seekBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		percentTextView = (TextView) findViewById(R.id.percentTextView);
		new ColoringTextWatcher(percentTextView);
		percentTextView.setText("50 %");

		seekBar = (SeekBar) findViewById(R.id.slide_seekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(getClass().getCanonicalName(), "Neue Mood:" + seekBar.getProgress());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				percentTextView.setText("" + progress + "%");
			}
		});

		new ColoringTextWatcher(percentTextView);

		seekBar.setProgress(defaultVote);
		percentTextView.setText(defaultVote + " %");

		getSlides();

		iswitch = (ImageSwitcher) findViewById(R.id.image_switcher);
		iswitch.setFactory(this);
		iswitch.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		iswitch.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

		for (int i = 0; i < slideIDs.size(); i++) {
			System.out.println(slideIDs.get(i));
		}
		Gallery gallery = (Gallery) findViewById(R.id.slide_gallery);
		gallery.setAdapter(new ImageAdapter(this));

		// Erste Folie anzeigen
		iswitch.setImageResource(slideIDs.get(0));
		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				iswitch.setImageResource(slideIDs.get(arg2));
			}

		});
	}

	/**
	 * 
	 */
	private void getSlides() {

		slideIDs.add(R.drawable.v1_f1);
		slideIDs.add(R.drawable.v1_f2);
		slideIDs.add(R.drawable.v1_f3);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hsrm.mi.mobcomp.y2k11grp04.ServiceActivity#getServiceMessageRunnable
	 * (android.os.Message)
	 */
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View makeView() {
		ImageView imageView = new ImageView(this);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		imageView.setBackgroundColor(0xFF000000);
		return imageView;
	}

	/**
	 * @author Coralie Reuter
	 * 
	 */
	public class ImageAdapter extends BaseAdapter {

		private final Context context;
		private final int galleryItemBackground;

		/**
		 * @param _context
		 */
		public ImageAdapter(Context _context) {
			context = _context;
			TypedArray ta = obtainStyledAttributes(R.styleable.Gallery);
			galleryItemBackground = ta.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);
			ta.recycle();

		}

		@Override
		public int getCount() {
			return slideIDs.size();
		}

		@Override
		public Object getItem(int position) {
			return slideIDs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
			imageView.setImageResource(slideIDs.get(position));
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(150, 150));
			imageView.setBackgroundResource(galleryItemBackground);
			return imageView;

		}

	}
}
