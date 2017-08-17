package com.volnoor.githubclient;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Eugene on 17.08.2017.
 */

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.RepositoryHolder> {
    private ArrayList<Repository> mRepositories;

    public RepositoryAdapter(ArrayList<Repository> repositories) {
        mRepositories = repositories;
    }

    @Override
    public RepositoryAdapter.RepositoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_row, parent, false);
        return new RepositoryHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RepositoryAdapter.RepositoryHolder holder, int position) {
        Repository repository = mRepositories.get(position);
        holder.bindRepository(repository.getName(), repository.getDescription());
    }

    @Override
    public int getItemCount() {
        return mRepositories.size();
    }

    public class RepositoryHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mDescription;

        public RepositoryHolder(View v) {
            super(v);

            mName = v.findViewById(R.id.tv_repository_name);
            mDescription = v.findViewById(R.id.tv_repository_description);
        }

        public void bindRepository(String name, String description) {
            mName.setText(name);
            mDescription.setText(description);
        }
    }
}
