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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private final String USER = "https://api.github.com/user";

    private String CLIENT_ID = "306bb28c16631d720b1e";
    private String CLIENT_SECRET = "38b5f018c35ec7404ec5cd77ec3c94bd97491384";
    private String redirectUri = "githubclient://callback";

    public static final String GITHUB_OAUTH = "https://github.com/login/oauth/access_token";

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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/oauth/authorize"
                + "?client_id=" + CLIENT_ID
                + "&scope=user,public_repo"
                + "&redirect_uri=" + redirectUri));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(redirectUri)) {
            Log.d(TAG, uri.toString());
            final String code = uri.getQueryParameter("code");
            Log.d(TAG, code);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    HttpUrl.Builder url = HttpUrl.parse(GITHUB_OAUTH).newBuilder();
                    url.addQueryParameter("client_id", CLIENT_ID);
                    url.addQueryParameter("client_secret", CLIENT_SECRET);
                    url.addQueryParameter("code", code);

                    String url_oauth = url.build().toString();

                    final Request request = new Request.Builder()
                            .header("Accept", "application/json")
                            .url(url_oauth)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            //TODO error
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {

                                String jsonData = response.body().string();

                                try {
                                    JSONObject jsonObject = new JSONObject(jsonData);
                                    String access_token = jsonObject.getString("access_token");
                                    Log.d(TAG, "auth_token " + access_token);

                                    //  runOnUiThread();
                                    //   startActivity(new Intent(this, MainActivity.class));

                                } catch (JSONException exp) {

                                    Log.e(TAG, "json exception: " + exp.getMessage());
                                }
                            }
                        }
                    });
                }
            }).start();
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