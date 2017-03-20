package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kaczmarkiewiczp.gitcracking.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.eclipse.egit.github.core.Repository;
import org.w3c.dom.Text;

import java.util.ArrayList;


public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private ArrayList<Repository> repositories;
    private Context context;

    public RepositoriesAdapter(Context context) {
        repositories = new ArrayList<>();
        this.context = context;

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
        /* retrieve required data from the Repository structure */
        String name = repositories.get(position).getName();
        String owner = repositories.get(position).getOwner().getLogin();
        String description = repositories.get(position).getDescription();
        Boolean isPrivate = repositories.get(position).isPrivate();
        String language = repositories.get(position).getLanguage();
        Integer forks = repositories.get(position).getForks();
        Integer watchers = repositories.get(position).getWatchers();
        Integer size = repositories.get(position).getSize();

        /* Set appropriate elements in out view */
        holder.textViewName.setText(owner + "/" + name);
        if (description == null || description.isEmpty()) {
            holder.textViewDescription.setVisibility(View.GONE);
        } else {
            holder.textViewDescription.setText(description);
        }
        if (language == null || language.isEmpty()) {
            holder.textViewLanguage.setText("unknown");
        } else {
            holder.textViewLanguage.setText(language);
        }
        holder.textViewForks.setText(forks.toString());
        holder.textViewWatchers.setText(watchers.toString());
        String s = Formatter.formatShortFileSize(context, size.longValue() * 1024);
        holder.textViewSize.setText(s);
        if (isPrivate)
            holder.linearLayoutPrivate.setVisibility(View.VISIBLE);
        else
            holder.linearLayoutPrivate.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (repositories == null) {
            return 0;
        }
        return repositories.size();
    }

    public void setRepositories(ArrayList<Repository> repositories) {
        this.repositories.addAll(repositories);
    }

    public void clearView() {
        repositories.clear();
    }

    public void updateView() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String repositoryName = repositories.get(position).getName();
        String firstLetter = repositoryName.substring(0, 1).toUpperCase();
        return firstLetter;
    }
}
