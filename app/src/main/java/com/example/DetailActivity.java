package com.example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.example.works.ReadFileWorker;
import com.example.works.WorkerKey;

public class DetailActivity extends AppCompatActivity {
    TextView tvFileName;
    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvFileName = findViewById(R.id.tv_file_name);
        tvContent = findViewById(R.id.tv_content);

        tvContent.setMovementMethod(new ScrollingMovementMethod());

        String fileName = null;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            fileName = extra.getString("file_name");
        }

        if (fileName != null) {
            tvFileName.setText(fileName);

            WorkRequest readFileWorkRequest =
                    new OneTimeWorkRequest.Builder(ReadFileWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString(WorkerKey.KEY_FILE_NAME, fileName)
                                    .build())
                            .build();

            WorkManager.getInstance(this).enqueue(readFileWorkRequest);

            WorkManager.getInstance(this).getWorkInfoByIdLiveData(readFileWorkRequest.getId())
                    .observe(this, info -> {
                        String content = info.getOutputData().getString(WorkerKey.KEY_READ_RESULT);
                        tvContent.setText(content);
                    });

        }
        else {
            tvFileName.setText("Error");
            tvContent.setVisibility(View.GONE);
        }
    }
}