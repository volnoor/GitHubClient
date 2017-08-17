package com.volnoor.githubclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RepositoriesFragment extends Fragment {
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

        ArrayList<Repository> repositoryArrayList = new ArrayList<>();
        repositoryArrayList.add(new Repository("hh", "ha"));
        repositoryArrayList.add(new Repository("hy", "hk"));
        mAdapter = new RepositoryAdapter(repositoryArrayList);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }
}