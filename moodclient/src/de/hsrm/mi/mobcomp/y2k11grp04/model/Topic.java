package de.hsrm.mi.mobcomp.y2k11grp04.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import de.hsrm.mi.mobcomp.y2k11grp04.service.Relation;

/**
 * Themen eines {@link Meeting Meetings}.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class Topic extends BaseModel {
	private String name;
	private Meeting meeting;
	private Uri image;
	private List<Question> questions = new ArrayList<Question>();
	private File imageFile;

	public Topic() {
	}

	public Topic(Parcel in) {
		readFromParcel(in);
	}

	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		name = in.readString();
		String uriString = in.readString();
		if (uriString.length() > 0)
			image = Uri.parse(uriString);
		String imageFileString = in.readString();
		if (imageFileString.length() > 0)
			imageFile = new File(imageFileString);
		for (Parcelable q : in
				.readParcelableArray(Topic.class.getClassLoader())) {
			questions.add((Question) q);
			((Question) q).setTopic(this);
		}
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(name);
		out.writeString(image != null ? image.toString() : "");
		out.writeString(imageFile != null ? imageFile.toString() : "");
		out.writeParcelableArray(
				questions.toArray(new Question[questions.size()]), 0);
	}

	public static final Parcelable.Creator<Topic> CREATOR = new Parcelable.Creator<Topic>() {
		public Topic createFromParcel(Parcel in) {
			return new Topic(in);
		}

		public Topic[] newArray(int size) {
			return new Topic[size];
		}
	};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Meeting getMeeting() {
		return meeting;
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}

	public Uri getImage() {
		return image;
	}

	public void setImage(Uri image) {
		this.image = image;
	}

	@Override
	public void setRelationItems(Relation relation,
			List<? extends StateModel> items) {
		if (relation.getModel() == Question.class) {
			questions = new ArrayList<Question>();
			for (StateModel m : items) {
				questions.add((Question) m);
				((Question) m).setTopic(this);
			}
		} else {
			super.setRelationItems(relation, items);
		}
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<Question> questions) {
		this.questions = questions;

	}

	/**
	 * Datei in dem das Bild des Topics abgespeichert ist
	 * 
	 * @param imageFile
	 */
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * Gibt die Datei zurück, in dem das Bild gespeichert ist
	 */
	public File getImageFile() {
		return imageFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((meeting == null) ? 0 : meeting.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Topic other = (Topic) obj;
		if (meeting == null) {
			if (other.meeting != null)
				return false;
		} else if (!meeting.equals(other.meeting))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
