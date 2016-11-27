package de.monarchcode.m4lik.burningseries.api;

import android.util.Log;

import java.io.IOException;

import de.monarchcode.m4lik.burningseries.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Malik on 25.11.2016
 * @author Malik Mann
 */

class ResponseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (BuildConfig.DEBUG) {


            MediaType contentType = response.body().contentType();
            String bodyString = response.body().string();

            Log.v("BSAPI", "URL: " + request.url().toString());
            Log.v("BSAPI", "Response: " + bodyString);

            ResponseBody body = ResponseBody.create(contentType, bodyString);
            return response.newBuilder().body(body).build();
        } else {
            return response;
        }
    }
}
