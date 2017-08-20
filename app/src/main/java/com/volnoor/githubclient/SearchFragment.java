package com.volnoor.githubclient;

import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class SearchFragment extends Fragment implements View.OnClickListener {

    private static final String GITHUB_BASE_URL = "https://api.github.com/search/repositories";
    private static final String PARAM_QUERY = "q";
    private static final String SAVE_KEY = "save_key";

    private EditText etSearch;

    private RepositoryAdapter mAdapter;
    private ArrayList<Repository> repositories;

    private ProgressBar progressBar;

    private SearchTask searchTask;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.et_search);

        Button button = view.findViewById(R.id.btn_search);
        button.setOnClickListener(this);

        RecyclerView mRecyclerView = view.findViewById(R.id.rv_search);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        // If screen orientation changed
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_KEY)) {
            repositories = savedInstanceState.getParcelableArrayList(SAVE_KEY);
        } else {
            repositories = new ArrayList<>();
        }

        mAdapter = new RepositoryAdapter(repositories);
        mRecyclerView.setAdapter(mAdapter);

        progressBar = view.findViewById(R.id.pb_search);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save downloaded list
        outState.putParcelableArrayList(SAVE_KEY, repositories);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        // Check for internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            String searchQuery = etSearch.getText().toString();

            // If nothing is entered
            if (searchQuery.length() < 1) {
                showAlertDialog(getString(R.string.error), getString(R.string.empty_search));
                return;
            }

            Uri uri = Uri.parse(GITHUB_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_QUERY, searchQuery)
                    .build();

            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                showAlertDialog(getString(R.string.error), getString(R.string.failed_create_url));
            }

            if (searchTask == null || searchTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                searchTask = new SearchTask();
                searchTask.execute(url);
            }
        } else {
            showAlertDialog(getString(R.string.error), getString(R.string.check_int_con));
        }
    }

    private class SearchTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject json = null;

            try {
                Request request = new Request.Builder()
                        .url(urls[0])
                        .build();

                Response response = new OkHttpClient().newCall(request).execute();

                json = new JSONObject(response.body().string());
            } catch (@NonNull IOException | JSONException e) {
                e.printStackTrace();
                showAlertDialog(getString(R.string.error), getString(R.string.error_acc_server));
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                repositories.clear();

                JSONArray jsonArray = jsonObject.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String description = jsonArray.getJSONObject(i).getString("description");

                    if (description.equals("null")) {
                        description = "No description";
                    }

                    repositories.add(new Repository(name, description));
                }

                mAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.INVISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
                showAlertDialog(getString(R.string.error), getString(R.string.error_reading_response));
                progressBar.setVisibility(View.INVISIBLE);
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