package by.grodno.bus;


import android.content.Context;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class NetManager {
    public interface ResponseProcessor {
        void onResponse(String response);
    }

    public static final String getHTMLFromUrl(String sourceUrl) throws Exception {
        URL url = new URL(sourceUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream is = urlConnection.getInputStream();
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader bReader = new BufferedReader(isReader);
        String res;
        StringBuilder builder = new StringBuilder();
        try {
            while ((res = bReader.readLine()) != null) {
                builder.append(res);
            }
        } finally {
            bReader.close();
        }
        return builder.toString();
    }

    public void request(final String url, final Context context, final ResponseProcessor responseProcessor) {
        new Thread(new Runnable() {
            Handler h = new Handler();

            @Override
            public void run() {
                try {
                    String response = getHTMLFromUrl(url);
                    if (responseProcessor != null) {
                        responseProcessor.onResponse(response);
                    }
                } catch (final Exception e) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            if (e instanceof UnknownHostException) {
                                ErrorHelper.showErrorDialog(context.getString(R.string.inet_is_unavailable), context, null);
                            } else {
                                ErrorHelper.showErrorDialog(e.getMessage(), context, null);
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
