package com.thcplusplus.mathter;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private Boolean settingChanged = true;
    private SharedPreferences prfHandler;
    private final String SETTING_NUMBER_OF_OPERANDS_IN_GAMESTART = "settingNumberOfOperands",SETTING_NUMBER_OF_OPERANDS_TRIGGER = "settingNumberOfOperandsTrigger",
            SETTING_OPERATORS_TO_USE = "settingOperators",SETTING_FONT_SIZE="settingFontSizes";                                                                                                                                                                           ;
    MainActivityFragment gameFragment;
    SharedPreferences.OnSharedPreferenceChangeListener settingChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            settingChanged = true;
        }
    } ;

    @Override
    protected void onStart() {
        super.onStart();
        if (settingChanged) {

            final byte numberOfOperandsInGameStart = Byte.parseByte(prfHandler.getString(SETTING_NUMBER_OF_OPERANDS_IN_GAMESTART, "2")),
                    numberOfOperandsTrigger = Byte.parseByte(prfHandler.getString(SETTING_NUMBER_OF_OPERANDS_TRIGGER, "5")),
                    fontSize=Byte.parseByte(prfHandler.getString(SETTING_FONT_SIZE,getString(R.string.title_activity_records ) ) );
            final byte operatorsToUse = Byte.parseByte(prfHandler.getString(SETTING_OPERATORS_TO_USE, "2"));
            gameFragment.updateGameOptions( numberOfOperandsInGameStart, numberOfOperandsTrigger, operatorsToUse, fontSize);
            settingChanged = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gameFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.gameFragment);
        PreferenceManager.setDefaultValues(this,R.xml.game_settings,false);
        prfHandler = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this );
        prfHandler.registerOnSharedPreferenceChangeListener( settingChangeListener );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final long id = item.getItemId();
        if(gameFragment.gameIsRunning()){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this , R.style.dialogTheme);
            dialogBuilder.setMessage(getString(R.string.game_is_running_warning));
            dialogBuilder.setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    manageActionBarItemSelect(id);
                }
            });
            dialogBuilder.setNegativeButton(getString(R.string.answer_no) , null);
            AlertDialog dialog = dialogBuilder.create();
            dialog.setCancelable(true);
            dialog.show();
        }
        else{
            try {
                manageActionBarItemSelect(id);
            }
            catch (Exception ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void manageActionBarItemSelect(final long id){
        if (id == R.id.action_settings) {
            Intent settingActivityIntent = new Intent(this, SettingActivity.class);
            startActivity(settingActivityIntent);
        } else if (id == R.id.action_records) {
            Intent recordsActivityIntent = new Intent(MainActivity.this, RecordsActivity.class);
            recordsActivityIntent.putExtra(RecordsActivity.SHOW_INPUT, false);
            startActivity(recordsActivityIntent);
        }
    }
}
