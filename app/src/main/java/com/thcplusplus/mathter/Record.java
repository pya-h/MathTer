package com.thcplusplus.mathter;

import android.content.Context;

/**
 * Created by BodY on 11/9/2018.
 */

class Record {
    private int id,value,numberOfOperandsInStart, numberOfOperandsTrigger, operators, allTimePrizes, gameDuration;
    private float accuracy;

    private String name;
    public static final String[] SELECTED_OPERATORS = { "+ , -", "+ , - , *", "+ , - , * , /", "+ , - , * , / , ^","+ , - , * , / , ^ , √"};

    public long getId(){
        return this.id;
    }

    public void setId(int pId){
        this.id = pId;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int pVal){
        this.value = pVal;
    }

    public int getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(int pGameDuration) {
        this.gameDuration = pGameDuration;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float pAccuracy) {
        this.accuracy = pAccuracy;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String pName){
        this.name = pName;
    }

    public int getNumberOfOperandsInStart(){
        return this.numberOfOperandsInStart;
    }

    public void setNumberOfOperandsInStart(int pNum){
        this.numberOfOperandsInStart = pNum;
    }

    public int getNumberOfOperandsTrigger(){
        return this.numberOfOperandsTrigger;
    }

    public void setNumberOfOperandsTrigger(int pTrigger){
        this.numberOfOperandsTrigger = pTrigger;
    }

    public int getOperators(){
        return this.operators;
    }

    public String getSelectedOperators(){
        return SELECTED_OPERATORS[ (int)(getOperators()) - 2];
    }

    public void setOperators(int pOperators){
        this.operators = pOperators;
    }

    public int getAllTimePrizes() {
        return allTimePrizes;
    }

    public void setAllTimePrizes(int allTimePrizes) {
        this.allTimePrizes = allTimePrizes;
    }

    public Record(int pId, String pName, int pVal, int pNum, int pTrigger, int pOperators, int pAllTimePrizes, int pGameDuration, float pAccuray){
        setId(pId);
        setValue(pVal);
        setName(pName);
        setNumberOfOperandsInStart(pNum );
        setNumberOfOperandsTrigger(pTrigger);
        setOperators(pOperators);
        setAllTimePrizes(pAllTimePrizes);
        setGameDuration(pGameDuration);
        setAccuracy(pAccuray);
    }

    @Override
    public String toString() {
        return getName() + " { "  +  getValue() + " }";
    }

    public String getCompleteReport(Context context){
        return String.format(  context.getString(R.string.record_full_report) , getName(), getValue(), getNumberOfOperandsInStart() , getNumberOfOperandsTrigger() , getSelectedOperators() , getAllTimePrizes() , getGameDuration() , getAccuracy(), "%");
    }
}
