package com.hrgirdowner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by adminsitrator on 31/10/2015.
 */
public class ConnectionDetector
{
    private Context _context;
    private String url;

    public ConnectionDetector(Context context)
    {
        this._context = context;
    }
    
    public String geturl()
    {
         //String url = "https://";//live
        String url = "http://";
        return url;
    }

    public String get_infogird_url()
    {
         //String url = "infogird.hrgird.com";//live
        //String url = "infogird.gogird.com";
        String url = "hrsaas.safegird.com";
        return url;
    }

    public String GetIpaddress()
    {
        WifiManager wm = (WifiManager)_context. getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i("IPAddress", "***** IP=" + ip);

                        return ip;
                    }
                }
            }
        }
        catch (Exception ex)
        {
        }
        return null;
    }

}