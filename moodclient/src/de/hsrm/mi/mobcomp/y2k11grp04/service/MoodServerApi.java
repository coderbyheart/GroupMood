package de.hsrm.mi.mobcomp.y2k11grp04.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import android.util.Log;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;
import de.hsrm.mi.mobcomp.y2k11grp04.model.StateModel;

public class MoodServerApi {
	private HttpClient client;

	private class JSONReader<T extends Model> {
		private JSONObject jsonData;
		private T objectInstance;
		private String dataKey;

		public JSONReader(JSONObject jsonData, Class<T> objectClass,
				String dataKey) {
			this.jsonData = jsonData;
			try {
				this.objectInstance = objectClass.newInstance();
			} catch (IllegalAccessException e) {
				throw new InvalidParameterException("Could not access class "
						+ objectClass.toString());
			} catch (InstantiationException e) {
				throw new InvalidParameterException(
						"Could not instantiate class " + objectClass.toString());
			}
			this.dataKey = dataKey == null ? this.objectInstance.getContext()
					: dataKey;
			Log.v(getClass().getCanonicalName(),
					"Lese " + objectInstance.getContext() + " aus "
							+ this.dataKey);
		}

		public T get() throws ApiException {
			JSONObject objectData;
			String objectContext;
			try {
				objectData = jsonData.getJSONObject(dataKey);
			} catch (JSONException e) {
				throw new ApiException("API error: Response has no " + dataKey
						+ " object.");
			}

			try {
				// Check context
				objectContext = objectData.getString("@context");
			} catch (JSONException e) {
				throw new ApiException("API error: "
						+ objectInstance.getContext()
						+ " object has no @context.");
			}
			Uri contextUri = getContextUri(objectInstance);
			if (!contextUri.toString().equals(objectContext)) {
				throw new ApiException("Unexpected context: " + objectContext
						+ ". Expected: " + contextUri.toString());
			}

			if (objectInstance instanceof StateModel) {
				Log.v(getClass().getCanonicalName(), "Checke URI");
				// Check @id
				try {
					Uri objectUri = Uri.parse(objectData.getString("@id"));
					((StateModel) objectInstance).setUri(objectUri);
				} catch (JSONException e) {
					throw new ApiException("API error: "
							+ objectInstance.getContext()
							+ " object has no @id.");
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
				if (key.equals("uri")) continue;
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
					} else { // String
						// TODO: support all Types
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

			return objectInstance;
		}

		private Uri getContextUri(T type) {
			return Uri
					.parse("http://groupmood.net/jsonld/" + type.getContext());
		}
	}

	public MoodServerApi() {
		client = new DefaultHttpClient();
	}

	public Meeting getMeeting(Uri meetingUri) throws ApiException {
		Log.v(getClass().getCanonicalName(),
				"Fetching meeting " + meetingUri.toString());
		Uri u = meetingUri.buildUpon().scheme("http").build();
		HttpGet request = new HttpGet(u.toString());
		JSONObject response = execute(request);
		return new JSONReader<Meeting>(response, Meeting.class, "result").get();
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
				jsonResponse, ApiStatus.class, "status");
		ApiStatus s = jsonResponseReader.get();
		if (!s.getMessage().equals(ApiStatus.STATUS_OK)) {
			throw new ApiException("API error: " + s.getMessage() + "("
					+ s.getCode() + ")");
		}
		return jsonResponse;
	}
}
