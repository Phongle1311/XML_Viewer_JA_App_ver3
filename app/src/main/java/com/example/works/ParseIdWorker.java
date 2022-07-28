package com.example.works;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ParseIdWorker extends Worker {
    Context mContext;

    public ParseIdWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Uri uri = Uri.parse(getInputData().getString(WorkerKey.KEY_URI));
        String fileName = getInputData().getString(WorkerKey.KEY_FILE_NAME);
        Log.d("xml", "parse: " + uri.getPath());

        InputStream is;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.failure(new Data.Builder()
                    .putString(WorkerKey.KEY_FAILURE, e.toString())
                    .build());
        }

        String instanceId;
        try {
            instanceId = parseXML(is);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return Result.failure(new Data.Builder()
                    .putString(WorkerKey.KEY_FAILURE, e.toString())
                    .build());
        }

        if (instanceId == null)
            return Result.failure(new Data.Builder()
                    .putString(WorkerKey.KEY_FAILURE, "not found")
                    .build());

        return Result.success(new Data.Builder()
                .putString(WorkerKey.KEY_INSTANCE_ID, instanceId)
                .putString(WorkerKey.KEY_URI, uri.toString())
                .putString(WorkerKey.KEY_FILE_NAME, fileName)
                .build());
    }

    private String parseXML(InputStream is) throws XmlPullParserException, IOException {
        XmlPullParserFactory parserFactory;
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            return processParsing(parser);
    }

    private String processParsing(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType=-1;
        String nodeName;
        String data = null;

        boolean stop = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !stop) {
                eventType = parser.next();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        nodeName = parser.getName();
                        if (nodeName.equalsIgnoreCase("instanceID")) {
                            data = parser.nextText();
                            stop = true;
                        }
                        break;
                }
            }
        return data;
    }
}