package com.quaap.primary.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quaap.primary.Levels;
import com.quaap.primary.MainActivity;
import com.quaap.primary.R;

/**
 * Created by tom on 12/15/16.
 * <p>
 * Copyright (C) 2016   Tom Kliethermes
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/*
Future Subjects:
Math 1, but typed answers.
Spelling.
Reading/vocabulary.
New big words.
Goofy math word problems.


*/

public abstract class SubjectMenuActivity extends AppCompatActivity implements Button.OnClickListener {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 121;
    private SharedPreferences mPrefs;
    private String mSubject;
    private Class mTargetActivity;

    private String mLevelSetName;
    private String username;

    protected void setTargetActivity(Class targetActivity) {
        mTargetActivity = targetActivity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mSubject = intent.getStringExtra(MainActivity.SUBJECT);
        mLevelSetName = intent.getStringExtra(MainActivity.LEVELSET);
        username = intent.getStringExtra(MainActivity.USERNAME);


        setContentView(R.layout.activity_subject_menu);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setTitle(getString(R.string.app_name) + ": " + mSubject + " (" + username + ")");
        }

        mPrefs = BaseActivity.getSharedPreferences(this, username, mSubject);

        Button resume_button = (Button)findViewById(R.id.resume_button);
        resume_button.setTag(-1);
        resume_button.setOnClickListener(this);

        Button clear_button = (Button)findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SubjectMenuActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Clear progress")
                        .setMessage("Are you sure you want to clear your progress?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()  {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearProgress();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });
        checkStorageAccess();
    }

    private void showLevelButtons() {
        int highest = mPrefs.getInt("highestLevelnum", 0);

        LinearLayout button_layout = (LinearLayout)findViewById(R.id.button_layout);

        button_layout.removeAllViews();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL;

        for( Level level: Levels.getLevels(mLevelSetName)) {

            LinearLayout levelrow = new LinearLayout(this);
            levelrow.setOrientation(LinearLayout.HORIZONTAL);
            button_layout.addView(levelrow);

            Button levelbutt = new Button(this);
            levelbutt.setLayoutParams(lp);
            levelbutt.setText(getString(R.string.level, level.getLevelNum()));
            levelbutt.setTag(level.getLevelNum() - 1);
            levelbutt.setOnClickListener(this);
            levelrow.addView(levelbutt);

            TextView desc = new TextView(this);
            desc.setText(level.getDescription(this));
            desc.setLayoutParams(lp);
            desc.setTextSize(16);
            levelrow.addView(desc);

            boolean beenthere = level.getLevelNum()-1<=highest;
            levelbutt.setEnabled(beenthere);
            desc.setEnabled(beenthere);
        }
    }

    private void clearProgress() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.clear();
        ed.apply();

        show_hide_gip();
        showLevelButtons();
    }

    private void show_hide_gip() {
        LinearLayout gip_layout = (LinearLayout)findViewById(R.id.gip_layout);
        TextView score_overview = (TextView)findViewById(R.id.score_overview);
        if (mPrefs.getInt("levelnum", -1)==-1) {
            gip_layout.setVisibility(View.GONE);
            score_overview.setText(" ");
        } else {
            gip_layout.setVisibility(View.VISIBLE);
            int correct = mPrefs.getInt("totalCorrect", 0);
            int incorrect = mPrefs.getInt("totalIncorrect", 0);
            int highest = mPrefs.getInt("highestLevelnum", 0)+1;
            int tscore = mPrefs.getInt("tscore", 0);
            if (correct+incorrect>0) {
                String score = "Level: " + highest + ". Correct: " + correct + "/" + (correct + incorrect) + ". Points: " + tscore;
                score_overview.setText(score);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        show_hide_gip();
        showLevelButtons();
    }

    private final static int SUBMENU_REQUEST = 1;
    public final static int RESULTCODE_SETDONE = 103;

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, mTargetActivity);
        intent.putExtra(BaseActivity.LEVELNAME, (int)view.getTag());
        intent.putExtra(MainActivity.USERNAME, username);
        intent.putExtra(MainActivity.LEVELSET, mLevelSetName);
        intent.putExtra(MainActivity.SUBJECT, mSubject);
        startActivityForResult(intent, SUBMENU_REQUEST);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SUBMENU_REQUEST && resultCode== RESULTCODE_SETDONE) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
            setResult(MainActivity.RESULTCODE_SETDONE);
            finish();
        }
    }

    private void checkStorageAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Yay!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Boo!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
