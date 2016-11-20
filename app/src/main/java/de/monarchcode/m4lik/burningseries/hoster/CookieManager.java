package de.monarchcode.m4lik.burningseries.hoster;

/**
 * Created by Malik on 03.11.2016.
 */

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class CookieManager {
    private Map map;
    private DateFormat b;

    public CookieManager() {
        this.map = new HashMap();
        this.b = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z", Locale.US);
    }

    public void storeCookies(URLConnection uRLConnection) {
        Map map;
        String a = a(uRLConnection.getURL().getHost());
        if (this.map.containsKey(a)) {
            map = (Map) this.map.get(a);
        } else {
            map = new HashMap();
            this.map.put(a, map);
        }
        int i = 1;
        while (true) {
            String headerFieldKey = uRLConnection.getHeaderFieldKey(i);
            if (headerFieldKey != null) {
                if (headerFieldKey.equalsIgnoreCase("Set-Cookie")) {
                    Map hashMap = new HashMap();
                    StringTokenizer stringTokenizer = new StringTokenizer(uRLConnection.getHeaderField(i), ";");
                    if (stringTokenizer.hasMoreTokens()) {
                        String nextToken = stringTokenizer.nextToken();
                        String substring = nextToken.substring(0, nextToken.indexOf(61));
                        nextToken = nextToken.substring(nextToken.indexOf(61) + 1, nextToken.length());
                        map.put(substring, hashMap);
                        hashMap.put(substring, nextToken);
                    }
                    while (stringTokenizer.hasMoreTokens()) {
                        String[] split = stringTokenizer.nextToken().split("\\" + String.valueOf('='));
                        if (split.length >= 2) {
                            hashMap.put(split[0].toLowerCase(), split[1]);
                        }
                    }
                }
                i++;
            } else {
                return;
            }
        }
    }

    public void setCookies(URLConnection uRLConnection) {
        URL url = uRLConnection.getURL();
        String a = a(url.getHost());
        String path = url.getPath();
        Map map = (Map) this.map.get(a);
        if (map != null) {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                a = (String) it.next();
                Map map2 = (Map) map.get(a);
                if (a((String) map2.get("path"), path) && b((String) map2.get("expires"))) {
                    stringBuilder.append(a);
                    stringBuilder.append("=");
                    stringBuilder.append((String) map2.get(a));
                    if (it.hasNext()) {
                        stringBuilder.append("; ");
                    }
                }
            }
            try {
                uRLConnection.setRequestProperty("Cookie", stringBuilder.toString());
            } catch (Exception e) {
                //throw new IOException("Illegal State! Cookies cannot be set on map URLConnection that is already connected. Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
            }
        }
    }

    private String a(String str) {
        if (str.indexOf(46) != str.lastIndexOf(46)) {
            return str.substring(str.indexOf(46) + 1);
        }
        return str;
    }

    private boolean b(String str) {
        if (str == null) {
            return true;
        }
        try {
            if (new Date().compareTo(this.b.parse(str)) > 0) {
                return false;
            }
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean a(String str, String str2) {
        if (str == null || str.equals("/") || str2.regionMatches(0, str, 0, str.length())) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.map.toString();
    }
}