package com.armyof2.docscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    public static int DOCFLAG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d("TAG", "onCreate: " + stringFromJNI());
    }

    public void onAdhrButtonClicked(View view){
        DOCFLAG = 1;
        Intent i = new Intent(this, PickImageActivity.class);
        startActivity(i);
    }

    public void onPanButtonClicked(View view){
        DOCFLAG = 2;
        Intent i = new Intent(this, PickImageActivity.class);
        startActivity(i);
    }

    public void onLicButtonClicked(View view){
        DOCFLAG = 3;
        Intent i = new Intent(this, PickImageActivity.class);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();*/
}
