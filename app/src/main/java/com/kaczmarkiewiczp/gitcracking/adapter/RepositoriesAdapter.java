package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kaczmarkiewiczp.gitcracking.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;


public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private String[] repositoriesData;

    public RepositoriesAdapter() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView =  (TextView) view.findViewById(R.id.tv_repositories_data);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.repositories_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RepositoriesAdapter.ViewHolder holder, int position) {
        String repo = repositoriesData[position];
        holder.textView.setText(repo);
    }

    @Override
    public int getItemCount() {
        if (repositoriesData == null) {
            return 0;
        }
        return repositoriesData.length;
    }

    public void setRepositoriesData(String[] repositoriesData) {
        this.repositoriesData = repositoriesData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return repositoriesData[position];
    }
}
