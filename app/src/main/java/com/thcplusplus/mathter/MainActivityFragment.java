package com.thcplusplus.mathter;


import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;

import java.util.Date;


public class MainActivityFragment extends Fragment {

    // widgets variable
    private TextView equationTextView,timePrizeTextView,allTimePrizesTextView,noteTextView;
    private LinearLayout lytSecondPart;
    private EditText answerEditText;
    private Button checkButton,nextButton,timerButton;
    private ProgressBar timeLeftProgress;
    private Handler delayHandler;
    private Date startTime,endTime;
    // game state variables
    private int allAnsweres = 0,numberOfCorrectAnswers = 0, allTimePrizes = 0, currentAnswer = 0, currentQuestionTimePrize = 0, currentQuestionNumber = 0;
    private Boolean answerIsTrue = false,gameIsStarted = false,secondPartAnimationPlayed = false;
    private String currentEquation;

    // game setting variables
    private byte numberOfOperands = 2,numberOfOperandsInGameStart = 2,numberOfOperandsTrigger = 5, operatorsToUse = 3;
    // time variables
    private final byte standardTime = 100;
    private int  remainingTime = 0, maxTimeLeft = standardTime;

    // game animations
    private Animation timePrizeAnimation,equationToLeftAnimation, equationFromRightAnimation, comeFromDownAnimation;

    // constnts for equation string making and calculations
    private final String[] OPERATORS = new String[] {" + ", " - ", " * ", " / ", " ^ ", " √"  };
    private final byte[] TIME_PRIZES = new byte[] {1,2,3,4,6,5};
    private final int MAX_NUMBER = 100, SPECIAL_MAX_NUMBER = 20;
    private final byte MAX_PRIZE = 40;
    private final byte PLUS_OP_INDEX = 0,MINUS_OP_INDEX = 1, MULTIPLY_OP_INDEX = 2, DEVIDE_OP_INDEX = 3,POWER_OP_INDEX = 4, SQRT_OP_INDEX = 5;
    private final short WRONG_ANSWER_TIME_LOSS = -5;
    private final byte multiplyToZeroMinTime = 3,multiplyToZeroMaxTime = 7, multiplyToZeroDenominator = 2;

    // game managment constants
    private final int RECORDS_ACTIVITY_REQUEST_CODE = 1;
    private final String ANSWER_EDITTEXT_ERROR_HANDLER = "AnswerBoxErrorHandler",EQUATION_CREATOR_ERROR_HANDLER = "EqCreatorErrorHandler";

    // timer & randomizer
    private CountDownTimer timer = null;
    private SecureRandom randomizer;


    // setting change methods
    private void updateFonts(byte fontSize)
    {
        equationTextView.setTextSize(fontSize);// edit this , send to better place for run
        answerEditText.setTextSize(fontSize);
        timePrizeTextView.setTextSize(fontSize - 5);
        allTimePrizesTextView.setTextSize(fontSize - 5);

    }
    public void updateGameOptions(byte pNumberOfOperandsInGameStart,byte pNumberOfOperandsTrigger,byte pOperatorsToUse,byte fontSize)
    {
        numberOfOperandsInGameStart = pNumberOfOperandsInGameStart;
        numberOfOperandsTrigger = pNumberOfOperandsTrigger;
        operatorsToUse = pOperatorsToUse;
        updateFonts(fontSize);
        resetGame();
    }

    // timer managment during game
    private void updateTimer(int valueInSec) {
        if(timer != null)
            timer.cancel();

        // progress bar max value change, this is for times that the remaining time passes the standardTime value
        if(valueInSec > maxTimeLeft){
            maxTimeLeft = valueInSec;
            timeLeftProgress.setMax(maxTimeLeft);
            timeLeftProgress.setProgress(maxTimeLeft);
        }

        // update the timer value and game interface in parts that shows timer
        timer = new CountDownTimer((valueInSec + 1 ) * 1000, 1000) {
            @Override
            public void onTick(long remainingTimeInMilis) {
                remainingTime = (int)(remainingTimeInMilis / 1000);
                timerButton.setText(String.format(getString(R.string.numeric_view_text), remainingTime));
                timeLeftProgress.setProgress(remainingTime);
            }

            // here are the events that must happen when timer is 0 ( game ends )
            @Override
            public void onFinish() {
                equationTextView.setText(getString(R.string.times_up_msg));
                checkButton.setEnabled(false);
                nextButton.setEnabled(false);
                // delay 2 sec and then start the records activity with proper values , sent via Bundle
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        endTime = new Date();
                        Intent recordsActivityIntent = new Intent(getContext(), RecordsActivity.class);

                        recordsActivityIntent.putExtra(RecordsActivity.SHOW_INPUT, true);
                        recordsActivityIntent.putExtra(RecordsActivity.NUMBER_OF_CORRECT_ANSWERS, numberOfCorrectAnswers);
                        recordsActivityIntent.putExtra(RecordsActivity.NUMBER_OF_OPERANDS,numberOfOperandsInGameStart);
                        recordsActivityIntent.putExtra(RecordsActivity.NUMBER_OF_OPERANDS_TRIGGER, numberOfOperandsTrigger);
                        recordsActivityIntent.putExtra(RecordsActivity.OPERATORS_TO_USE, operatorsToUse);
                        recordsActivityIntent.putExtra(RecordsActivity.ALL_TIME_PRIZES, allTimePrizes);
                        recordsActivityIntent.putExtra(RecordsActivity.GAME_DURATION, (int)( (endTime.getTime() - startTime.getTime())/1000 ));
                        recordsActivityIntent.putExtra(RecordsActivity.ACCURACY, ( (float)numberOfCorrectAnswers / (float)allAnsweres ) * 100.0f);

                        startActivityForResult(recordsActivityIntent, RECORDS_ACTIVITY_REQUEST_CODE);
                    }
                } , 2000 );

            }
        }.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //reset the game when the RecordsActivity stops
        if(requestCode == RECORDS_ACTIVITY_REQUEST_CODE )
            resetGame();
    }



    private void initAnimations() {
        // initialize the 3 animation used in the game , consisting  object init and adding listeners
        timePrizeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.prize_animation);
        timePrizeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //if the answer was true start the equation_to_left animation exatly the time when this animation starts
                if(answerIsTrue)
                    equationTextView.startAnimation(equationToLeftAnimation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // update the current prize textview after the animations ends
                allTimePrizesTextView.setText( String.format(getString(R.string.numeric_view_text),allTimePrizes) );
                timePrizeTextView.setTextColor(Color.CYAN);
                timePrizeTextView.setText(String.format(getString(R.string.time_prize_textview_positive_text), currentQuestionTimePrize));

                if(remainingTime > 0)
                    checkButton.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        equationToLeftAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.go_toleft_animation);
        equationToLeftAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //after the equation_to_left animation ends , this animation must start immediatly
                equationTextView.startAnimation(equationFromRightAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        equationFromRightAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.come_fromright_animation);
        equationFromRightAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // to_left and from_right animation , send the old values o left and bring new question from right, sso before the text shows up from right , its values must be updated
                createTheNextQuestion();
                updateGameUI();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        comeFromDownAnimation = AnimationUtils.loadAnimation(getActivity() ,R.anim.come_from_down);
        comeFromDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if( !secondPartAnimationPlayed ) {
                    nextButton.setVisibility(View.VISIBLE);
                    checkButton.setVisibility(View.VISIBLE);
                    timePrizeTextView.setVisibility(View.VISIBLE);
                    allTimePrizesTextView.setVisibility(View.VISIBLE);

                    timerButton.setText(String.format(getString(R.string.numeric_view_text), remainingTime));
                    checkButton.setText(String.format(getString(R.string.check_button_text), 0));
                    nextButton.setText(String.format(getString(R.string.next_button_text) , 0));

                    lytSecondPart.startAnimation(comeFromDownAnimation);
                    secondPartAnimationPlayed = true;
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(secondPartAnimationPlayed) {
                    updateTimer(standardTime);
                    answerEditText.setEnabled(true);
                    answerEditText.setText("");
                    answerEditText.setHint(getString(R.string.answer_edittext_hint));
                    createTheNextQuestion();
                    updateGameUI();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private byte getExtraPrize(int number){
        // calculate the extra time for the answer by calculating its digits number, cause as the answer is larger , calculation is much harder
        byte digits = 0;
        number = Math.abs(number);
        for(digits = 0; number > 0; number /= 10, digits++ );
        return (byte)(digits / 2);
    }


    private void updatePrizes(int prize){
        // update alltimePrize and currenttime prize values and update UI and show proper animations
        remainingTime += prize;
        allTimePrizes += prize;
        timePrizeTextView.setTextColor(prize >= 0 ? Color.CYAN : Color.RED );
        timePrizeTextView.startAnimation(timePrizeAnimation);
        allTimePrizesTextView.setTextColor(prize >= 0 ? Color.GREEN : Color.RED );

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetGame(){
        // reset timer , game state variables, widgets and game interface and so on
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        remainingTime = 0;
        timer = null;
        timerButton.setText(getString(R.string.timer_button_hint));
        timeLeftProgress.setProgress(0);

        timePrizeTextView.setVisibility(View.GONE);
        allTimePrizesTextView.setVisibility(View.GONE);
        currentQuestionTimePrize = 0;
        allTimePrizes = 0;
        allTimePrizesTextView.setText( String.format(getString(R.string.numeric_view_text),0) );

        noteTextView.setVisibility(View.VISIBLE);

        answerEditText.setText("");
        answerEditText.setEnabled(false);
        answerEditText.setHint(getString(R.string.answer_edittext_desabled_text));
        equationTextView.setText("");

        checkButton.setVisibility(View.GONE);
        checkButton.setEnabled(true);
        nextButton.setEnabled(true);
        nextButton.setVisibility(View.GONE);

        secondPartAnimationPlayed = false;
        gameIsStarted = false;
        currentQuestionNumber = 0;
        numberOfCorrectAnswers = 0;
        allAnsweres = 0;
        numberOfOperands = numberOfOperandsInGameStart; // change this asshole //

    }

    private void startGame(){

        noteTextView.setVisibility(View.GONE);
        answerEditText.startAnimation(comeFromDownAnimation);
        timeLeftProgress.startAnimation(comeFromDownAnimation);
        equationTextView.startAnimation(comeFromDownAnimation);

        gameIsStarted = true;
        startTime = new Date();
    }
    private void updateGameUI()
    {
        // check if the number of questions has passed the trigger, one operand must be added
        if(currentQuestionNumber % numberOfOperandsTrigger == 0)
            numberOfOperands++;

        // update colors , textview texts, buttons text and so on
        equationTextView.setTextColor(Color.rgb(randomizer.nextInt(200), randomizer.nextInt(200), randomizer.nextInt(200)));
        equationTextView.setText(currentEquation);
        answerEditText.setText("");
        nextButton.setText(String.format(getString(R.string.next_button_text),currentQuestionNumber));
        checkButton.setText(String.format(getString(R.string.check_button_text),numberOfCorrectAnswers));
        timePrizeTextView.setTextColor(Color.BLUE);
        timePrizeTextView.setText(String.format(getString(R.string.time_prize_textview_positive_text), currentQuestionTimePrize));

    }

    private void createTheNextQuestion() {
        int secondOperand, opIndex;
        Boolean hasSqrt = false, multipliedToZero = false;
        currentEquation = "";
        currentQuestionTimePrize = 0;
        answerIsTrue = false;
        for(int i = 1;i < numberOfOperands;i++)
            currentEquation += "(";
        currentAnswer = randomizer.nextInt(MAX_NUMBER + 1);
        currentEquation += Integer.toString( currentAnswer );
        for(int i = 1; i < numberOfOperands; i++) {
            hasSqrt = false;
            opIndex = randomizer.nextInt(operatorsToUse);

            if(opIndex == SQRT_OP_INDEX) {
                hasSqrt = true;
                opIndex = randomizer.nextInt(operatorsToUse - 1);
            }

            if(opIndex == POWER_OP_INDEX) {
                final int absCurrentAnswer = Math.abs(currentAnswer);
                if(absCurrentAnswer > SPECIAL_MAX_NUMBER) {
                    i--;
                    continue;
                }
                secondOperand = absCurrentAnswer < (SPECIAL_MAX_NUMBER / 2) ? randomizer.nextInt(SPECIAL_MAX_NUMBER + 1 - absCurrentAnswer * 2) : 2;
            }
            else {
                do {
                    secondOperand = randomizer.nextInt(( !hasSqrt ? MAX_NUMBER + 1 : SPECIAL_MAX_NUMBER + 1 ));
                }
                while ((opIndex == MULTIPLY_OP_INDEX && (currentAnswer > 10 || currentAnswer < 10) && (secondOperand > 10 || secondOperand < 10)) || (opIndex == DEVIDE_OP_INDEX && (secondOperand == 0 || currentAnswer % secondOperand != 0)));
            }

            currentQuestionTimePrize += TIME_PRIZES[opIndex];

            if(opIndex == MULTIPLY_OP_INDEX && secondOperand == 0)
                multipliedToZero = true;

            // here im tryin to decrease some prize time for simpler questions
            if( secondOperand == 1 && opIndex >= MULTIPLY_OP_INDEX && opIndex <= POWER_OP_INDEX )
                currentQuestionTimePrize -= (TIME_PRIZES[opIndex] - TIME_PRIZES[opIndex - 2]);
            else if(secondOperand == 0 && (opIndex == MULTIPLY_OP_INDEX || opIndex == POWER_OP_INDEX))
                currentQuestionTimePrize = (TIME_PRIZES[opIndex] - TIME_PRIZES[opIndex - 1]);
            else if(secondOperand == 10 && (opIndex == MULTIPLY_OP_INDEX || opIndex == DEVIDE_OP_INDEX))
                currentQuestionTimePrize -= TIME_PRIZES[opIndex - 1];
            else if(Math.abs(currentAnswer) == Math.abs(secondOperand) && opIndex == DEVIDE_OP_INDEX){
                currentQuestionTimePrize -= (TIME_PRIZES[DEVIDE_OP_INDEX] / 2);
            }

            //********** 2 ^ 0 ^ 1 ^ 2 ^ 3 ?? ***************//

            //calculate the next operand
            switch(opIndex)
            {
                case PLUS_OP_INDEX:
                    currentAnswer += secondOperand;  break;
                case MINUS_OP_INDEX:
                    currentAnswer -= secondOperand; break;
                case MULTIPLY_OP_INDEX:
                    currentAnswer *= secondOperand; break;
                case DEVIDE_OP_INDEX:
                    currentAnswer /= secondOperand; break;
                case POWER_OP_INDEX:
                    currentAnswer = (int)Math.pow(currentAnswer, secondOperand); break;
                default:
                    Toast.makeText(getContext(),getString(R.string.wrong_operator),Toast.LENGTH_LONG).show();
                    Log.wtf(EQUATION_CREATOR_ERROR_HANDLER , getString(R.string.wrong_operator) + " , OP_INDEX: " + Integer.toString(opIndex));
                    resetGame();
                    return;
            }

            //get extra time prize for the new operation considerin the number of anser digits
            currentQuestionTimePrize += getExtraPrize(currentAnswer);

            //check if the snd operand has sqrt and add its prize
            if(hasSqrt) {
                if(secondOperand != 0 && secondOperand != 1)
                    currentQuestionTimePrize += TIME_PRIZES[SQRT_OP_INDEX];
                else
                    currentQuestionTimePrize += (int) (TIME_PRIZES[SQRT_OP_INDEX] / 2);

            }

            //create the question string for showin up
            currentEquation += OPERATORS[opIndex] + ( !hasSqrt ? Integer.toString(secondOperand) : (OPERATORS[SQRT_OP_INDEX] + Integer.toString(secondOperand*secondOperand))) + ")";
        }

        // multiply to zero time prize improvise
        if(currentQuestionTimePrize > MAX_PRIZE)
            currentQuestionTimePrize = MAX_PRIZE;
        if(multipliedToZero && currentAnswer == 0 && numberOfOperands > multiplyToZeroDenominator ) {
            while (currentQuestionTimePrize > multiplyToZeroMaxTime)
                currentQuestionTimePrize /= multiplyToZeroDenominator;
            if(currentQuestionTimePrize < multiplyToZeroMinTime)
                currentQuestionTimePrize = multiplyToZeroMinTime;
        }

        currentEquation += " = ?";
        currentQuestionNumber++;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
        v = inflater.inflate(R.layout.fragment_main, container, false);

            randomizer = new SecureRandom();
            delayHandler = new Handler();
            lytSecondPart = (LinearLayout) v.findViewById(R.id.lytSecondPart);
            timeLeftProgress = (ProgressBar) v.findViewById(R.id.timeLeftProgress);
            timePrizeTextView = (TextView) v.findViewById(R.id.timePrizeTextView);
            answerEditText = (EditText) v.findViewById(R.id.answerEditText);
            allTimePrizesTextView = (TextView) v.findViewById(R.id.allTimePrizesTextView);
            equationTextView = (TextView) v.findViewById(R.id.equationTextView);
            noteTextView = (TextView) v.findViewById(R.id.noteTextView);

            checkButton = (Button) v.findViewById(R.id.checkButton);
            nextButton = (Button) v.findViewById(R.id.nextButton);

            initAnimations();

            // add listeners for check button and next button
            checkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final int userInput = Integer.parseInt(answerEditText.getText().toString());
                        v.setEnabled(false);
                        allAnsweres++;
                        if (userInput == currentAnswer) {
                            numberOfCorrectAnswers++;
                            answerIsTrue = true;
                            updatePrizes(currentQuestionTimePrize);
                            updateTimer(remainingTime);
                        } else {
                            timePrizeTextView.setText(String.format(getString(R.string.numeric_view_text), WRONG_ANSWER_TIME_LOSS));
                            answerIsTrue = false;
                            updatePrizes(WRONG_ANSWER_TIME_LOSS);
                            updateTimer(remainingTime);
                        }
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getContext(), getString(R.string.numberformatexception_message), Toast.LENGTH_LONG).show();
                        answerEditText.setText("");
                        Log.e(ANSWER_EDITTEXT_ERROR_HANDLER, ex.getMessage());
                    } catch (Exception ex) {
                        Toast.makeText(getContext(), getString(R.string.unknown_exception_message) + "\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                        answerEditText.setText("");
                        Log.e(ANSWER_EDITTEXT_ERROR_HANDLER, ex.getMessage());
                    }
                }
            });

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    equationTextView.startAnimation(equationToLeftAnimation);
                }
            });

            timerButton = (Button) v.findViewById(R.id.timerButton);
            timerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( !gameIsStarted ){
                        startGame();
                    }
                }
            });
            timerButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                /* maybe this part needs more codes!! maybe*/
                    if (gameIsStarted)
                        resetGame();
                    return true;
                }
            });
            resetGame();

        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return v;
    }
    public Boolean gameIsRunning(){
        return gameIsStarted;
    }
}