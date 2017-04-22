package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.Comparators;
import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.RepositoryContents;

import java.util.Collections;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private List<RepositoryContents> files;
    private Context context;
    private final OnClickListener onClickListener;

    public interface OnClickListener {
        void onFileClicked(RepositoryContents repositoryContents);
    }

    public FilesAdapter(OnClickListener listener) {
        onClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.file_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RepositoryContents file = files.get(position);
        String fileName = file.getName();
        String fileSize = Formatter.formatShortFileSize(context, file.getSize() * 1024);

        holder.textViewFileName.setText(fileName);
        if (file.getType().equals("dir")) {
            holder.imageViewFileIcon.setImageResource(R.drawable.ic_folder_grey_24dp);
        } else {
            holder.imageViewFileIcon.setImageResource(R.drawable.ic_file_grey_24dp);
            holder.textViewFileSize.setText(fileSize);
        }
    }

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    public void addFiles(List<RepositoryContents> files) {
        this.files = files;
        Collections.sort(files, new Comparators.FilesComparator());
        notifyItemRangeInserted(0, files.size());
    }

    public void clearFiles() {
        if (files == null) {
            return;
        }

        int itemCount = files.size();
        files.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView imageViewFileIcon;
        public final TextView textViewFileName;
        public final TextView textViewFileSize;

        public ViewHolder(View view) {
            super(view);
            imageViewFileIcon = (ImageView) view.findViewById(R.id.iv_file_icon);
            textViewFileName = (TextView) view.findViewById(R.id.tv_filename);
            textViewFileSize = (TextView) view.findViewById(R.id.tv_file_size);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int positionedClicked = getAdapterPosition();
            RepositoryContents fileClicked = files.get(positionedClicked);
            onClickListener.onFileClicked(fileClicked);
        }
    }
}
