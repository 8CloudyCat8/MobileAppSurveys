package com.cloudycat.cloudyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    public static String FirstName;
    public String LastName;
    public String ID;
    public String Email;
    public String Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton buttonOnMain = (MaterialButton)  findViewById(R.id.buttonToLogin);
        TextView login = (TextView) findViewById(R.id.login_email);
        TextView password = (TextView) findViewById(R.id.login_password);

        View.OnClickListener onClickButtonToMain = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login.getText().toString().isEmpty() && password.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Данные не заполнены", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                String url2 = "http://185.20.225.206/api/v1/login";

                sendJsonPostRequest(login.getText().toString(), password.getText().toString());

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };
        buttonOnMain.setOnClickListener(onClickButtonToMain);

        //jsonParams.put("username", "NikitaDolganov@gmail.com");
        //jsonParams.put("password", "NikitaDolganov1");

    }
    private void sendJsonPostRequest(String login, String password){
        try {
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
            // Make new json object and put params in it
            JSONObject jsonParams = new JSONObject();

            jsonParams.put("username", login);
            jsonParams.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,

                    "http://185.20.225.206/api/v1/login",
                    jsonParams,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            String firstName;
                            String lastName;
                            String email;
                            String id;
                            String phone;
                            JSONObject userInfo;
                            JSONObject additionalDetails;
                            JSONObject additionalDetails1;
                            try {
                                userInfo = (JSONObject) response.get("userInfo");
                                additionalDetails = (JSONObject) userInfo.get("additionalDetails");


                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                firstName = additionalDetails.get("firstName").toString();
                                lastName = additionalDetails.get("lastName").toString();
                                email = userInfo.get("email").toString();
                                id = userInfo.get("id").toString();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            FirstName= firstName;
                            LastName= lastName;
                            Email = email;
                            ID = id;

                            Toast toast = Toast.makeText(getApplicationContext(), "Привет, "+FirstName + " " + LastName, Toast.LENGTH_SHORT);

                            toast.show();
                        }
                    },

                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Неверный логин или пароль", Toast.LENGTH_SHORT);
                            toast.show();

                        }
                    });


            Volley.newRequestQueue(getApplicationContext()).
                    add(request);



        } catch(JSONException ex){
            Toast toast = Toast.makeText(getApplicationContext(),
                    ex.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}