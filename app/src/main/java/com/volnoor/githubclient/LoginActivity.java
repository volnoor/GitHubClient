package com.volnoor.githubclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private final String URL = "https://api.github.com/users/";

    private String clientId = "306bb28c16631d720b1e";
    private String clientSecret = "38b5f018c35ec7404ec5cd77ec3c94bd97491384";
    private String redirectUri = "githubclient://callback";

    private EditText etUsername;

    private LoginTask loginTask;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.et_username);
    }

    public void signIn(View v) {
//        String username = etUsername.getText().toString();
//
//        // Only one task at a time
//        if (loginTask == null || loginTask.isCancelled()) {
//            loginTask = new LoginTask();
//            loginTask.execute(URL + username);
//        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&scope=user&redirect_uri=" + redirectUri));
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(redirectUri)) {
            Log.d(TAG, uri.toString());
            String code = uri.getQueryParameter("code");
            Log.d(TAG, code);

            //startActivity(new Intent(this, MainActivity.class));
        }
    }

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
                Request request = new Request.Builder()
                        .url(strings[0])
                        .build();

                Response response = new OkHttpClient().newCall(request).execute();

                json = new JSONObject(response.body().string());
            } catch (@NonNull IOException | JSONException e) {
                Log.e(TAG, "" + e.getLocalizedMessage());
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                // A JSON object with "message" is return if username is non-existent
                if (jsonObject.has("message")) {
                    // Username doesn't exist
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Error")
                            .setMessage("This username doesn't exist")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .create()
                            .show();

                    progressDialog.dismiss();

                    // Cancel AsyncTask
                    cancel(false);

                    return;
                }

                try {
                    progressDialog.dismiss();

                    // Start MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", jsonObject.getString("login"));
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}