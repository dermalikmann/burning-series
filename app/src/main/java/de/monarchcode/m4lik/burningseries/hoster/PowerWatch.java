package de.monarchcode.m4lik.burningseries.hoster;

import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerWatch extends Hoster {
    protected static final Pattern filenamePattern;
    protected static final Pattern hashPattern;
    protected static final Pattern urlPattern;

    static {
        filenamePattern = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"([0-9a-zA-Z-._ ]+)\">");
        hashPattern = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9-]+)\">");
        urlPattern = Pattern.compile("sources: \\[\\{file:\"(http://([a-zA-Z0-9/.]+))\",label:");
    }

    public String get(String videoID) {
        String fullURL = "http://powerwatch.pw/" + videoID;
        try {
            CharSequence GetRequest = GetRequestString(fullURL);
            Map dataArgs = new HashMap(7);
            Matcher matcher = filenamePattern.matcher(GetRequest);
            Matcher matcher2 = hashPattern.matcher(GetRequest);
            if (!matcher.find() || !matcher2.find()) {
                return "Skies are clear. Unusal.";
            }
            dataArgs.put("fname", matcher.group(1));
            dataArgs.put("hash", matcher2.group(1));
            dataArgs.put("op", "download1");
            dataArgs.put("usr_login", "");
            dataArgs.put("id", videoID);
            dataArgs.put("referer", "");
            dataArgs.put("imhuman", "");
            Map refererArgs = new HashMap(1);
            refererArgs.put("Referer", fullURL);
            SystemClock.sleep(5000);
            try {
                Matcher matcher3 = urlPattern.matcher(PostRequestString(fullURL, dataArgs, refererArgs));
                if (matcher3.find()) {
                    return matcher3.group(1);
                }
                return "We ain't found shit Sir!";
            } catch (Exception e) {
                return "We ain't found shit Sir!x2";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Whoops... The thing broke.";
        }
    }
}
