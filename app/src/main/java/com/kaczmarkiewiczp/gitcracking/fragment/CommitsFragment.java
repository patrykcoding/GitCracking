package com.kaczmarkiewiczp.gitcracking.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaczmarkiewiczp.gitcracking.R;

public class CommitsFragment extends Fragment {

    private View rootView;
    private Context context;

    public CommitsFragment() {
        // requires empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_commits, container, false);
        rootView = view;
        context = view.getContext();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_commits);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        // TODO adapter stuff
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
