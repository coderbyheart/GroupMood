/**
 * 
 */
package de.hsrm.mi.mobcomp.y2k11grp04.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.ServiceActivity;
import de.hsrm.mi.mobcomp.y2k11grp04.extra.ColoringTextWatcher;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Slide;

/**
 * @author Coralie Reuter
 * 
 */
public class SlideRatingActivity extends ServiceActivity {
	private final String TAG = SlideRatingActivity.class.getSimpleName();
	private final ArrayList<Slide> slides = new ArrayList<Slide>();
	private Meeting meeting;

	private WebView webView;
	private TextView percentTextView;
	private final int defaultVote = 50;
	private SeekBar seekBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		meeting = new Meeting(1, "Demo-Meeting");
		meeting.setSlides(slides);

		percentTextView = (TextView) findViewById(R.id.percentTextView);
		new ColoringTextWatcher(percentTextView);
		percentTextView.setText("50 %");

		seekBar = (SeekBar) findViewById(R.id.slide_seekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(getClass().getCanonicalName(),
						"Neue Mood:" + seekBar.getProgress());

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				percentTextView.setText("" + progress + "%");
			}
		});

		new ColoringTextWatcher(percentTextView);

		seekBar.setProgress(defaultVote);
		percentTextView.setText(defaultVote + " %");

		try {
			getSlides();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gallery gallery = (Gallery) findViewById(R.id.slide_gallery);
		gallery.setAdapter(new ImageAdapter(this));

		webView = (WebView) findViewById(R.id.image_switcher);
		WebSettings webSettings = webView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		webSettings.setSupportZoom(true);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLightTouchEnabled(true);

		String webViewWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth() + "px";

		String webViewHeight = webView.getHeight() + "px";

		final String htmlBeginning = "<HTML><HEAD><style type=\"text/css\">html, body {height: 100%;margin: 0;padding: 0;}</style></HEAD><BODY><img src=\"";
		final String htmlEnd = "\"width=\"" + webViewWidth
				+ "\"></BODY></HTML>";
		String summary = htmlBeginning + slides.get(0).getImageUrl() + htmlEnd;
		Log.d(TAG, summary);
		webView.loadData(summary, "text/html", "utf-8");

		// Erste Folie anzeigen
		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int id,
					long arg3) {
				String summary = htmlBeginning + slides.get(id).getImageUrl()
						+ htmlEnd;
				webView.loadData(summary, "text/html", "utf-8");
			}

		});
	}

	/**
	 * @throws MalformedURLException
	 * 
	 */
	private void getSlides() throws MalformedURLException {

		slides.add(new Slide().setImageUrl(
				new URL("http://dummyimage.com/480x800/000/fff.jpg"))
				.setNumber(1));
		slides.add(new Slide().setImageUrl(
				new URL("http://dummyimage.com/480x800/ffffff/fff.jpg"))
				.setNumber(1));
		slides.add(new Slide().setImageUrl(
				new URL("http://dummyimage.com/480x800/540054/fff.jpg"))
				.setNumber(1));
		slides.add(new Slide().setImageUrl(
				new URL("http://dummyimage.com/480x800/943540/fff.jpg"))
				.setNumber(1));
		slides.add(new Slide().setImageUrl(
				new URL("http://dummyimage.com/480x800/234054/fff.jpg"))
				.setNumber(1));

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

	// @Override
	// public View makeView() {
	// ImageView imageView = new ImageView(this);
	// imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	// imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
	// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	// imageView.setBackgroundColor(0xFF000000);
	// return imageView;
	// }

	/**
	 * @author Coralie Reuter
	 * 
	 */
	public class ImageAdapter extends BaseAdapter {
		private final Context context;
		private final int galleryItemBackground;

		private Drawable ImageOperations(ImageAdapter imageAdapter, URL url) {
			try {
				InputStream is = (InputStream) url.getContent();
				Drawable d = Drawable.createFromStream(is, "src");
				return d;
			} catch (MalformedURLException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}

		/**
		 * @param _context
		 */
		public ImageAdapter(Context _context) {
			context = _context;
			TypedArray ta = obtainStyledAttributes(R.styleable.Gallery);
			galleryItemBackground = ta.getResourceId(
					R.styleable.Gallery_android_galleryItemBackground, 0);
			ta.recycle();

		}

		@Override
		public int getCount() {
			return slides.size();
		}

		@Override
		public Object getItem(int position) {
			return slides.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
			try {
				URL url = slides.get(position).getImageUrl();
				Drawable image = ImageOperations(this, url);
				imageView.setImageDrawable(image);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(150, 150));
			imageView.setBackgroundResource(galleryItemBackground);
			return imageView;

		}
	}
}
