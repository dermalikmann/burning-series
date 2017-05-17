package de.m4lik.burningseries.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Object class for the API. Used to authorize and starting the API calls.
 *
 * @author Malik Mann
 */

public class API {

    private static final char[] hexArray;

    static {
        hexArray = "0123456789abcdef".toCharArray();
    }

    private String session;
    private String token;
    private String baseURL;
    private String userAgent;
    private APIInterface apiInterface;

    public API() {
        session = "";
        token = "";
        baseURL = "https://bs.to/";
        userAgent = "bs.android";

        buildRetrofit();
    }

    /**
     * Creates instance of an Retrofit object.
     * Additionally implements an interceptor via an OkHTTPClient.
     */
    private void buildRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ResponseInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiInterface = retrofit.create(APIInterface.class);
    }

    /**
     * Generates a valid token for the current API request.
     * URI has to be without session.
     *
     * @param uri
     */
    public void generateToken(String uri) {
        this.token = "sandbox";
    }

    /*
     * Getter & Setter
     */

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    public APIInterface getInterface() {
        return apiInterface;
    }

    public String getUserAgent() {
        return userAgent;
    }
}