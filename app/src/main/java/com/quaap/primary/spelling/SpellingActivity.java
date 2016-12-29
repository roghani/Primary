package com.quaap.primary.spelling;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.quaap.primary.R;
import com.quaap.primary.base.BaseActivity;
import com.quaap.primary.base.InputMode;
import com.quaap.primary.base.StdGameActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpellingActivity extends StdGameActivity
        implements TextToVoice.VoiceReadyListener,
        BaseActivity.AnswerGivenListener<String>,
        BaseActivity.AnswerTypedListener{


    private List<String> words;

    private String word;

    private String[] unspellMap;

    TextToVoice v;
    public SpellingActivity() {
        super(R.layout.std_spelling_prob);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        unspellMap = getResources().getStringArray(R.array.unspell);

        if (unspellMap.length%2!=0) {
            throw new IllegalArgumentException("unspell array must have even number of arguments");
        }

        super.onCreate(savedInstanceState);


        Button b = (Button)findViewById(R.id.btn_repeat);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.speak(word);
            }
        });
    }

    @Override
    protected void onPause() {
        if (v!=null) {
            v.shutDown();
            v = null;
        }
        super.onPause();
    }


    @Override
    protected void onResume() {
        setReadyForProblem(false);
        findViewById(R.id.spelling_problem_area).setVisibility(View.INVISIBLE);
        findViewById(R.id.spell_loading).setVisibility(View.VISIBLE);
        v = new TextToVoice(this);
        v.setVoiceReadyListener(this);

        super.onResume();

    }

    @Override
    protected void onShowLevel() {
        super.onShowLevel();
        words = Arrays.asList(getResources().getStringArray(((SpellingLevel)levels[levelnum]).getmWordlistId()));
    }

    @Override
    protected void showProbImpl() {

        int tries=0;
        do {
            word = words.get(getRand(words.size()-1));
        } while (tries++<50 && seenProblem(word));

        Log.d("spell", word);

        SpellingLevel level = (SpellingLevel) levels[levelnum];

        if (level.getInputMode() == InputMode.Buttons) {
            List<String> answers = getAnswerChoices(word);

            makeChoiceButtons(getAnswerArea(), answers, this);

        } else if (level.getInputMode() == InputMode.Input) {

            makeInputBox(getAnswerArea(), getKeysArea(), this, INPUTTYPE_TEXT, 0, 0);
        } else {
            throw new IllegalArgumentException("Unknown inputMode! " + level.getInputMode());
        }
        v.speak(word);

    }

    @Override
    public boolean answerTyped(String answer) {
        return answerGiven(answer);
    }

    @Override
    public boolean answerGiven(String answer) {

        int points = 0;
        boolean isright = answer.toLowerCase().trim().equals(word.toLowerCase());
        if (isright) {
            points = word.length() * (levelnum+1);
        }
        answerDone(isright, points, word, word, answer.trim());

        if (!isright) {

        }
        return isright;
    }


    final protected Handler handler = new Handler();
    @Override
    public void onVoiceReady(TextToVoice ttv) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.spelling_problem_area).setVisibility(View.VISIBLE);
                findViewById(R.id.spell_loading).setVisibility(View.GONE);
                setReadyForProblem(true);
            }
        });
        Log.d("sp1", "onVoiceReady called");
    }

    @Override
    public void onSpeakComplete(TextToVoice ttv) {
        startTimer();
    }

    @Override
    public void onError(TextToVoice ttv) {

    }




    private final int numanswers = 4;




    protected List<String> getAnswerChoices(String realanswer) {
        List<String> answers = new ArrayList<>();
        int maxtries = unspellMap.length;
        int tries = 0;
        do {
            String badspell;
            tries = 0;
            do {
                badspell = unspell(word);
            } while (tries++<maxtries && answers.contains(badspell));
            if (tries<maxtries) {
                answers.add(badspell);
            }

        } while (answers.size()<numanswers && tries<maxtries);

        Collections.shuffle(answers);
        return answers;
    }



    public String unspell(String word) {
        List<String> words = new ArrayList<>();


        for (int j=0; j<1; j++) {
            int i = ((int) (Math.random() * ((unspellMap.length-1) / 2 )) * 2);
            word = word.replaceFirst(unspellMap[i], unspellMap[i + 1]);
        }
        return word;
    }


}
