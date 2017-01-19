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
    protected static final Pattern filenamePattern;
    protected static final Pattern hashPattern;
    protected static final Pattern geturlPattern;

    static {
        filenamePattern = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"(.*)\">");
        hashPattern = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9-]+)\">");
        geturlPattern = Pattern.compile("',\\d+,\\d+,'");
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
            Map dataArgs = new HashMap(7);
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

            Map refererArgs = new HashMap(1);
            refererArgs.put("Referer", fullURL);

            SystemClock.sleep(6500);

            try {
                String postReq = "eval(" + PostRequestString(fullURL, dataArgs, refererArgs).split("eval\\(")[1];
                Matcher test = geturlPattern.matcher(postReq);
                test.find();
                String p = postReq.split("return p\\}\\('")[1].split("',\\d+,\\d+")[0];
                String[] urlMatcher1 = test.group().split(",");
                Integer a = Integer.parseInt(urlMatcher1[1]);
                Integer c = Integer.parseInt(urlMatcher1[2]);
                String[] k = postReq.split("\\d+,\\d+,'")[1].split("'\\.split\\('")[0].split("\\|");
                for (Integer i = c - 1; i >= 0; i--) {
                    if (!k[i].isEmpty()) {
                        String currentID = Integer.toString(i, a);
                        p = p.replaceAll("\\b" + currentID + "\\b", k[i]);
                    }
                }
                try {
                    String quality = p.split("hd_default:\"")[1].split("\"")[0];
                    return p.split("label:\"" + quality + "\",file:\"")[1].split("\"")[0];
                } catch (Exception e) {
                    FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while finding video URL (" + fullURL + ")");
                    FirebaseCrash.report(e);
                    //We ain't found shit, Sir!
                    return "3";
                }
            } catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while fetching video URL (" + fullURL + ")");
                FirebaseCrash.report(e);
                return "4";
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, "HOSTER", "Error while getting video page (" + fullURL + ")");
            FirebaseCrash.report(e);
            //Whoops... The thing broke.
            return "5";
        }
    }
}