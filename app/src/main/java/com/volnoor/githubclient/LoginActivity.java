package com.volnoor.githubclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String GITHUB_OAUTH = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_AUTHORIZE = "https://github.com/login/oauth/authorize";
    private static final String CLIENT_ID = "306bb28c16631d720b1e";
    private static final String CLIENT_SECRET = "38b5f018c35ec7404ec5cd77ec3c94bd97491384";
    private static final String REDIRECT_URI = "githubclient://callback";
    private static final String USER = "https://api.github.com/user";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void signIn(View v) {
        // Check for internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_AUTHORIZE
                    + "?client_id=" + CLIENT_ID
                    + "&scope=user,public_repo" // Permission to read/update
                    + "&redirect_uri=" + REDIRECT_URI));
            startActivity(intent);
        } else {
            showAlertDialog("Error", "Please check your internet connection");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();

        // If authorized successfully
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            String code = uri.getQueryParameter("code");

            new LoginTask().execute(code);
        }
    }

    // Input: authorization code, output: json object with user data
    private class LoginTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject json = null;

            try {
                // Getting json with access token
                HttpUrl.Builder url = HttpUrl.parse(GITHUB_OAUTH).newBuilder()
                        .addQueryParameter("client_id", CLIENT_ID)
                        .addQueryParameter("client_secret", CLIENT_SECRET)
                        .addQueryParameter("code", strings[0]);

                Request request = new Request.Builder()
                        .header("Accept", "application/json")
                        .url(url.build().toString())
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                // Json with access token
                json = new JSONObject(response.body().string());

                String accessToken = json.getString("access_token");

                // Get username using token
                url = HttpUrl.parse(USER).newBuilder()
                        .addQueryParameter("access_token", accessToken);

                request = new Request.Builder()
                        .header("Accept", "application/json")
                        .url(url.build().toString())
                        .build();

                response = client.newCall(request).execute();

                // Json with user data
                json = new JSONObject(response.body().string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                showAlertDialog("Error", "Error getting access");
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    progressDialog.dismiss();

                    // Get username from json
                    String username = jsonObject.getString("login");

                    // Save username to preferences
                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("username", username);
                    edit.apply();

                    // Start MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showAlertDialog("Error", "Error reading response");
                }
            }
        }
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create();

        alertDialog.show();
    }
}