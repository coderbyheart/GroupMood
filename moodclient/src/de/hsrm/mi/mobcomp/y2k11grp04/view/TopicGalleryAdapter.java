package de.hsrm.mi.mobcomp.y2k11grp04.view;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_item, null);
		TextView text = (TextView) view.findViewById(R.id.groupMood_topicItem_Name);
		ImageView image = (ImageView) view.findViewById(R.id.groupMood_topicItem_Image);
		Topic topic = getItem(position);
		if (topic != null) {

			if (topic.getImage() == null) {
				text.setText(topic.getName());
				view.removeView(image);
			} else {
				// TODO: MediaView verwenden. Media-URI kommt vom Service.
				//  Doppelklick soll Vollbild anzeigen.
				// view.removeView(text);
				text.setText("Image: " + topic.getName());
				view.removeView(image);
			}
		}
		return view;
	}
}