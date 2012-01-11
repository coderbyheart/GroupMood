//package de.hsrm.mi.mobcomp.y2k11grp04.functions;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//
//import uk.co.jasonfry.android.tools.ui.PageControl;
//import uk.co.jasonfry.android.tools.ui.SwipeView;
//import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.widget.BaseAdapter;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//
//import com.devsmart.android.ui.HorizontalListView;
//
//import de.hsrm.mi.mobcomp.y2k11grp04.R;
//import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
//
///**
// * @author Coralie Reuter
// * 
// */
//public class SlideRatingActivity extends Activity {
//	private static final int DEFAULT_PROGRESS = 50;
//	private int currentImage = 0;
//	private SeekBar s1;
//	private SeekBar s2;
//	private TextView tv;
//	private WebView image_tall;
//	private SwipeView mSwipeView;
//	private final ArrayList<Topic> images = new ArrayList<Topic>();
//	private int currentSeekBar = 0;
//	private static String TAG = SlideRatingActivity.class.getSimpleName();
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.gallery);
//
//		loadImages();
//
//		View image_previews = null;
//
//		if (getResources().getConfiguration().orientation == 1) {
//			image_previews = findViewById(R.id.horizontal_list_view);
//			((HorizontalListView) image_previews).setAdapter(new ImageAdapter(this));
//
//		} else {
//
//			tv = (TextView) findViewById(R.id.percentage);
//			tv.setText("" + DEFAULT_PROGRESS);
//			image_previews = findViewById(R.id.vertical_list_view);
//			((ListView) image_previews).setAdapter(new ImageAdapter(this));
//
//			PageControl mPageControl = (PageControl) findViewById(R.id.page_control);
//			mSwipeView = (SwipeView) findViewById(R.id.swipe_view);
//
//			for (int i = 0; i < images.get(currentImage).getNumberOfSeekbars(); i++) {
//				mSwipeView.addView(new FrameLayout(this));
//			}
//
//			s1 = new SeekBar(this);
//			s2 = new SeekBar(this);
//			s1.setProgress(images.get(currentImage).getSeekBars().get(0).getProgress());
//			s2.setProgress(images.get(currentImage).getSeekBars().get(1).getProgress());
//
//			((FrameLayout) mSwipeView.getChildContainer().getChildAt(0)).addView(s1);
//			((FrameLayout) mSwipeView.getChildContainer().getChildAt(1)).addView(s2);
//
//			SwipeImageLoader mSwipeImageLoader = new SwipeImageLoader();
//
//			mSwipeView.setOnPageChangedListener(mSwipeImageLoader);
//
//			mSwipeView.setPageControl(mPageControl);
//		}
//		// tall image
//		image_tall = (WebView) findViewById(R.id.image_tall);
//		WebSettings webImageSettings = image_tall.getSettings();
//		webImageSettings.setBuiltInZoomControls(true);
//		webImageSettings.setSupportZoom(true);
//		webImageSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
//		webImageSettings.setJavaScriptEnabled(true);
//		webImageSettings.setLightTouchEnabled(true);
//		webImageSettings.setLoadWithOverviewMode(true);
//		webImageSettings.setUseWideViewPort(true);
//		image_tall.setPadding(0, 0, 0, 0);
//
//		loadTallImage(image_tall, images.get(0).getUrl());
//
//	}
//
//	private class SwipeImageLoader implements OnPageChangedListener {
//
//		@Override
//		public void onPageChanged(int oldPage, int newPage) {
//
//			// going forwards
//			if (newPage > oldPage) {
//				// if at the end, don't load one page after the end
//				if (newPage != (mSwipeView.getPageCount() - 1)) {
//					SeekBar s = new SeekBar(SlideRatingActivity.this);
//					s.setProgress(images.get(currentImage).getSeekBars().get(newPage + 1).getProgress());
//					((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage + 1)).addView(s);
//					currentSeekBar = newPage + 1;
//
//				}
//				// if at the beginning, don't destroy one before the beginning
//				if (oldPage != 0) {
//					((FrameLayout) mSwipeView.getChildContainer().getChildAt(oldPage - 1)).removeAllViews();
//					currentSeekBar = newPage - 1;
//
//				}
//
//			}
//			// going backwards
//			else {
//				// if at the beginning, don't load one before the beginning
//				if (newPage != 0) {
//					SeekBar s = new SeekBar(SlideRatingActivity.this);
//					s.setProgress(images.get(currentImage).getSeekBars().get(newPage - 1).getProgress());
//					((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage - 1)).addView(s);
//					currentSeekBar = newPage - 1;
//				}
//				// if at the end, don't destroy one page after the end
//				if (oldPage != (mSwipeView.getPageCount() - 1)) {
//					((FrameLayout) mSwipeView.getChildContainer().getChildAt(oldPage + 1)).removeAllViews();
//					currentSeekBar = newPage + 1;
//
//				}
//			}
//
//		}
//	}
//
//	private void loadTallImage(WebView wv, URL imageURL) {
//
//		String htmlTemplateHead = "<HTML><HEAD><meta name=\"viewport\" content=\"width=device-width\"><style type=\"text/css\">html, body {height: 100%;margin: 0;padding:0; background-color: #000000;}</style></HEAD>";
//		String htmlTemplateBody = "<BODY> <div align=\"center\" ><img src=\"" + imageURL + "\"></div></BODY>";
//		String htmlTemplateFoot = "</HTML>";
//
//		String summary = htmlTemplateHead + htmlTemplateBody + htmlTemplateFoot;
//		Log.d(TAG, summary);
//
//		// wv.setInitialScale(getScale());
//		wv.loadData(summary, "text/html", "utf-8");
//	}
//
//	/**
//	 * @author Coralie Reuter
//	 * 
//	 */
//	public class ImageAdapter extends BaseAdapter {
//		@SuppressWarnings("unused")
//		private final Context context;
//		private final int galleryItemBackground;
//
//		private Drawable ImageOperations(ImageAdapter imageAdapter, URL url) {
//			try {
//				InputStream is = (InputStream) url.getContent();
//				Drawable d = Drawable.createFromStream(is, "src");
//				Log.d(TAG, "Drawable");
//				return d;
//			} catch (MalformedURLException e) {
//				return null;
//			} catch (IOException e) {
//				return null;
//			}
//		}
//
//		/**
//		 * @param _context
//		 */
//		public ImageAdapter(Context _context) {
//			context = _context;
//			TypedArray ta = obtainStyledAttributes(R.styleable.Gallery);
//			galleryItemBackground = ta.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);
//			ta.recycle();
//
//		}
//
//		@Override
//		public int getCount() {
//			return images.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return images.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(final int position, View convertView, ViewGroup parent) {
//			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, null);
//
//			ImageView iv = (ImageView) view.findViewById(R.id.image);
//			try {
//				URL url = images.get(position).getUrl();
//				Drawable img = ImageOperations(this, url);
//				iv.setImageDrawable(img);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//			iv.setPadding(2, 2, 2, 2);
//			iv.setScaleType(ImageView.ScaleType.FIT_XY);
//			iv.setBackgroundResource(galleryItemBackground);
//
//			iv.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					loadTallImage(image_tall, images.get(position).getUrl());
//					setCurrentImage(position);
//				}
//
//			});
//
//			return view;
//
//		}
//	}
//
//	private void loadImages() {
//		Log.d(TAG, "load images");
//		SeekBar tempBar = new SeekBar(this);
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//		layoutParams.weight = 1;
//		layoutParams.leftMargin = 5;
//		layoutParams.rightMargin = 5;
//		tempBar.setLayoutParams(layoutParams);
//		tempBar.setProgress(DEFAULT_PROGRESS);
//		tempBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				tv.setText(seekBar.getProgress() + "");
//				Log.d(TAG, "onStopTrackingTouch");
//
//			}
//
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				Log.d(TAG, "onStartTrackingTouch");
//
//			}
//
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//				Log.d(TAG, "onProgressChanged");
//
//			}
//		});
//		try {
//			images.add(new Topic().setUrl(
//					new URL("http://images.idgentertainment.de/images/idgwpgsgp/bdb/2198613/1920x1200.jpg"))
//					.setSeekBars(tempBar, this));
//			images.add(new Topic().setUrl(new URL("http://dummyimage.com/480x800/000/fff.jpg")).setSeekBars(
//					tempBar, this));
//			images.add(new Topic().setUrl(new URL("http://dummyimage.com/480x800/ffffff/fff.jpg")).setSeekBars(
//					tempBar, this));
//			images.add(new Topic().setUrl(new URL("http://dummyimage.com/480x800/540054/fff.jpg")).setSeekBars(
//					tempBar, this));
//			images.add(new Topic().setUrl(new URL("http://dummyimage.com/480x800/943540/fff.jpg")).setSeekBars(
//					tempBar, this));
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * @return the currentImage
//	 */
//	public int getCurrentImage() {
//		return currentImage;
//	}
//
//	/**
//	 * @param currentImage
//	 */
//	public void setCurrentImage(int currentImage) {
//		this.currentImage = currentImage;
//	}
//}