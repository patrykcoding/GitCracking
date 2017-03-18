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

import java.util.ArrayList;


public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private ArrayList<String> repositoriesData;
    private ArrayList<String> repositoriesData2;

    public RepositoriesAdapter() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;
        public final TextView textView2;

        public ViewHolder(View view) {
            super(view);
            textView =  (TextView) view.findViewById(R.id.tv_repositories_name);
            textView2 = (TextView) view.findViewById(R.id.tv_repositories_description);
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
        String repo = repositoriesData.get(position);
        String otherData = repositoriesData2.get(position);
        holder.textView.setText(repo);
        holder.textView2.setText(otherData);
    }

    @Override
    public int getItemCount() {
        if (repositoriesData == null) {
            return 0;
        }
        return repositoriesData.size();
    }

    public void setRepositoriesData(ArrayList<String> repositoriesData) {
        this.repositoriesData = repositoriesData;
        notifyDataSetChanged();
    }

    public void setRepositoriesData2(ArrayList<String> repositoriesData2) {
        this.repositoriesData2 = repositoriesData2;
        notifyDataSetChanged();
    }

    public void addMore(String item, String item2) {
        this.repositoriesData.add(0, item);
        this.repositoriesData2.add(0, item2);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return repositoriesData.get(position);
    }
}
