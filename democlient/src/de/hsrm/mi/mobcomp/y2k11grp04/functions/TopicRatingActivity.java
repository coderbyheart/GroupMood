package de.hsrm.mi.mobcomp.y2k11grp04.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.devsmart.android.ui.HorizontalListView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.ServiceActivity;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Choice;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class TopicRatingActivity extends ServiceActivity {
	private static final String DEFAULT_PROGRESS = "50";
	private static int currentTopic = 0;
	private static int currentQuestion = 0;

	private int SCREEN_ORIENTATION_PORTRAIT = 1;
	private String TAG = TopicRatingActivity.class.getSimpleName();

	private Meeting m;

	private ArrayList<Topic> topics;
	private View image_gallery;
	private WebView wv_image;
	private TextView questionDescription;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		// TODO: MEETING
		m = new Meeting(1, "MEETING");
		/* LOAD DUMMY DATA */
		try {
			addDummyTopics();
			addDummyQuestions();
			addDummyQuestionsOptions();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: TOPICS
		topics = new ArrayList<Topic>(m.getTopics());

		if (getResources().getConfiguration().orientation == SCREEN_ORIENTATION_PORTRAIT)
			portrait();
		else
			landscape();

		wv_image = (WebView) findViewById(R.id.image_tall);

		WebSettings webImageSettings = wv_image.getSettings();
		webImageSettings.setBuiltInZoomControls(true);
		webImageSettings.setSupportZoom(true);
		webImageSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webImageSettings.setJavaScriptEnabled(true);
		webImageSettings.setLightTouchEnabled(true);
		webImageSettings.setLoadWithOverviewMode(true);
		webImageSettings.setUseWideViewPort(true);
		wv_image.setPadding(0, 0, 0, 0);

		load_wv_image(m.getTopics().get(0).getImage());
	}

	private void landscape() {
		questionDescription = (TextView) findViewById(R.id.percentage);
		questionDescription.setText(m.getTopics().get(currentTopic)
				.getQuestions().get(currentQuestion).getName());
		image_gallery = findViewById(R.id.vertical_list_view);
		((ListView) image_gallery).setAdapter(new ImageAdapter(this));
	}

	private void portrait() {
		image_gallery = findViewById(R.id.horizontal_list_view);
		((HorizontalListView) image_gallery).setAdapter(new ImageAdapter(this));
	}

	private void load_wv_image(URL imageURL) {
		String htmlTemplateHead = "<HTML><HEAD><meta name=\"viewport\" content=\"width=device-width\"><style type=\"text/css\">html, body {height: 100%; width:100%; margin: 0;padding:0; background-color: #000000; position: relative;}</style></HEAD>";
		String htmlTemplateBody = "<BODY> <div align=\"center\" ><img src=\""
				+ imageURL + "\"></div></BODY>";
		String htmlTemplateFoot = "</HTML>";

		String summary = htmlTemplateHead + htmlTemplateBody + htmlTemplateFoot;

		wv_image.loadData(summary, "text/html", "utf-8");
	}

	public class ImageAdapter extends BaseAdapter {
		private final Context context;
		private final int galleryItemBackground;
		private HashMap<URL, Drawable> drawnImages;

		private Drawable ImageOperations(ImageAdapter imageAdapter, URL url) {

			if (drawnImages.containsKey(url)) {
				return drawnImages.get(url);
			} else {
				try {
					InputStream is = (InputStream) url.getContent();
					Drawable d = Drawable.createFromStream(is, "src");
					drawnImages.put(url, d);
					Log.d(TAG, "Drawable " + url + "zum Array hinzugefügt");
					return d;
				} catch (MalformedURLException e) {
					return null;
				} catch (IOException e) {
					return null;
				}
			}
		}

		/**
		 * @param _context
		 */
		public ImageAdapter(Context _context) {
			context = _context;
			drawnImages = new HashMap<URL, Drawable>();
			TypedArray ta = obtainStyledAttributes(R.styleable.Gallery);
			galleryItemBackground = ta.getResourceId(
					R.styleable.Gallery_android_galleryItemBackground, 0);
			ta.recycle();

		}

		@Override
		public int getCount() {
			return m.getTopics().size();
		}

		@Override
		public Object getItem(int position) {
			return m.getTopics().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.gallery_item, null);

			ImageView iv = (ImageView) view.findViewById(R.id.image);
			try {
				URL url = m.getTopics().get(position).getImage();
				Drawable img = ImageOperations(this, url);
				iv.setImageDrawable(img);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			iv.setPadding(2, 2, 2, 2);
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			iv.setBackgroundResource(galleryItemBackground);

			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					load_wv_image(m.getTopics().get(position).getImage());
					currentTopic = position;
				}

			});

			return view;

		}
	}

	private void addDummyTopics() throws MalformedURLException {
		m.addTopic(new Topic().setId(0).setMeeting(m).setName("0 T")
				.setImage(new URL("http://dummyimage.com/480x800/000/fff.jpg")));
		m.addTopic(new Topic()
				.setId(1)
				.setMeeting(m)
				.setName("1 T")
				.setImage(
						new URL("http://dummyimage.com/480x800/251459/fff.jpg")));
		m.addTopic(new Topic()
				.setId(2)
				.setMeeting(m)
				.setName("2 T")
				.setImage(
						new URL("http://dummyimage.com/480x800/eeeeee/fff.jpg")));
		m.addTopic(new Topic()
				.setId(3)
				.setMeeting(m)
				.setName("3 T")
				.setImage(
						new URL("http://dummyimage.com/480x800/540054/fff.jpg")));
		m.addTopic(new Topic()
				.setId(4)
				.setMeeting(m)
				.setName("4 T")
				.setImage(
						new URL("http://dummyimage.com/480x800/943540/fff.jpg")));
		m.addTopic(new Topic()
				.setId(5)
				.setMeeting(m)
				.setName("5 T")
				.setImage(
						new URL("http://dummyimage.com/480x800/234023/fff.jpg")));

		for (Topic t : m.getTopics()) {
			Log.i(" T ", t.getName());
		}

	}

	private void addDummyQuestions() {

		for (int i = 0; i < m.getTopics().size(); i++) {
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Wie gefällt es Dir?")
									.setModus("single")
									.setTopic(m.getTopics().get(i))
									.setType("range"));
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Bist Du müde?")
									.setModus("single")
									.setTopic(m.getTopics().get(i))
									.setType("singlechoice"));
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Was kannst Du?")
									.setModus("single")
									.setTopic(m.getTopics().get(i))
									.setType("multiplechoice"));
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Wie gefällt es Dir? (AVG)")
									.setModus("avg")
									.setTopic(m.getTopics().get(i))
									.setType("range"));
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Bist Du müde? (AVG)")
									.setModus("avg")
									.setTopic(m.getTopics().get(i))
									.setType("singlechoice"));
			m.getTopics()
					.get(i)
					.addQuestion(
							new Question().setName("Was kannst Du? (AVG)")
									.setModus("avg")
									.setTopic(m.getTopics().get(i))
									.setType("multiplechoice"));

		}

		for (Topic t : m.getTopics()) {
			for (Question q : t.getQuestions())
				Log.i(" Q ", t.getName() + q.getName());
		}
	}

	private void addDummyQuestionsOptions() {
		for (int i = 0; i < m.getTopics().size(); i++) {
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(0)
					.addQuestionOption(
							new QuestionOption()
									.setKey("min_value")
									.setValue("0")
									.setQuestion(
											m.getTopics().get(i).getQuestions()
													.get(0)));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(0)
					.addQuestionOption(
							new QuestionOption()
									.setKey("max_value")
									.setValue("100")
									.setQuestion(
											m.getTopics().get(i).getQuestions()
													.get(0)));

			m.getTopics()
					.get(i)
					.getQuestions()
					.get(1)
					.addChoice(
							(new Choice().setName("JA").setQuestion(m
									.getTopics().get(i).getQuestions().get(1))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(1)
					.addChoice(
							(new Choice().setName("Vielleicht").setQuestion(m
									.getTopics().get(i).getQuestions().get(1))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(1)
					.addChoice(
							(new Choice().setName("NEIN").setQuestion(m
									.getTopics().get(i).getQuestions().get(1))));

			m.getTopics()
					.get(i)
					.getQuestions()
					.get(2)
					.addChoice(
							(new Choice().setName("singen").setQuestion(m
									.getTopics().get(i).getQuestions().get(2))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(2)
					.addChoice(
							(new Choice().setName("schlafen").setQuestion(m
									.getTopics().get(i).getQuestions().get(2))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(2)
					.addChoice(
							(new Choice().setName("lachen").setQuestion(m
									.getTopics().get(i).getQuestions().get(2))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(2)
					.addChoice(
							(new Choice().setName("tanzen").setQuestion(m
									.getTopics().get(i).getQuestions().get(2))));

			// AVG
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(3)
					.addQuestionOption(
							new QuestionOption()
									.setKey("min_value")
									.setQuestion(
											m.getTopics().get(i).getQuestions()
													.get(3)).setValue("0"));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(3)
					.addQuestionOption(
							new QuestionOption()
									.setKey("max_value")
									.setQuestion(
											m.getTopics().get(i).getQuestions()
													.get(3)).setValue("100"));

			m.getTopics()
					.get(i)
					.getQuestions()
					.get(4)
					.addChoice(
							(new Choice().setName("JA").setQuestion(m
									.getTopics().get(i).getQuestions().get(4))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(4)
					.addChoice(
							(new Choice().setName("Vielleicht").setQuestion(m
									.getTopics().get(i).getQuestions().get(4))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(4)
					.addChoice(
							(new Choice().setName("NEIN").setQuestion(m
									.getTopics().get(i).getQuestions().get(4))));

			m.getTopics()
					.get(i)
					.getQuestions()
					.get(5)
					.addChoice(
							(new Choice().setName("singen").setQuestion(m
									.getTopics().get(i).getQuestions().get(5))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(5)
					.addChoice(
							(new Choice().setName("schlafen").setQuestion(m
									.getTopics().get(i).getQuestions().get(5))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(5)
					.addChoice(
							(new Choice().setName("lachen").setQuestion(m
									.getTopics().get(i).getQuestions().get(5))));
			m.getTopics()
					.get(i)
					.getQuestions()
					.get(5)
					.addChoice(
							(new Choice().setName("tanzen").setQuestion(m
									.getTopics().get(i).getQuestions().get(5))));
		}

		for (Topic t : m.getTopics()) {
			for (Question q : t.getQuestions()){
				for (QuestionOption qo : q.getQuestionOptions())
					Log.i(" QO ", t.getName() + q.getName() + qo.getKey());
				for (Choice c : q.getChoices())
					Log.i(" C ", t.getName() + q.getName() + c.getName());

		}
			}
	}

	@Override
	protected ServiceMessageRunnable getServiceMessageRunnable(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

}
