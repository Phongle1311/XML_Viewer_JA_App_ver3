package com.example.works;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.database.XmlFileDatabase;
import com.example.model.XmlFile;
import com.google.gson.Gson;

import java.util.List;

public class LoadDbWorker extends Worker {
    Context mContext;

    public LoadDbWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        List<XmlFile> files = XmlFileDatabase.getInstance(mContext).xmlFileDAO().getAllFiles();
        Gson gson = new Gson();
        String json = gson.toJson(files);

        return Result.success(new Data.Builder()
                .putString(WorkerKey.KEY_LIST_FILE, json)
                .build());
    }
}
