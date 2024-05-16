package com.cloudycat.cloudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudycat.cloudyapp.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView navHeaderText, loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);

        setSupportActionBar(binding.appBarMain.toolbar);
        // кнопка справа снизу
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_enter)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navHeaderText = navigationView.getHeaderView(0).findViewById(R.id.nav_header_text);
        loginUser = navigationView.getHeaderView(0).findViewById(R.id.login);
        getRequestValue();
        getUserValue();


        // Find the ImageView in the navigation header
        ImageView profileImageView = binding.navView.getHeaderView(0).findViewById(R.id.imageView);

        // Set an OnClickListener on the ImageView
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start EditProfileActivity
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        TextView log = (TextView) findViewById(R.id.login);
        String name;
        name= LoginActivity.FirstName;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void getRequestValue() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://185.20.225.206/api/v1/wallet/balance";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String text = getRublesString(Integer.parseInt(response));
                        navHeaderText.setText("Баланс - " + " " + text);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        queue.add(request);
    }
    private void getUserValue() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://185.20.225.206/api/v1/me";


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String firstName,lastName;


                        try {
                            JSONObject userInfo = (JSONObject) response.get("userInfo");
                            JSONObject additionalDetails = userInfo.getJSONObject("additionalDetails");

                            firstName = additionalDetails.getString("firstName");
                            lastName = additionalDetails.getString("lastName");

                            loginUser.setText(firstName + " " + lastName);


                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        queue.add(request);

    }

    public static String getRublesString(int value) {
        int remainder = value % 100;
        int lastDigit = value % 10;

        if (remainder >= 11 && remainder <= 19) {
            return value + " рублей";
        } else if (lastDigit == 1) {
            return value + " рубль";
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return value + " рубля";
        } else {
            return value + " рублей";
        }
    }
}