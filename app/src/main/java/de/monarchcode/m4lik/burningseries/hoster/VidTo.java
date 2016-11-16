package de.monarchcode.m4lik.burningseries.hoster;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Malik on 04.11.2016.
 */
public class VidTo extends Hoster {
    protected static final Pattern idPattern;
    protected static final Pattern filenamePattern;
    protected static final Pattern hashPattern;
    protected static final Pattern geturlPattern;

    static {
        idPattern = Pattern.compile("<input type=\"hidden\" name=\"fid\" value=\"([0-9a-zA-Z-._ ]+)\">");
        filenamePattern = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"([0-9a-zA-Z-._ ]+)\">");
        hashPattern = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9-]+)\">");
        geturlPattern = Pattern.compile("',\\d+,\\d+,'");
    }

    public String get(String videoID) {
        String fullURL = "http://vidto.me/" + videoID;
        Log.d("BS", "Fetching video URL with id " + videoID + "...");
        Log.d("BS", "Updating database...");
        try {
            String GetReq = GetRequestString(fullURL);
            if (GetReq.equals("SOCKET_TIMEOUT")) {
                return "Oh damnit. Took too long...";
            }
            if (GetReq.contains("File was removed") || GetReq.contains("File Not Found") || GetReq.contains("AndStream404Error")) {
                return "The Skies are clear. Unusal.";
            }
            CharSequence GetRequest = GetRequestString(fullURL);
            Map dataArgs = new HashMap(7);
            Matcher filenameMatcher = filenamePattern.matcher(GetRequest);
            Matcher hashMatcher = hashPattern.matcher(GetRequest);
            if (!filenameMatcher.find() || !hashMatcher.find()) {
                return "Skies are clear. Unusal.";
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
                    Log.e("BS", "Error wile finding video URL");
                    return "We ain't found shit, Sir!";
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BS", "Error wile fetching video URL");
                return "We ain't found shit, Sir!x2";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BS", "Error wile getting video page");
            return "Whoops... The thing broke.";
        }
    }
}