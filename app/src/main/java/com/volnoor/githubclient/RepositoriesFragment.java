package com.volnoor.githubclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RepositoriesFragment extends Fragment {

    private static final String URL = "https://api.github.com/users/";
    private static final String REPOS = "/repos";
    private static final String SAVE_KEY = "save_key";

    private ArrayList<Repository> repositories;
    private RepositoryAdapter mAdapter;

    private ProgressBar progressBar;

    public RepositoriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RepositoriesFragment.
     */
    public static RepositoriesFragment newInstance() {
        return new RepositoriesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repositories, container, false);

        RecyclerView mRecyclerView = view.findViewById(R.id.rv_repositories);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        // No need to download if screen orientation changed
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_KEY)) {
            repositories = savedInstanceState.getParcelableArrayList(SAVE_KEY);
        } else {
            repositories = new ArrayList<>();
        }

        mAdapter = new RepositoryAdapter(repositories);
        mRecyclerView.setAdapter(mAdapter);

        progressBar = view.findViewById(R.id.pb_repositories);

        // Download repositories list
        if (repositories.isEmpty()) {
            SharedPreferences prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String username = prefs.getString("username", "");
            if (username.equals("")) {
                showAlertDialog(getString(R.string.error), "Error reading username");
            } else {
                new LoadTask().execute(URL + username + REPOS);
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save list that was downloaded
        outState.putParcelableArrayList(SAVE_KEY, repositories);
        super.onSaveInstanceState(outState);
    }

    // Takes URL, return json with repositories
    private class LoadTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(String... strings) {
            JSONArray json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(strings[0])
                        .build();
                Response response = client.newCall(request).execute();

                json = new JSONArray(response.body().string());
            } catch (@NonNull IOException | JSONException e) {
                e.printStackTrace();
                showAlertDialog(getString(R.string.error), getString(R.string.error_acc_server));
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String description = jsonArray.getJSONObject(i).getString("description");

                    if (description.equals("null")) {
                        description = "No description";
                    }

                    Repository repository = new Repository(name, description);

                    repositories.add(repository);
                }

                mAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.INVISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
                showAlertDialog(getString(R.string.error), getString(R.string.error_reading_response));
            }
        }
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create();

        alertDialog.show();
    }
}