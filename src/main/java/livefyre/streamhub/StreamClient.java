package livefyre.streamhub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class StreamClient {
	public static String generateStreamUrl(String networkId,
			String collectionId, String eventId) throws MalformedURLException {
		final Builder uriBuilder = new Uri.Builder()
				.scheme(Config.scheme)
				.authority(
						Config.streamDomain + "."
								+ Config.getHostname(networkId))
				.appendPath("v3.1").appendPath("collection")
				.appendPath(collectionId).appendPath("").appendPath(eventId);

		return uriBuilder.toString();
	}

	/**
	 * Performs a long poll request to the Livefyre's stream endpoint
	 * 
	 * @param networkId
	 *            The collection's network as identified by domain, i.e.
	 *            livefyre.com.
	 * @param collectionId
	 *            The Id of the collection
	 * @param eventId
	 *            The last eventId that was returned from either stream or
	 *            bootstrap. Event time a new eventId is returned, it should be
	 *            used in the next stream request.
	 * @param handler
	 *            Response handler
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public static void pollStreamEndpoint(final String networkId,
			final String collectionId, final String eventId,
			final AsyncHttpResponseHandler handler) throws IOException,
			JSONException {
		final String streamEndpoint = generateStreamUrl(networkId,
				collectionId, eventId);
		HttpClient.client.get(streamEndpoint, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String responce) {
				handler.onSuccess(responce);
				try {

					if (responce != null) {
						Log.d("Stream Clint Call", "Success" + responce);
						JSONObject responceJson = new JSONObject(responce);
						String lastEvent;
						if (responceJson.has("data")) {
							lastEvent = responceJson.getJSONObject("data")
									.getString("maxEventId");

							pollStreamEndpoint(networkId, collectionId,
									lastEvent, handler);
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				try {
					pollStreamEndpoint(networkId, collectionId, eventId,
							handler);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}
}
