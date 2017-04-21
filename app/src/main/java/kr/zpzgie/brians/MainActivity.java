package kr.zpzgie.brians;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import kr.zpzgie.libloc.GpsManager;

import static kr.zpzgie.libloc.GpsManager.MSG_REQUEST_PERMISSION;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





    }

    @Override
    public void onResume(){
        super.onResume();
        GpsManager.getInstance(this);
//         new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                GpsManager.getInstance(MainActivity.this);
//
//            }
//        }, 10*1000);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();;

        GpsManager manager = GpsManager.getInstance();
        if (manager != null)
            manager.close();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MSG_REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }

        }
    }
}
