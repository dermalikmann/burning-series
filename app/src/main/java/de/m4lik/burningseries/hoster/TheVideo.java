package de.m4lik.burningseries.hoster;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TheVideo extends Hoster {
    protected static final Pattern filenamePattern;
    protected static final Pattern hashPattern;
    protected static final Pattern mpriPattern;
    protected static final Pattern vtPattern;
    protected static final Pattern hdPattern;
    protected static final Pattern sdPattern;
    protected static final Pattern ldPattern;
    protected static final Pattern vldPattern;

    static {
        filenamePattern = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"[a-zA-Z0-9-]+\">");
        hashPattern = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9-]+)\">");
        mpriPattern = Pattern.compile("='([a-zA-Z0-9]{22,26})'");
        vtPattern = Pattern.compile("([a-zA-Z0-9]{15,})");
        hdPattern = Pattern.compile("file\":\"(https://([a-zA-Z0-9/.:]+))\",\"label\":\"720p\"");
        sdPattern = Pattern.compile("file\":\"(https://([a-zA-Z0-9/.:]+))\",\"label\":\"480p\"");
        ldPattern = Pattern.compile("file\":\"(https://([a-zA-Z0-9/.:]+))\",\"label\":\"360p\"");
        vldPattern = Pattern.compile("file\":\"(https://([a-zA-Z0-9/.:]+))\",\"label\":\"240p\"");
    }

    public String get(String videoID) {
        String fullURL = "https://thevideo.me/" + videoID;
        try {
            CharSequence GetRequest = GetRequestString(fullURL);

            if (GetRequest.equals("SOCKET_TIMEOUT")) {
                //Oh damnit. Took too long...
                return "1";
            }

            Map<String, String> dataArgs = new HashMap<>(10);

            Matcher hashMatcher = hashPattern.matcher(GetRequest);

            Log.d("BSTV", GetRequest.toString());

            if (!hashMatcher.find()) {
                //The Skies are clear. Unusual.
                return "2";
            }

            //TODO: Redo the _vhash and gfk regexpatterns

            dataArgs.put("_vhash", GetRequest.toString().split("name: '_vhash', value: '")[1].split("'")[0]);
            dataArgs.put("gfk", GetRequest.toString().split("name: 'gfk', value: '")[1].split("'")[0]);
            dataArgs.put("op", "download1");
            dataArgs.put("usr_login", "");
            dataArgs.put("id", videoID);
            dataArgs.put("fname", GetRequest.toString().split("<input type=\"hidden\" name=\"fname\" value=\"")[1].split("\"")[0]);
            dataArgs.put("referer", "");
            dataArgs.put("hash", hashMatcher.group(1));
            dataArgs.put("inhu", "foff");
            dataArgs.put("imhuman", "");

            Map<String, String> refererArgs = new HashMap<>(1);
            refererArgs.put("Referer", fullURL);

            Thread.sleep(5000);

            String PostRequest = PostRequestString(fullURL, dataArgs, refererArgs);

            Matcher mpriMatcher = mpriPattern.matcher(PostRequest);
            mpriMatcher.find();
            String mpriKey = mpriMatcher.group(1);


            String videoTokenRequest = GetRequestString("https://thevideo.me/vsign/player/" + mpriKey);

            Matcher vtMatcher = vtPattern.matcher(videoTokenRequest);
            vtMatcher.find();
            String videoToken = vtMatcher.group(0);
            videoToken = "?direct=false&ua=1&vt=" + videoToken;

            try {
                Matcher urlMatcher = hdPattern.matcher(PostRequest);
                if (!urlMatcher.find()) {
                    urlMatcher = sdPattern.matcher(PostRequest);
                    if (!urlMatcher.find()) {
                        urlMatcher = ldPattern.matcher(PostRequest);
                        if (!urlMatcher.find()) {
                            urlMatcher = vldPattern.matcher(PostRequest);
                            if (!urlMatcher.find()) {
                                //We ain't found shit, Sir!
                                FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while finding video URL (" + fullURL + ")");
                                return "3";
                            }
                            return urlMatcher.group(1) + videoToken;
                        }
                        return urlMatcher.group(1) + videoToken;
                    }
                    return urlMatcher.group(1) + videoToken;
                }
                return urlMatcher.group(1) + videoToken;
            } catch (Exception e) {
                e.printStackTrace();
                FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while fetching video URL (" + fullURL + ")");
                return "4";
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while fetching video URL (" + fullURL + ")");
            return "1";
        }
    }
}
