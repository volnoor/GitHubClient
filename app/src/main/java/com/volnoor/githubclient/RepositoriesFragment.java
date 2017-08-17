package com.volnoor.githubclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RepositoriesFragment extends Fragment {
    private static final String TAG = RepositoriesFragment.class.getSimpleName();
    private static final String ARG_USERNAME = "username";

    private String username;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RepositoryAdapter mAdapter;

    public RepositoriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username username.
     * @return A new instance of fragment RepositoriesFragment.
     */
    public static RepositoriesFragment newInstance(String username) {
        RepositoriesFragment fragment = new RepositoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repositories, container, false);

        mRecyclerView = view.findViewById(R.id.rv_repositories);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        new LoadTask().execute("https://api.github.com/users/" + username + "/repos");

        return view;
    }

    private class LoadTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... strings) {
            JSONArray json = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(strings[0])
                        .build();
                Response response = client.newCall(request).execute();
                // Log.e(TAG, response.body().string());
                // json = new JSONObject(response.body().string());
                json = new JSONArray(response.body().string());
                //  Log.e(TAG, "here we go"+ new JSONObject(response.body().string()).toString());
            } catch (@NonNull IOException | JSONException e) {
                Log.e(TAG, "" + e.getLocalizedMessage());
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                ArrayList<Repository> repositoryArrayList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String description = jsonArray.getJSONObject(i).getString("description");

                    Repository repository = new Repository(name, description);

                    repositoryArrayList.add(repository);
                    Log.d(TAG, repository.getName() + ", " + repository.getDescription());
                }

                mAdapter = new RepositoryAdapter(repositoryArrayList);
                mRecyclerView.setAdapter(mAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}