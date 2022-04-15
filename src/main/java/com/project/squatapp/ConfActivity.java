package com.project.squatapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConfActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_conf);

        EditText repNum = findViewById(R.id.repNum);
        EditText restTime = findViewById(R.id.restTime);


        Button startBtn = findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer reps = Integer.valueOf(repNum.getText().toString());
                Integer rest = Integer.valueOf(restTime.getText().toString());

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("repNum", reps);
                intent.putExtra("restTime", rest);
                startActivity(intent);
            }
        });

    }
}
