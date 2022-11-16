package com.thcplusplus.mathter;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import static android.view.View.GONE;

public class RecordsActivity extends ListActivity {

    private EditText edtRecordName;
    private LinearLayout lytRecords,lytOptions;
    private CheckBox checkSorted, checkAscending;
    private DatabaseInterface databaseInterface;
    static final String SHOW_INPUT = "bundleShowInput", NUMBER_OF_CORRECT_ANSWERS = "bundleCorrectAnswers", NUMBER_OF_OPERANDS = "bundleOperandsNumber", NUMBER_OF_OPERANDS_TRIGGER = "bundleTriggerCount" ,
        OPERATORS_TO_USE = "bundleOperatorsToUse", ALL_TIME_PRIZES = "PRIZES", GAME_DURATION = "GAME_DURATION", ACCURACY = "ACCURACY";
    private Animation comeFromRightAnimation,goToRight, comeFromDownAnimation;


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            Record selectedRecord = (Record) getListAdapter().getItem(position);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.dialogTheme);
            alertDialogBuilder.setMessage( selectedRecord.getCompleteReport(this) );
            alertDialogBuilder.setPositiveButton(getString(R.string.alert_dialogbox_ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(true);
            alertDialog.show();
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseInterface.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        try{
            if(getActionBar() != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
            final Intent intent = getIntent();
            if( !intent.getBooleanExtra(SHOW_INPUT, false) ){
                LinearLayout lytInput = (LinearLayout) findViewById(R.id.lytInput);
                lytInput.setVisibility(GONE);
            }

            comeFromRightAnimation = AnimationUtils.loadAnimation(this, R.anim.come_fromright_animation);
            comeFromDownAnimation = AnimationUtils.loadAnimation(this, R.anim.come_from_down);

            goToRight = AnimationUtils.loadAnimation(this, R.anim.go_to_right);

            Button btnSaveRecord,btnDiscard;

            lytRecords = (LinearLayout) findViewById(R.id.lytRecords);
            lytOptions = (LinearLayout) findViewById(R.id.lytOptions);

            checkSorted = (CheckBox) findViewById(R.id.check_sort);
            checkAscending = (CheckBox) findViewById(R.id.check_ascending);
            edtRecordName = (EditText) findViewById(R.id.edittext_record_name);
            btnSaveRecord = (Button) findViewById(R.id.button_save);
            btnDiscard = (Button) findViewById(R.id.button_discard);

            lytOptions.startAnimation(comeFromDownAnimation);
            lytRecords.startAnimation(comeFromDownAnimation);

            databaseInterface = new DatabaseInterface(this);

            setTitle(getString(R.string.title_activity_records));
            checkAscending.setVisibility(View.GONE);
            databaseInterface.open();
            List<Record> records = databaseInterface.getAllRecords(false,false);
            ArrayAdapter<Record> adapter = new ArrayAdapter<Record>(this, R.layout.list_text_view, records);

            setListAdapter(adapter);

            btnSaveRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseInterface.createRecord(edtRecordName.getText().toString(), intent.getIntExtra(NUMBER_OF_CORRECT_ANSWERS, 0), intent.getByteExtra(NUMBER_OF_OPERANDS,(byte)2),
                            intent.getByteExtra(NUMBER_OF_OPERANDS_TRIGGER, (byte)5), intent.getByteExtra(OPERATORS_TO_USE, (byte)2 ),
                            intent.getIntExtra(ALL_TIME_PRIZES, 0), intent.getIntExtra(GAME_DURATION, 60), intent.getFloatExtra(ACCURACY, 0.0f));
                    databaseInterface.close();

                    finish();
                }
            });

            btnDiscard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseInterface.close();
                    finish();
                }
            });

            checkSorted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    List<Record> records = databaseInterface.getAllRecords(isChecked, checkAscending.isChecked());
                    ArrayAdapter<Record> adapter = new ArrayAdapter<Record>(RecordsActivity.this,R.layout.list_text_view, records);
                    setListAdapter(adapter);
                    if(isChecked) {
                        checkAscending.setVisibility(View.VISIBLE);
                        checkAscending.startAnimation(comeFromRightAnimation);
                    }
                    else{
                        checkAscending.startAnimation(goToRight);
                        checkAscending.setVisibility(View.GONE);
                    }
                }
            });

            checkAscending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(checkSorted.isChecked()){
                        List<Record> records = databaseInterface.getAllRecords(true, isChecked);
                        ArrayAdapter<Record> adapter = new ArrayAdapter<Record>(RecordsActivity.this, R.layout.list_text_view, records);
                        setListAdapter(adapter);
                    }
                }
            });
        }
        catch(NullPointerException ex){
            Log.e(RecordsActivity.class.getName() + "; " + "NullPointer: ", ex.getMessage() );
            Toast.makeText(this, ex.getMessage() , Toast.LENGTH_LONG).show();
        }
        catch (SQLException ex){
            Log.e(DatabaseInterface.class.getName(), ex.getMessage() );
            Toast.makeText(this, ex.getMessage() , Toast.LENGTH_LONG).show();
        }
        catch (Exception ex){
            Log.e(RecordsActivity.class.getName()+ "; " + "OtherExceptions: ", ex.getMessage() );
            Toast.makeText(this, ex.getMessage() , Toast.LENGTH_LONG).show();
        }

    }
}
