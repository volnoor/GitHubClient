package com.volnoor.githubclient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String GITHUB_OAUTH = "https://github.com/login/oauth/access_token";
    private static final String CLIENT_ID = "306bb28c16631d720b1e";
    private static final String CLIENT_SECRET = "38b5f018c35ec7404ec5cd77ec3c94bd97491384";
    private static final String REDIRECT_URI = "githubclient://callback";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void signIn(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/oauth/authorize"
                + "?client_id=" + CLIENT_ID
                + "&scope=user,public_repo" // Permission to read/update
                + "&redirect_uri=" + REDIRECT_URI));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();

        // If authorized from a browser
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            String code = uri.getQueryParameter("code");

            new LoginTask().execute(code);
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
            JSONObject json = null;
            try {
                Response response = client.newCall(request).execute();

                json = new JSONObject(response.body().string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    progressDialog.dismiss();
                    String accessToken = jsonObject.getString("access_token");

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("access_token", accessToken);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}