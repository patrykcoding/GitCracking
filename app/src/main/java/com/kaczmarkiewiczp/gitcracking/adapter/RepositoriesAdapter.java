package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kaczmarkiewiczp.gitcracking.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private ArrayList<String> repositoriesName;
    private ArrayList<String> repositoriesOwner;
    private ArrayList<String> repositoriesDescription;
    private ArrayList<Boolean> repositoriesPrivate;
    private ArrayList<String> repositoriesLanguage;
    private ArrayList<String> repositoriesForks;
    private ArrayList<String> repositoriesWatchers;
    private ArrayList<String> repositoriesSize;

    public RepositoriesAdapter() {
        repositoriesName = new ArrayList<>();
        repositoriesOwner = new ArrayList<>();
        repositoriesDescription = new ArrayList<>();
        repositoriesPrivate = new ArrayList<>();
        repositoriesLanguage = new ArrayList<>();
        repositoriesForks = new ArrayList<>();
        repositoriesWatchers = new ArrayList<>();
        repositoriesSize = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewName;
        public final TextView textViewDescription;
        public final LinearLayout linearLayoutPrivate;
        public final TextView textViewLanguage;
        public final TextView textViewForks;
        public final TextView textViewWatchers;
        public final TextView textViewSize;


        public ViewHolder(View view) {
            super(view);
            textViewName = (TextView) view.findViewById(R.id.tv_repositories_name);
            textViewDescription = (TextView) view.findViewById(R.id.tv_repositories_description);
            linearLayoutPrivate = (LinearLayout) view.findViewById(R.id.repositories_private);
            textViewLanguage = (TextView) view.findViewById(R.id.tv_repositories_code);
            textViewForks = (TextView) view.findViewById(R.id.tv_repositories_forks);
            textViewWatchers = (TextView) view.findViewById(R.id.tv_repositories_stars);
            textViewSize = (TextView) view.findViewById(R.id.tv_repositories_storage);
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
        String name = repositoriesName.get(position);
        String owner = repositoriesOwner.get(position);
        String description = repositoriesDescription.get(position);
        Boolean isPrivate = repositoriesPrivate.get(position);
        String language = repositoriesLanguage.get(position);
        String forks = repositoriesForks.get(position);
        String watchers = repositoriesWatchers.get(position);
        String size = repositoriesSize.get(position);

        holder.textViewName.setText(owner + "/" + name);
        holder.textViewDescription.setText(description);
        if (language == null || language.isEmpty()) {
            holder.textViewLanguage.setText("unknown");
        } else {
            holder.textViewLanguage.setText(language);
        }
        holder.textViewForks.setText(forks);
        holder.textViewWatchers.setText(watchers);
        holder.textViewSize.setText(size);
        if (isPrivate)
            holder.linearLayoutPrivate.setVisibility(View.VISIBLE);
        else
            holder.linearLayoutPrivate.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (repositoriesName == null) {
            return 0;
        }
        return repositoriesName.size();
    }

    public void addRepositoriesName(ArrayList<String> names) {
        repositoriesName.addAll(names);
    }

    public void addRepositoriesOwner(ArrayList<String> owners) {
        repositoriesOwner.addAll(owners);
    }

    public void addDescriptions(ArrayList<String> descritpions) {
        repositoriesDescription.addAll(descritpions);
    }

    public void addLanguages(ArrayList<String> languages) {
        repositoriesLanguage.addAll(languages);
    }

    public void addForks(ArrayList<String> forks) {
        repositoriesForks.addAll(forks);
    }

    public void addWatchers(ArrayList<String> watchers) {
        repositoriesWatchers.addAll(watchers);
    }

    public void addSizes(ArrayList<String> sizes) {
        repositoriesSize.addAll(sizes);
    }

    public void addPrivates(ArrayList<Boolean> isPrivate) {
        repositoriesPrivate.addAll(isPrivate);
    }

    public void updateView() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return repositoriesName.get(position).substring(0,1).toUpperCase();
    }
}
