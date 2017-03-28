package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kaczmarkiewiczp.gitcracking.R;

import java.util.ArrayList;


public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    private ArrayList<String> userIconList;
    private ArrayList<String> userNameList;
    private ArrayList<String> userLoginList;
    private ArrayList<Boolean> isRemovableList;
    private Context context;
    private final ListItemClickListener onClickListener;

    public interface ListItemClickListener {
        void onListItemClick(String userName);
    }

    public AccountsAdapter(ListItemClickListener listener) {
        userIconList = new ArrayList<>();
        userNameList = new ArrayList<>();
        userLoginList = new ArrayList<>();
        isRemovableList = new ArrayList<>();
        onClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.accounts_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String userIconUrl = userIconList.get(position);
        String userName = userNameList.get(position);
        String userLogin = userLoginList.get(position);

        Glide
                .with(context)
                .load(userIconUrl)
                .error(context.getDrawable(android.R.drawable.sym_def_app_icon))
                .placeholder(R.drawable.progress_animation)
                .crossFade()
                .into(holder.imageViewUserIcon);
        holder.textViewUserLogin.setText(userLogin);
        if (userName != null && !userName.isEmpty()) {
            holder.textViewUserName.setVisibility(View.VISIBLE);
            holder.textViewUserName.setText(userName);
        } else {
            holder.textViewUserName.setVisibility(View.GONE);
        }
        if (isRemovableList.get(position)) {
            holder.imageViewRemove.setImageResource(R.drawable.ic_delete_forever_black_24dp);
            holder.imageViewRemove.setClickable(true);
            holder.imageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.onClick(v);
                }
            });
        } else {
            holder.imageViewRemove.setImageResource(R.drawable.ic_delete_forever_grey_24dp);
            holder.imageViewRemove.setClickable(false);
            holder.imageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Can't remove currently logged-in user", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (userLoginList == null) {
            return 0;
        }
        return userLoginList.size();
    }

    public void addUser(String userLogin, @Nullable String userName, String userIconUrl, boolean canRemove) {
        userLoginList.add(userLogin);
        if (userName != null) {
            userNameList.add(userName);
        } else {
            // user's name can be null but we still want all three lists to grow at the same rate
            userNameList.add("");
        }
        isRemovableList.add(canRemove);
        userIconList.add(userIconUrl);
        notifyItemInserted(userLoginList.size() - 1);
    }

    public void removeUser(String login) {
        int position = userLoginList.indexOf(login);
        if (position == -1) {
            return;
        }
        userLoginList.remove(position);
        userNameList.remove(position);
        userIconList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeUsers() {
        int size = userLoginList.size();

        userLoginList.clear();
        userNameList.clear();
        userIconList.clear();

        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView imageViewUserIcon;
        public final TextView textViewUserName;
        public final TextView textViewUserLogin;
        public final ImageView imageViewRemove;

        public ViewHolder(View view) {
            super(view);
            imageViewUserIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
            textViewUserName = (TextView) view.findViewById(R.id.tv_user_name);
            textViewUserLogin = (TextView) view.findViewById(R.id.tv_user_login);
            imageViewRemove = (ImageView) view.findViewById(R.id.iv_remove);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            String userName = userLoginList.get(clickedPosition);
            onClickListener.onListItemClick(userName);
        }
    }
}
