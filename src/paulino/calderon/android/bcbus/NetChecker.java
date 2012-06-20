package paulino.calderon.android.bcbus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetChecker {
    public static boolean isOnline(Context con) {
        //ConnectivityManager is used to check available network(s)
        ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null){
                //no network is available
                return false;
        } else {
                //at least one type of network is available
                return true;
        }
}

public static enum NetStatus {
        NONET, FAULTYURL, NETOK, CONN_MISSING
}

public static NetStatus checkNet(Context con) {
        // is there an active connection to any network?
        if (isOnline(con) == false) {
                return NetStatus.CONN_MISSING;
        }

        // is the server reachable?
        try {
                URL url = new URL("http://helloandroid.com");

                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(1000 * 5); // Timeout is in seconds
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                        //http response is OK
                        return NetStatus.NETOK;
                }
        } catch (MalformedURLException e1) {
                return NetStatus.FAULTYURL;
        } catch (IOException e) {
                return NetStatus.NONET;
        }
        return NetStatus.NETOK;
}
}
