package de.m4lik.burningseries.hoster;

import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Malik on 04.11.2016.
 */
class VidTo extends Hoster {

    protected static final Pattern hdPattern;
    protected static final Pattern sdPattern;
    protected static final Pattern ldPattern;
    protected static final Pattern vldPattern;
    private static final Pattern filenamePattern;
    private static final Pattern hashPattern;
    private static final Pattern getURLPattern;

    static {
        filenamePattern = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"(.*)\">");
        hashPattern = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9-]+)\">");
        getURLPattern = Pattern.compile("',\\d+,\\d+,'");
        hdPattern = Pattern.compile("file:\"(http://([a-zA-Z0-9/.:]+))\",label:\"720p\"");
        sdPattern = Pattern.compile("file:\"(http://([a-zA-Z0-9/.:]+))\",label:\"480p\"");
        ldPattern = Pattern.compile("file:\"(http://([a-zA-Z0-9/.:]+))\",label:\"360p\"");
        vldPattern = Pattern.compile("file:\"(http://([a-zA-Z0-9/.:]+))\",label:\"240p\"");
    }

    public String get(String videoID) {
        String fullURL = "http://vidto.me/" + videoID;
        try {
            String GetReq = GetRequestString(fullURL);
            if (GetReq.equals("SOCKET_TIMEOUT")) {
                //Oh damnit. Took too long...
                return "1";
            }
            if (GetReq.contains("File was removed") || GetReq.contains("File Not Found")) {
                //The Skies are clear. Unusual.
                return "2";
            }
            CharSequence GetRequest = GetRequestString(fullURL);
            Map<String, String> dataArgs = new HashMap<>(7);
            Matcher filenameMatcher = filenamePattern.matcher(GetRequest);
            Matcher hashMatcher = hashPattern.matcher(GetRequest);
            if (!filenameMatcher.find() || !hashMatcher.find()) {
                //"Skies are clear. Unusual.
                return "2";
            }
            dataArgs.put("op", "download1");
            dataArgs.put("usr_login", "");
            dataArgs.put("id", videoID.replace(".html", ""));
            dataArgs.put("fname", filenameMatcher.group(1));
            dataArgs.put("referer", "");
            dataArgs.put("hash", hashMatcher.group(1));
            dataArgs.put("imhuman", "Proceed to video");

            Map<String, String> refererArgs = new HashMap<>(1);
            refererArgs.put("Referer", fullURL);

            SystemClock.sleep(6500);

            String postReq = PostRequestString(fullURL, dataArgs, refererArgs);
            Log.v("BSHOSTER", postReq);

            try {
                Matcher urlMatcher = hdPattern.matcher(postReq);
                if (!urlMatcher.find()) {
                    urlMatcher = sdPattern.matcher(postReq);
                    if (!urlMatcher.find()) {
                        urlMatcher = ldPattern.matcher(postReq);
                        if (!urlMatcher.find()) {
                            urlMatcher = vldPattern.matcher(postReq);
                            if (!urlMatcher.find()) {
                                //We ain't found shit, Sir!
                                FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while finding video URL (" + fullURL + ")");
                                return "3";
                            }
                            return urlMatcher.group(1);
                        }
                        return urlMatcher.group(1);
                    }
                    return urlMatcher.group(1);
                }
                return urlMatcher.group(1);

            } catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while fetching video URL (" + fullURL + ")");
                e.printStackTrace();
                FirebaseCrash.report(e);
                return "4";
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while getting video page (" + fullURL + ")");
            e.printStackTrace();
            FirebaseCrash.report(e);
            //Whoops... The thing broke.
            return "5";
        }
    }
}