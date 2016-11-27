package de.monarchcode.m4lik.burningseries.api;

import android.util.Base64;

import org.json.JSONObject;

import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Malik (M4lik) on 29.09.2016.
 * @author M4lik, mm.malik.mann@gmail.com
 */

public class API {

    private String session;
    private String token;
    private String baseURL;
    private String userAgent;
    private String pubKey;
    private String verify;

    private static final char[] hexArray;

    private APIInterface apiInterface;

    static {
        hexArray = "0123456789abcdef".toCharArray();
    }

    public API() {
        session = "";
        token = "sandbox";
        baseURL = "https://bs.to/";
        userAgent = "bs.android";
        pubKey = "PgfLa3cGNY5nDN3isibzuGsomSWspjAs";
        verify = "FSOGiKFVdaJmJH1axROzqcS8s8jhV3UT";

        buildRetrofit();
    }

    public String getSession() {
        return session;
    }

    public String getToken() {
        return token;
    }

    public APIInterface getInterface() {
        return apiInterface;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setSession(String session) {
        this.session = session;
    }

     private void setToken(String token) {
        this.token = token;
    }

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

    private String doHMAC(Long l, String str) {
        try {
            Key secretKeySpec = new SecretKeySpec(verify.getBytes("UTF-8"), "HmacSHA256");
            Mac instance = Mac.getInstance("HmacSHA256");
            instance.init(secretKeySpec);
            return encryption(instance.doFinal((l.toString() + "/" + str).getBytes("ASCII")));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String encryption(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = hexArray[i2 >>> 4];
            cArr[(i * 2) + 1] = hexArray[i2 & 15];
        }
        return new String(cArr);
    }
}
