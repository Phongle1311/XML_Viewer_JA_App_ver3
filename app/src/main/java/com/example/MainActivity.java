package com.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.adapter.XmlFileAdapter;
import com.example.model.XmlFile;
import com.example.works.CopyFileWorker;
import com.example.works.LoadDbWorker;
import com.example.works.ParseIdWorker;
import com.example.works.WorkerKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    ArrayList<XmlFile>              xmlFiles;
    XmlFileAdapter                  mAdapter;
    RecyclerView                    rcvListFile;
    ActivityResultLauncher<Intent>  resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultLauncher = registerForActivityResult(new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        String path = uri.getPath();
                        String fileName = path.substring(path.lastIndexOf('/') + 1);

                        OneTimeWorkRequest parseIdWorkRequest =
                                new OneTimeWorkRequest.Builder(ParseIdWorker.class)
                                        .setInputData(new Data.Builder()
                                                .putString(WorkerKey.KEY_URI, uri.toString())
                                                .putString(WorkerKey.KEY_FILE_NAME, fileName)
                                                .build())
                                        .build();

                        OneTimeWorkRequest copyFileWorkRequest =
                                new OneTimeWorkRequest.Builder(CopyFileWorker.class).build();

                        WorkManager.getInstance(this)
                                .beginWith(parseIdWorkRequest)
                                .then(copyFileWorkRequest)
                                .enqueue();

                        WorkManager.getInstance(this).getWorkInfoByIdLiveData(copyFileWorkRequest.getId())
                                .observe(this, info -> {
                                    if (info != null && info.getState() == WorkInfo.State.SUCCEEDED) {
                                        boolean insert = info.getOutputData().getBoolean(WorkerKey.KEY_INSERTED, true);

                                        if (insert) {
                                            String instanceId = info.getOutputData().getString(WorkerKey.KEY_INSTANCE_ID);
                                            XmlFile newFile = new XmlFile(fileName, instanceId);
                                            xmlFiles.add(newFile);
                                            mAdapter.notifyItemInserted(xmlFiles.size() - 1);
                                        }
                                    }
                                });
                    }
                });

        xmlFiles = new ArrayList<>();
        rcvListFile = findViewById(R.id.rcv_list_file);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvListFile.setLayoutManager(linearLayoutManager);
        mAdapter = new XmlFileAdapter(xmlFiles, this::onClickToDetailPage);
        rcvListFile.setAdapter(mAdapter);

        WorkRequest loadDbWorkRequest =
                new OneTimeWorkRequest.Builder(LoadDbWorker.class).build();
        WorkManager.getInstance(this).enqueue(loadDbWorkRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loadDbWorkRequest.getId())
                .observe(this, info -> {
                    if (info != null && info.getState() == WorkInfo.State.SUCCEEDED) {
                        String json = info.getOutputData().getString(WorkerKey.KEY_LIST_FILE);

                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<Collection<XmlFile>>(){}.getType();
                        Collection<XmlFile> files = gson.fromJson(json, collectionType);
                        if (files != null) {
                            xmlFiles.addAll(files);
                            mAdapter.notifyItemRangeInserted(0, xmlFiles.size());
                        }
                    }
                });

        findViewById(R.id.btn_import).setOnClickListener(v -> openFile());
    }

    private void visibleRcv() {
        if (xmlFiles != null) {
            if (xmlFiles.size() != 0) {
                Log.d("xml", xmlFiles.size()+"");
                rcvListFile.setVisibility(View.VISIBLE);
                findViewById(R.id.empty_view).setVisibility(View.GONE);
                return;
            }
        }
        Log.d("xml", "gone");
        rcvListFile.setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        resultLauncher.launch(intent);
    }

    void onClickToDetailPage(XmlFile file) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("file_name", file.getName());
        startActivity(intent);
    }
}