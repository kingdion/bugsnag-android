package com.bugsnag.android;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

class DefaultDelivery implements Delivery {

    private final ConnectivityManager connectivityManager;

    DefaultDelivery(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
    }

    @Override
    public void deliver(SessionTrackingPayload payload,
                        Configuration config) throws BadResponseException, NetworkException {
        String endpoint = config.getSessionEndpoint();
        int status = deliver(endpoint, payload, config.getSessionApiHeaders());

        if (status != 202) {
            throw new BadResponseException("Request failed with status " + status, null);
        } else {
            Logger.info("Completed session tracking request");
        }
    }

    @Override
    public void deliver(Report report,
                        Configuration config) throws BadResponseException, NetworkException {
        String endpoint = config.getEndpoint();
        int status = deliver(endpoint, report, config.getErrorApiHeaders());

        if (status / 100 != 2) {
            throw new BadResponseException("Request failed with status " + status, null);
        } else {
            Logger.info("Completed error API request");
        }
    }

    int deliver(String urlString,
                JsonStream.Streamable streamable,
                Map<String, String> headers) throws NetworkException {
        checkHasNetworkConnection();
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.addRequestProperty("Content-Type", "application/json");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }

            JsonStream stream = null;

            try {
                OutputStream out = conn.getOutputStream();
                Charset charset = Charset.forName("UTF-8");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset));
                stream = new JsonStream(writer);
                streamable.toStream(stream);
            } finally {
                IOUtils.closeQuietly(stream);
            }

            // End the request, get the response code
            return conn.getResponseCode();
        } catch (IOException exception) {
            throw new NetworkException("IOException encountered in request", exception);
        } finally {
            IOUtils.close(conn);
        }
    }

    private void checkHasNetworkConnection() throws NetworkException {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // conserve device battery by avoiding radio use
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting())) {
            throw new NetworkException("No network connection available", null);
        }
    }
}
