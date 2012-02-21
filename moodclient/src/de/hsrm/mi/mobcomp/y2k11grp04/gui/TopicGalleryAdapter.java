package de.hsrm.mi.mobcomp.y2k11grp04.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

/**
 * @author Coralie Reuter
 * @author Markus Tacker
 * 
 */
public class TopicGalleryAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = TopicGalleryAdapter.class.getSimpleName();
	private List<Topic> topics;

	/**
	 * @param topics
	 */
	public TopicGalleryAdapter(List<Topic> topics) {
		this.topics = topics;
	}

	@Override
	public int getCount() {
		return getTopics().size();
	}

	@Override
	public Topic getItem(int position) {
		return getTopics().get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Topic topic = getItem(position);
		if (topic == null)
			return null;
		return createTopicView(parent, topic);
	}

	protected View createTopicView(ViewGroup parent, Topic topic) {
		ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.topic_item, parent, false);

		if (topic.getImage() == null) {
			TextView topicName = (TextView) view
					.findViewById(R.id.groupMood_topicItem_Name);
			topicName.setText(topic.getName());
			view.removeView(view.findViewById(R.id.groupMood_topicItem_Image));
			view.removeView(view
					.findViewById(R.id.groupMood_topicItem_Image_Loading));
		} else {
			view.removeView(view.findViewById(R.id.groupMood_topicItem_Name));
			if (topic.getImageFile() == null) {
				view.removeView(view
						.findViewById(R.id.groupMood_topicItem_Image));
			} else {
				ImageView topicImage = (ImageView) view
						.findViewById(R.id.groupMood_topicItem_Image);

				File thumbFile = getThumbFile(topic.getImageFile());
				// Bitmap erzeugen und lokal abspeichern
				if (!thumbFile.exists()) {
					Bitmap bm = BitmapFactory.decodeFile(topic.getImageFile()
							.getAbsolutePath());
					Bitmap thumb = Bitmap
							.createScaledBitmap(bm, 150, 150, true);
					try {
						OutputStream stream = new FileOutputStream(
								thumbFile.getAbsolutePath());
						thumb.compress(CompressFormat.PNG, 75, stream);
						stream.close();
					} catch (Exception e) {
						Log.e(getClass().getCanonicalName(), e.getMessage());
					}
					thumb.recycle();
					bm.recycle();
					System.gc();
				}
				// Bitmap laden
				topicImage.setImageBitmap(BitmapFactory.decodeFile(thumbFile
						.getAbsolutePath()));
				view.removeView(view
						.findViewById(R.id.groupMood_topicItem_Image_Loading));
			}
		}
		return view;
	}

	private File getThumbFile(File imageFile) {

		String path = imageFile.getAbsolutePath();
		String pathWithoutExt = path.substring(0, path.lastIndexOf("."));
		return new File(pathWithoutExt + "-thumb.png");
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}
}