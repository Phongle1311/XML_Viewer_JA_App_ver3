package com.example.works;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ReadFileWorker extends Worker {
    Context mContext;

    public ReadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString(WorkerKey.KEY_FILE_NAME);
        String content = readFile(fileName);

        return Result.success(new Data.Builder()
                .putString(WorkerKey.KEY_READ_RESULT, content).build());
    }

    private String readFile(String fileName){
        File dir = mContext.getDir("official_data", Context.MODE_PRIVATE);
        File file = new File(dir, fileName);
        FileInputStream is = null;
        byte[] buffer = new byte[(int) file.length()];
        String result = "";
        int totalBytes = -1;

        try {
            is = new FileInputStream(file);
            totalBytes = is.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = "File not found";
        } catch (IOException e) {
            e.printStackTrace();
            result = e.toString();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (totalBytes!=-1)
            result = new String(buffer);
        return result;
    }
}
