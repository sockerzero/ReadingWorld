package com.dnd.readingworld.Init;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by Asus on 11/19/2016.
 */

public class Init {
    public static final String URL_STORAGE_REFERENCE = "gs://readingworld-1b158.appspot.com";
    public static final String FOLDER_STORAGE_IMG = "images";

    public static void initToast(Context c, String message){
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }

    public  static boolean CheckConnect(Context context) {
        boolean check;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        check = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return check;
    }



}
