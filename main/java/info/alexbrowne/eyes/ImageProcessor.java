package info.alexbrowne.eyes;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by alex on 11/15/14.
 */
public class ImageProcessor extends AsyncTask<byte[], Void, Void> {

    private static String TAG = "ImageProcessor";
    private final double CONFIDENCE_THRESHOLD = 0.03;
    private Speaker speaker;
    private ProcessManager pm;

    public ImageProcessor(Speaker speaker, ProcessManager pm) {
        this.speaker = speaker;
        this.pm = pm;
    }

    @Override
    protected Void doInBackground(byte[]... imageData) {
        String rawData = "";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    "http://www.clarifai.com/demo/upload/");
            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            ContentBody bin = new ByteArrayBody(imageData[0], "image.jpg");
            reqEntity.addPart("files[]", bin);
            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            rawData = s.toString();
            Log.d(TAG, "Response: " + rawData);
        } catch (Exception e) {
            // handle exception here
            Log.e(TAG, "Error posting image file: " + e.getMessage());
        }

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(rawData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Guess> topGuesses = new ArrayList<Guess>();
        try {
            JSONObject result = jObject.getJSONArray("files").getJSONObject(0);
            JSONArray values = result.getJSONArray("predicted_classes");
            JSONArray confidences = result.getJSONArray("predicted_probs");
            double prevConfidence = 0.0;
            for (int i = 0; i < values.length(); i++) {
                Guess guess = new Guess(values.getString(i), confidences.getString(i));
                double c = guess.getConfidence();
                if (c >= CONFIDENCE_THRESHOLD && c >= prevConfidence*0.5)  {
                    // Add to our guesses iff confidence for this guess is over some
                    // threshold and this guess is at least half as likely
                    topGuesses.add(guess);
                    prevConfidence = c;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Speak the top results
        Log.d(TAG, "top guesses: " + topGuesses);
        speaker.allow(true);
        if (topGuesses.size() == 0) {
            speaker.speak("Unknown object");
        } else if (topGuesses.size() == 1) {
            speaker.speak(topGuesses.get(0).toString());
        } else {
            String phrase = TextUtils.join(" or ", topGuesses.subList(0, Math.min(3, topGuesses.size())));
            speaker.speak(phrase);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void nothing) {
        pm.setReady(true);
    }
}
