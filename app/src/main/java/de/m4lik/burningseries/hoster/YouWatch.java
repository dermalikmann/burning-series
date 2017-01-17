package de.m4lik.burningseries.hoster;

import android.os.SystemClock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Malik on 03.11.2016
 * @author Malik Mann
 */

public class YouWatch extends Hoster {
    private static final Pattern a;
    private static final Pattern b;
    private static final Pattern c;

    static {
        a = Pattern.compile("<input type=\"hidden\" name=\"hash\" value=\"([a-zA-Z0-9]+)\">");
        b = Pattern.compile("<input type=\"hidden\" name=\"fname\" value=\"([^\"]+)\">");
        c = Pattern.compile("([a-zA-Z0-9]+)\\|(\\d+)\\|fs(\\d+)\\|setup\\|flvplayer'\\.split\\('|'\\),0,\\{\\}\\)\\)");
    }

    public String get(String videoID) {
        String fullURL = "http://youwatch.org/" + videoID;
        try {
            CharSequence GetRequest = GetRequestString(fullURL);
            Matcher matcher = a.matcher(GetRequest);
            Matcher matcher2 = b.matcher(GetRequest);
            if (!matcher.find() || !matcher2.find()) {
                return "Skies are clear. Unusal.";
            }
            String group = matcher.group(1);
            String group2 = matcher2.group(1);
            Map dataArgs = new HashMap(7);
            dataArgs.put("iamhuman", "Slow Download");
            dataArgs.put("hash", group);
            dataArgs.put("referer", "");
            dataArgs.put("fname", group2);
            dataArgs.put("id", videoID);
            dataArgs.put("usr_login", "");
            dataArgs.put("op", "download1");
            Map refererArgs = new HashMap(1);
            refererArgs.put("Referer", fullURL);
            SystemClock.sleep(10000);
            try {
                Matcher matcher3 = c.matcher(PostRequestString(fullURL, dataArgs, refererArgs));
                if (matcher3.find()) {
                    return "http://fs" + matcher3.group(3) + ".youwatch.org:" + matcher3.group(2) + "/" + matcher3.group(1) + "/video.mp4";
                }
                return "We ain't found shit Sir!";
            } catch (IOException e) {
                return "We ain't found shit Sir!x2";
            }
        } catch (IOException e2) {
            return "Whoops... The thing broke.";
        }
    }
}
