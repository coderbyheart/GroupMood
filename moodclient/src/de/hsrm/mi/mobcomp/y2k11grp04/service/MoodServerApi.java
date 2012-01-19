package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
import de.hsrm.mi.mobcomp.y2k11grp04.model.StateModel;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;

public class MoodServerApi {
	private HttpClient client;
	private Map<Uri, Class<? extends Model>> contextToModel = new HashMap<Uri, Class<? extends Model>>();
	private Map<Class<? extends Model>, Uri> modelToContext = new HashMap<Class<? extends Model>, Uri>();

	private class JSONReader<T extends Model> {
		private T objectInstance;

		public static final String KEY_CONTEXT = "@context";
		public static final String KEY_ID = "@id";
		public static final String KEY_RELATIONS = "@relations";
		public static final String KEY_STATUS = "status";
		public static final String KEY_RESULT = "result";
		private JSONObject objectData;

		public JSONReader(JSONObject jsonData, Class<T> objectClass,
				String dataKey) throws ApiException {
			newInstance(objectClass);
			Uri context = modelToContext.get(objectClass);
			Log.v(getClass().getCanonicalName(), "Lese " + context.toString()
					+ " aus " + dataKey);
			try {
				objectData = jsonData.getJSONObject(dataKey);
			} catch (JSONException e) {
				throw new ApiException("API error: Response has no " + dataKey
						+ " object.");
			}
		}

		private void newInstance(Class<T> objectClass) {
			try {
				this.objectInstance = objectClass.newInstance();
			} catch (IllegalAccessException e) {
				throw new InvalidParameterException("Could not access class "
						+ objectClass.toString());
			} catch (InstantiationException e) {
				throw new InvalidParameterException(
						"Could not instantiate class " + objectClass.toString());
			}
		}

		public JSONReader(Class<T> objectClass, JSONObject objectData) {
			this.objectData = objectData;
			newInstance(objectClass);
		}

		public T get() throws ApiException {

			String objectContext;
			Uri contextUri = getObjectInstanceContext();

			try {
				// Check context
				objectContext = objectData.getString(KEY_CONTEXT);
			} catch (JSONException e) {
				throw new ApiException("API error: " + contextUri.toString()
						+ " object has no " + KEY_CONTEXT);
			}

			if (!contextUri.toString().equals(objectContext)) {
				throw new ApiException("Unexpected context: " + objectContext
						+ ". Expected: " + contextUri.toString());
			}

			if (objectInstance instanceof StateModel) {
				Log.v(getClass().getCanonicalName(), "Checke URI");
				// Check @id
				try {
					Uri objectUri = Uri.parse(objectData.getString(KEY_ID));
					((StateModel) objectInstance).setUri(objectUri);
				} catch (JSONException e) {
					throw new ApiException("API error: "
							+ contextUri.toString() + " object has no "
							+ KEY_ID);
				}
			}

			for (Method m : objectInstance.getClass().getMethods()) {
				if (!Modifier.isPublic(m.getModifiers())) {
					continue;
				}

				@SuppressWarnings("rawtypes")
				Class[] params = m.getParameterTypes();
				if (params.length != 1) {
					continue;
				}
				@SuppressWarnings("rawtypes")
				Class param = m.getParameterTypes()[0];
				if (!m.getName().substring(0, 3).equals("set")) {
					continue;

				}
				String key = m.getName().substring(3, 4).toLowerCase()
						+ m.getName().substring(4);
				if (key.equals("uri"))
					continue;
				try {
					if (param == int.class || param.equals(Integer.class)) {
						Integer value;
						try {
							value = objectData.getInt(key);
						} catch (JSONException e) {
							throw new ApiException(
									"Failed to get int value for " + key);
						}
						try {
							m.invoke(objectInstance, value);
						} catch (IllegalArgumentException e) {
							throw new ApiException(objectInstance.getClass()
									.toString()
									+ "#"
									+ m.getName()
									+ "(Integer) did not work.");
						}
					} else if (param == Uri.class) {
						String uriVal;
						try {
							uriVal = objectData.isNull(key) ? null : objectData
									.getString(key);
						} catch (JSONException e) {
							throw new ApiException(
									"Failed to get Uri value for " + key);
						}
						if (uriVal != null) {
							Uri value = Uri.parse(uriVal);
							try {
								m.invoke(objectInstance, value);
							} catch (IllegalArgumentException e) {
								throw new ApiException(objectInstance
										.getClass().toString()
										+ "#"
										+ m.getName() + "(Uri) did not work.");
							}
						}

					} else if (param == boolean.class) {
						boolean value;
						try {
							value = objectData.getBoolean(key);
						} catch (JSONException e) {
							throw new ApiException(
									"Failed to get boolean value for " + key);
						}
						try {
							m.invoke(objectInstance, value);
						} catch (IllegalArgumentException e) {
							throw new ApiException(objectInstance.getClass()
									.toString()
									+ "#"
									+ m.getName()
									+ "(boolean) did not work.");
						}
					} else if (param == String.class) {
						String value;
						try {
							value = objectData.getString(key);
						} catch (JSONException e) {
							throw new ApiException(
									"Failed to get string value for " + key);
						}
						try {
							m.invoke(objectInstance, value);
						} catch (IllegalArgumentException e) {
							throw new ApiException(objectInstance.getClass()
									.toString()
									+ "#"
									+ m.getName()
									+ "(String) did not work.");
						}
					} else {
						// TODO: support all Types
						Log.d(getClass().getCanonicalName(), "Skipped value "
								+ key + " of type " + param.toString() + " on "
								+ objectInstance.getClass().toString());
					}
				} catch (IllegalAccessException e) {
					throw new ApiException("Oops. I thought "
							+ objectInstance.getClass().toString() + "#"
							+ m.getName() + " was public.");
				} catch (InvocationTargetException e) {
					throw new ApiException("Could not I invoke "
							+ objectInstance.getClass().toString() + "#"
							+ m.getName());
				}
			}

			// Add relations
			if (objectInstance instanceof StateModel) {
				if (objectData.has(KEY_RELATIONS)) {
					Log.v(getClass().getCanonicalName(), "Lese @relations von "
							+ contextUri.toString());

					try {
						List<Relation> instanceRelations = new ArrayList<Relation>();
						JSONArray relations = objectData
								.getJSONArray(KEY_RELATIONS);
						for (int i = 0; i < relations.length(); i++) {
							Relation rel = new JSONReader<Relation>(
									Relation.class, relations.getJSONObject(i))
									.get();
							Log.v(getClass().getCanonicalName(),
									contextUri.toString() + " hat Relation zu "
											+ rel.getRelatedcontext());
							if (!contextToModel.containsKey(rel
									.getRelatedcontext())) {
								Log.v(getClass().getCanonicalName(),
										"Skipped relation with unknown context "
												+ rel.getRelatedcontext()
														.toString());
								continue;
							}
							rel.setModel(contextToModel.get(rel
									.getRelatedcontext()));
							instanceRelations.add(rel);
						}
						((StateModel) objectInstance)
								.setRelations(instanceRelations);
					} catch (JSONException e) {
						throw new ApiException("Failed to read relations from "
								+ contextUri.toString());
					}
				}
			}

			return objectInstance;
		}

		/**
		 * Lädt das Objekt und alle Relationen
		 * 
		 * TODO: Sicherstellen dass nur abwärts zeigende Relationen verfolgt
		 * werden, sonst lädt man sich so den ganzen Graphen
		 * 
		 * @return T
		 * @throws ApiException
		 */
		@SuppressWarnings("unchecked")
		public T getRecursive() throws ApiException {
			T top = this.get();
			if (top instanceof StateModel) {
				for (Relation relation : ((StateModel) top).getRelations()) {
					if (!contextToModel.containsKey(relation
							.getRelatedcontext())) {
						Log.v(getClass().getCanonicalName(),
								"Skipped unknown context "
										+ relation.getRelatedcontext()
												.toString());
						continue;
					}
					if (relation.isList()) {
						Log.v(getClass().getCanonicalName(), "Lade Relation "
								+ relation.getHref().toString() + " von "
								+ modelToContext.get(top.getClass()).toString());
						HttpGet request = new HttpGet(relation.getHref()
								.toString());
						try {
							JSONObject response = execute(request);
							JSONArray items = response.getJSONArray(KEY_RESULT);
							List<StateModel> instanceItems = new ArrayList<StateModel>();
							for (int i = 0; i < items.length(); i++) {
								JSONObject item = items.getJSONObject(i);
								try {
									Class<? extends Model> itemClass = contextToModel
											.get(Uri.parse(item
													.getString(KEY_CONTEXT)));
									@SuppressWarnings("rawtypes")
									StateModel itemInstance = (StateModel) new JSONReader(
											itemClass, item).getRecursive();
									instanceItems.add(itemInstance);
								} catch (JSONException e) {
									throw new ApiException(
											"No context in item for "
													+ getObjectInstanceContext()
															.toString());
								}
							}
							Log.v(getClass().getCanonicalName(), "Set "
									+ instanceItems.size()
									+ " related items on "
									+ top.getClass().getCanonicalName());
							((StateModel) top).setRelationItems(relation,
									instanceItems);
						} catch (JSONException e) {
							throw new ApiException(
									"Failed to read list relation "
											+ relation.getHref().toString()
											+ " from "
											+ getObjectInstanceContext()
													.toString());
						}
					} else {
						// TODO: Implementieren
					}
				}
			}
			return top;
		}

		private Uri getObjectInstanceContext() {
			return modelToContext.get(this.objectInstance.getClass());
		}
	}

	public MoodServerApi() {
		client = new DefaultHttpClient();
		registerModel(ApiStatus.class,
				Uri.parse("http://groupmood.net/jsonld/apistatus"));
		registerModel(Relation.class,
				Uri.parse("http://groupmood.net/jsonld/relation"));
	}

	public Meeting getMeeting(Uri meetingUri) throws ApiException {
		Log.v(getClass().getCanonicalName(),
				"Fetching meeting " + meetingUri.toString());
		Uri u = meetingUri
				.buildUpon()
				.scheme(meetingUri.toString().contains("+https") ? "https"
						: "http").build();
		HttpGet request = new HttpGet(u.toString());
		JSONObject response = execute(request);
		Meeting meeting = new JSONReader<Meeting>(response, Meeting.class,
				JSONReader.KEY_RESULT).get();
		return meeting;
	}

	public Meeting getMeetingRecursive(Uri meetingUri) throws ApiException {
		Log.v(getClass().getCanonicalName(),
				"Fetching meeting " + meetingUri.toString());
		Uri u = meetingUri
				.buildUpon()
				.scheme(meetingUri.toString().contains("+https") ? "https"
						: "http").build();
		HttpGet request = new HttpGet(u.toString());
		JSONObject response = execute(request);
		Meeting meeting = new JSONReader<Meeting>(response, Meeting.class,
				JSONReader.KEY_RESULT).getRecursive();
		return meeting;
	}

	private JSONObject execute(HttpUriRequest request) throws ApiException {
		HttpResponse response;
		request.setHeader("Accept", "application/json");
		String dataAsString;
		try {
			response = client.execute(request);

			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != 200) {
				throw new ApiException("Invalid response from server: "
						+ status.toString());
			}

			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			dataAsString = new String(content.toByteArray());
		} catch (ClientProtocolException e) {
			throw new ApiException("Protocol error: " + e.toString());
		} catch (IOException e) {
			throw new ApiException("I/O error: " + e.toString());
		}
		Log.v(getClass().getCanonicalName(), dataAsString);
		JSONObject jsonResponse;
		try {
			jsonResponse = (JSONObject) new JSONTokener(dataAsString)
					.nextValue();
		} catch (JSONException e) {
			throw new ApiException("Failed to parse JSON response: "
					+ dataAsString);
		}
		JSONReader<ApiStatus> jsonResponseReader = new JSONReader<ApiStatus>(
				jsonResponse, ApiStatus.class, JSONReader.KEY_STATUS);
		ApiStatus s = jsonResponseReader.get();
		if (!s.getMessage().equals(ApiStatus.STATUS_OK)) {
			throw new ApiException("API error: " + s.getMessage() + "("
					+ s.getCode() + ")");
		}
		return jsonResponse;
	}

	public void registerModel(Class<? extends Model> modelClass, Uri context) {
		contextToModel.put(context, modelClass);
		modelToContext.put(modelClass, context);
	}

	/**
	 * Lädt die Topics eines Meetings
	 * 
	 * @param meeting
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Topic> getTopics(Meeting meeting) throws ApiException {
		// Suche Topic-Relation dieses Meetings
		Relation topicRelation = null;
		for (Relation rel : meeting.getRelations()) {
			if (rel.getModel().equals(Topic.class))
				topicRelation = rel;
		}
		// FIXME: Relations müssen auch serialisiert werden
		topicRelation = new Relation();
		topicRelation.setHref(Uri
				.parse(meeting.getUri().toString() + "/topics"));
		Log.v(getClass().getCanonicalName(), "Fetching topics "
				+ topicRelation.getHref().toString());
		HttpGet request = new HttpGet(topicRelation.getHref().toString());
		JSONObject response = execute(request);

		ArrayList<Topic> topics = new ArrayList<Topic>();

		try {
			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
			for (int i = 0; i < items.length(); i++) {
				Topic topic = new JSONReader<Topic>(Topic.class,
						items.getJSONObject(i)).get();
				topics.add(topic);
			}
		} catch (JSONException e) {
			throw new ApiException("Failed to read topics from "
					+ topicRelation.getHref().toString());
		}
		return topics;
	}

	public ArrayList<Question> getQuestionsWithExtras(Topic topic)
			throws ApiException {
		// FIXME: Relations müssen auch serialisiert werden
		Relation questionRelation = new Relation();
		questionRelation.setHref(Uri.parse(topic.getUri().toString()
				+ "/questions"));
		Log.v(getClass().getCanonicalName(), "Fetching topics "
				+ questionRelation.getHref().toString());
		HttpGet request = new HttpGet(questionRelation.getHref().toString());
		JSONObject response = execute(request);

		ArrayList<Question> questions = new ArrayList<Question>();

		try {
			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
			for (int i = 0; i < items.length(); i++) {
				Question question = new JSONReader<Question>(Question.class,
						items.getJSONObject(i)).getRecursive();
				questions.add(question);
			}
		} catch (JSONException e) {
			throw new ApiException("Failed to read topics from "
					+ questionRelation.getHref().toString());
		}
		return questions;
	}
}
