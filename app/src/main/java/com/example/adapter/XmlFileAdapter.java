package com.example.adapter;

import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.XmlFile;

import java.util.ArrayList;

public class XmlFileAdapter extends RecyclerView.Adapter<XmlFileAdapter.XmlFileViewHolder> {
    final ArrayList<XmlFile> listFiles;
    final IClickItem iClick;

    public interface IClickItem {
        void onClickToDetailPage(XmlFile file);
    }

    public XmlFileAdapter(ArrayList<XmlFile> listFiles, IClickItem listener) {
        this.listFiles = listFiles;
        iClick = listener;
    }

    @Override
    public int getItemCount() {
        if (listFiles == null) return 0;
        return listFiles.size();
    }

    public XmlFile getFile(int i) {
        return listFiles.get(i);
    }
    public void setFile(int i, XmlFile file) {
        listFiles.set(i, file);
        notifyItemChanged(i);
    }

    @NonNull
    @Override
    public XmlFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_xml_file, parent, false);
        return new XmlFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull XmlFileViewHolder holder, int position) {
        XmlFile file = listFiles.get(position);
        if (file == null) return;
        holder.bind(file);
    }

    public class XmlFileViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstanceID;
        View item;

        public XmlFileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstanceID = itemView.findViewById(R.id.tv_instanceID);
            item = itemView;
        }

        public void bind(XmlFile file) {
            tvInstanceID.setText(file.getInstanceId());
            item.setOnClickListener(view -> iClick.onClickToDetailPage(file));
        }
    }
}