package de.m4lik.monarchcode.burningseries.api;

import android.util.Base64;

import org.json.JSONObject;

import java.io.IOException;
import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Malik on 29.09.2016.
 */

public class API {

    private String session;
    private String token;
    private String baseURL;
    private String userAgent;
    private String pubKey;
    private String verify;

    protected static final char[] hexArray;

    private Retrofit retrofit;
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

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public APIInterface getApiInterface() {
        return apiInterface;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void buildRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(APIInterface.class);
    }

    /*private HttpURLConnection makeUrlConnection(String methode, String uri, String session) {
        try {
            if (session != null) {
                uri = uri + "?s=" + session;
            }
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(apiURL + uri).openConnection();
            httpURLConnection.setRequestMethod(methode);
            httpURLConnection.setRequestProperty("BS-Token", generateToken(uri));
            httpURLConnection.setRequestProperty("User-Agent", "bs.android");
            return httpURLConnection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public void generateToken(String uri) {
        generateToken(uri, false);
    }

    public void generateToken(String uri, Boolean sb) {
        if (sb) { setToken("sandbox"); return; }
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
            return encryption(instance.doFinal((l.toString() + "/" +  str).getBytes("ASCII")));
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

    /*private JsonReader getJsonObj(String uri, String session) {
        try {
            return new JsonReader(new InputStreamReader(makeUrlConnection("GET", uri, session).getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonReader getLoginJsonObj(String uri, String session, HashMap<String, String> hashMap) {
        try {
            HttpURLConnection connection = makeUrlConnection("POST", uri, session);
            connection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            String str3 = "";
            int i = 0;
            for (Map.Entry entry : hashMap.entrySet()) {
                str3 = str3 + (i != 0 ? "&" : "") + URLEncoder.encode((String) entry.getKey(), "UTF-8") + "=" + URLEncoder.encode((String) entry.getValue(), "UTF-8");
                i++;
            }
            dataOutputStream.writeBytes(str3);
            dataOutputStream.flush();
            dataOutputStream.close();
            return new JsonReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public GenreMap getSeriesList() {
        return (GenreMap) gson.fromJson(getJsonObj("series:genre", this.session), (Type) GenreMap.class);
    }

    public SeasonObj getSeason(int i, int i2) {
        return (SeasonObj) gson.fromJson(getJsonObj("series/" + Integer.toString(i) + "/" + Integer.toString(i2), this.session), (Type) SeasonObj.class);
    }

    public EpisodeObj getEpisodes(int i, int i2, int i3) {
        return (EpisodeObj) gson.fromJson(getJsonObj("series/" + Integer.toString(i) + "/" + Integer.toString(i2) + "/" + Integer.toString(i3), this.session), (Type) EpisodeObj.class);
    }

    public LoginObj login(String str, String str2) {
        HashMap hashMap = new HashMap(2);
        hashMap.put("login[user]", str);
        hashMap.put("login[pass]", str2);
        return (LoginObj) gson.fromJson(getLoginJsonObj("login", this.session, hashMap), (Type) LoginObj.class);
    }

    public LinkObj watch(int i) {
        return (LinkObj) gson.fromJson(getJsonObj("watch/" + Integer.toString(i), this.session), (Type) LinkObj.class);
    }

    public Unwatch unwatch(int i) {
        return (Unwatch) gson.fromJson(getJsonObj("unwatch/" + Integer.toString(i), this.session), (Type) Unwatch.class);
    }

    public Version version() {
        return (Version) gson.fromJson(getJsonObj("version/android", this.session), (Type) Version.class);
    }

    public Session session() {
        return (Session) gson.fromJson(getJsonObj("user/session", this.session), (Type) Session.class);
    }

    public UserFavorites favorites() {
        return UserFavorites.fromJson(getJsonObj("user/series", this.session));
    }

    public void pushFavorites(UserFavorites userFavorites) {
        getJsonObj("user/series/set/" + userFavorites.toList(), this.session);
    }*/
}
