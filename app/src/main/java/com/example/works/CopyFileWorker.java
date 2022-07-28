package com.example.works;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.database.XmlFileDatabase;
import com.example.model.XmlFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CopyFileWorker extends Worker {
    Context mContext;

    public CopyFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Uri uri = Uri.parse(getInputData().getString(WorkerKey.KEY_URI));
        String fileName = getInputData().getString(WorkerKey.KEY_FILE_NAME);
        String instanceId = getInputData().getString(WorkerKey.KEY_INSTANCE_ID);

        if (uri == null || fileName == null)
            return Result.failure();

        boolean insert = true;
        List<XmlFile> list =  XmlFileDatabase.getInstance(mContext).xmlFileDAO()
                .getByInstanceId(instanceId);
        if (list != null)
            if (!list.isEmpty()){       // if existed ID
                insert = false;
            }

        if (insert)
            XmlFileDatabase.getInstance(mContext).xmlFileDAO()
                    .insertFile(new XmlFile(fileName, instanceId));
        else
            XmlFileDatabase.getInstance(mContext).xmlFileDAO()
                    .updateByInstanceId(instanceId, fileName);


        InputStream is;
        OutputStream os;
        try {
            is = mContext.getContentResolver().openInputStream(uri);

            File dir = mContext.getDir("official_data", Context.MODE_PRIVATE);
            File outFile = new File(dir, fileName);
            os = new FileOutputStream(outFile);

            copyFile(is, os);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }

        return Result.success(new Data.Builder()
                .putString(WorkerKey.KEY_INSTANCE_ID, instanceId)
                .putBoolean(WorkerKey.KEY_INSERTED, insert)
                .build());
    }

    private void copyFile(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
    }
}
