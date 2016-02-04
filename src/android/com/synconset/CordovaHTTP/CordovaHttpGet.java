/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import android.util.Base64;

public class CordovaHttpGet extends CordovaHttp implements Runnable {
    public CordovaHttpGet(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, params, headers, callbackContext);
    }
    
    @Override
    public void run() {
        try {
            Log.e("XXX", "CUSTOM GET START!!! " + this.getUrlString());
            HttpRequest request = HttpRequest.get(this.getUrlString(), this.getParams(), true);
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            int code = request.code();
            String body;
            if(this.getUrlString().matches("^.*/sheets/.*/jpeg.*$")) {
              body = Base64.encodeToString(request.bytes(), Base64.DEFAULT);
            } else {
              body = request.body(CHARSET);
            }
            JSONObject response = new JSONObject();
            response.put("status", code);
            if (code >= 200 && code < 300) {
                response.put("data", body);

                response.put ("headers", request.headers());

                List<String> cookies = new ArrayList<String>();
                Iterator itr = request.headers().get("Set-Cookie").iterator();
                while(itr.hasNext()) {
                    cookies.add("\"" + itr.next().toString() + "\"");
                }

                response.put("cookies", cookies);

                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error (response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        } catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}
