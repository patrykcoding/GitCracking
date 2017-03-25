package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kaczmarkiewiczp.gitcracking.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.eclipse.egit.github.core.User;

import java.util.ArrayList;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<User> people;
    private Context context;

    public PeopleAdapter() {
        people = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.people_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String userName = people.get(position).getName();
        String userLogin = people.get(position).getLogin();
        String description = people.get(position).getBio();
        String location = people.get(position).getLocation();
        String userIconUrl = people.get(position).getAvatarUrl();

        if (userName != null) {
            holder.textViewName.setText(userName);
        }
        if (userLogin != null) {
            holder.textViewUsername.setText(userLogin);
        }
        if (description != null) {
            holder.linearLayoutDescription.setVisibility(View.VISIBLE);
            holder.textViewDescription.setText(description);
        } else {
            holder.linearLayoutDescription.setVisibility(View.GONE);
        }
        if (location != null) {
            holder.linearLayoutLocation.setVisibility(View.VISIBLE);
            holder.textViewLocation.setText(location);
        } else {
            holder.linearLayoutLocation.setVisibility(View.GONE);
        }
        Glide.with(context).load(userIconUrl).into(holder.imageViewUserIcon);
    }

    @Override
    public int getItemCount() {
        if (people == null) {
            return 0;
        }
        return people.size();
    }

    public void setPeople(ArrayList<User> people) {
        this.people.addAll(people);
        notifyDataSetChanged();
    }

    public void clearPeople() {
        this.people.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String username = people.get(position).getLogin();
        String firstLetter = username.substring(0, 1).toUpperCase();
        return firstLetter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageViewUserIcon;
        public final TextView textViewName;
        public final TextView textViewUsername;
        public final LinearLayout linearLayoutDescription;
        public final TextView textViewDescription;
        public final LinearLayout linearLayoutLocation;
        public final TextView textViewLocation;

        public ViewHolder(View view) {
            super(view);
            imageViewUserIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
            textViewName = (TextView) view.findViewById(R.id.tv_user_name);
            textViewUsername = (TextView) view.findViewById(R.id.tv_user_login);
            linearLayoutDescription = (LinearLayout) view.findViewById(R.id.ll_user_description);
            textViewDescription = (TextView) view.findViewById(R.id.tv_user_description);
            linearLayoutLocation = (LinearLayout) view.findViewById(R.id.ll_user_location);
            textViewLocation = (TextView) view.findViewById(R.id.tv_user_location);
        }
    }
}
