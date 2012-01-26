package de.hsrm.mi.mobcomp.y2k11grp04.view;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	private final List<Topic> topics;

	/**
	 * @param topics
	 */
	public TopicGalleryAdapter(List<Topic> topics) {
		this.topics = topics;
	}

	@Override
	public int getCount() {
		return topics.size();
	}

	@Override
	public Topic getItem(int position) {
		return topics.get(position);
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

				Bitmap bm = BitmapFactory.decodeFile(topic.getImageFile()
						.getAbsolutePath());
				Bitmap thumb = Bitmap.createScaledBitmap(bm, 150, 150, true);
				bm.recycle();
				System.gc();
				topicImage.setImageBitmap(thumb);
				view.removeView(view
						.findViewById(R.id.groupMood_topicItem_Image_Loading));
			}
		}
		return view;
	}
}