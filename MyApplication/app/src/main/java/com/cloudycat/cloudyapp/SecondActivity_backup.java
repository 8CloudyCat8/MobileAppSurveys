package com.cloudycat.cloudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity_backup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        SeekBar seekBar = findViewById(R.id.seekBar);
        final TextView seekBarValue = findViewById(R.id.seekBarValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button button = findViewById(R.id.submitBtn);
        View.OnClickListener onClickButtonToMain = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int seekBarProgress = seekBar.getProgress();
                Intent intent = new Intent(SecondActivity_backup.this, MainActivity.class);
                intent.putExtra("seekBarProgress", seekBarProgress);
                startActivity(intent);
            }
        };
        button.setOnClickListener(onClickButtonToMain);
    }
}
