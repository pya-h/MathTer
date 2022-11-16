package com.thcplusplus.mathter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class SettingActivity extends AppCompatActivity {

    public final String PACKAGE_NAME = "com.thcplusplus.mathter";
    public final String INTENT_ERROR = "BazaarIntentError";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            if(getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException ex){
            Log.e(SettingActivity.class.getName(), ex.getMessage());
            Toast.makeText(this, ex.getMessage() , Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if(id == R.id.action_about){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.dialogTheme);
            alertDialogBuilder.setMessage( getString(R.string.about_me_text) );

            alertDialogBuilder.setPositiveButton(getString(R.string.dialog_send_comment_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startMarketIntent(Intent.ACTION_VIEW,"myket://comment?id=");
                }
            });

            alertDialogBuilder.setNegativeButton(getString(R.string.dialog_app_page_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startMarketIntent(Intent.ACTION_VIEW,"myket://details?id=");
                }
            });

            alertDialogBuilder.setNeutralButton(getString(R.string.alert_dialogbox_ok_button), null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(true);
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startMarketIntent(String mode,String contentProviderText){
        try {
            Intent intent = new Intent(mode);
            intent.setData(Uri.parse(contentProviderText + PACKAGE_NAME));
            startActivity(intent);
        }
        catch (Exception ex){
            Toast.makeText(this, getString(R.string.intent_not_found_error), Toast.LENGTH_LONG).show();
            Log.e(INTENT_ERROR, ex.getMessage());
        }
    }
}
