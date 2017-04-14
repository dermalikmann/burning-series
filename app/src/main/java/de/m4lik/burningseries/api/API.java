package de.m4lik.burningseries.api;

import android.util.Base64;

import org.json.JSONObject;

import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private String pubKey;
    private String verify;
    private APIInterface apiInterface;

    public API() {
        session = "";
        token = "sandbox";
        baseURL = "https://bs.to/";
        userAgent = "bs.android";
        pubKey = "PgfLa3cGNY5nDN3isibzuGsomSWspjAs";
        verify = "FSOGiKFVdaJmJH1axROzqcS8s8jhV3UT";

        buildRetrofit();
    }

    /**
     * Block encryption via an byte array
     *
     * @param byteArray
     * @return encryptedString
     */
    private static String encryption(byte[] byteArray) {
        char[] cArr = new char[(byteArray.length * 2)];
        for (int i = 0; i < byteArray.length; i++) {
            int i2 = byteArray[i] & 255;
            cArr[i * 2] = hexArray[i2 >>> 4];
            cArr[(i * 2) + 1] = hexArray[i2 & 15];
        }
        return new String(cArr);
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
        if (!uri.equals("login"))
            uri = uri + "?s=" + (getSession() == null ? "" : getSession());
        Long ts = System.currentTimeMillis() / 1000;
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("public_key", pubKey);
            jSONObject.put("timestamp", ts.intValue());
            jSONObject.put("hmac", doHMAC(ts, uri));
            setToken(Base64.encodeToString(jSONObject.toString().getBytes("UTF-8"), 2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the Hmac Key for the token and encrypts it
     *
     * @param timestamp
     * @param uri
     * @return encryptedHmacString
     */
    private String doHMAC(Long timestamp, String uri) {
        try {
            Key secretKeySpec = new SecretKeySpec(verify.getBytes("UTF-8"), "HmacSHA256");
            Mac instance = Mac.getInstance("HmacSHA256");
            instance.init(secretKeySpec);
            return encryption(instance.doFinal((timestamp.toString() + "/" + uri).getBytes("ASCII")));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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