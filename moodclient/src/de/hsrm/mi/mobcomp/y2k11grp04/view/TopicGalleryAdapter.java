package de.hsrm.mi.mobcomp.y2k11grp04.view;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.hsrm.mi.mobcomp.y2k11grp04.R;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class TopicGalleryAdapter extends BaseAdapter {

	private List<Topic> topics;

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
		View view = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.gallery_item_text, null);
		// TODO: Image
		Topic topic = getItem(position);
		if (topic != null) {
			TextView text = (TextView) view.findViewById(R.id.groupMood_name);
			text.setText(topic.getName());
		}
		return view;
	}
}