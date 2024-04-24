package com.cloudycat.cloudyapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudycat.cloudyapp.databinding.ActivityEnterBinding;
import com.cloudycat.cloudyapp.repository.MainRepository;
import com.google.firebase.database.FirebaseDatabase;
import com.permissionx.guolindev.PermissionX;

public class EnterActivity extends AppCompatActivity {

    private ActivityEnterBinding views;

    private MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().getReference().child("cloudy").setValue("Hello");
        views = ActivityEnterBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        init();
    }

    private void init() {
        mainRepository = MainRepository.getInstance();
        views.enterBtn.setOnClickListener(v -> {
            PermissionX.init(this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            //login to firebase here

                            mainRepository.login(
                                    views.username.getText().toString(), getApplicationContext(), () -> {
                                        //if success then we want to move to call activity
                                        startActivity(new Intent(EnterActivity.this, CallActivity.class));
                                    }
                            );
                        }
                    });
        });
    }
}