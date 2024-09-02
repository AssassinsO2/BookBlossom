package com.example.bookblossom.models;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class UpdateChecker {

    private static final String TAG = "UpdateChecker";
    private Context context;

    public UpdateChecker(Context context) {
        this.context = context;
    }

    public void checkForUpdates() {
        String versionCheckUrl = "http://192.168.18.48:8000/version.json";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(versionCheckUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to check for updates", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String latestVersion = jsonObject.getString("latestVersion");
                        Log.e(TAG, "Latest Version: " + latestVersion);
                        String currentVersion = "1.0";  // Hardcoded version for testing

                        if (!currentVersion.equals(latestVersion)) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                try {
                                    Log.e(TAG, "Update Prompt generated");
                                    promptForUpdate(jsonObject.getString("url"));
                                } catch (JSONException e) {
                                    Log.e(TAG, "Failed to parse JSON", e);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse JSON", e);
                    }
                }
            }
        });
    }

    private void promptForUpdate(String apkUrl) {
        Log.e(TAG, "Inside Update Prompt function");
        Log.e(TAG, "Apk Url: " + apkUrl);
        new AlertDialog.Builder(context)
                .setTitle("Update Available")
                .setMessage("A new version of the app is available. Would you like to update now?")
                .setPositiveButton("Yes", (dialog, which) -> downloadAndInstallUpdate(apkUrl))
                .setNegativeButton("No", null)
                .show();
    }

    private void downloadAndInstallUpdate(String apkUrl) {
        Log.e(TAG, "Inside downloadAndInstallUpdate function");

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Downloading Update");
        request.setDescription("Downloading the latest version of the app");
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "bookblossom.apk");

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (manager != null) {
            long downloadId = manager.enqueue(request);
            Log.e(TAG, "Download Enqueued with ID: " + downloadId);

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Log.e(TAG, "Download Complete Broadcast Received");
                    long receivedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (receivedDownloadId == downloadId) {
                        checkDownloadStatus(manager, downloadId); // Pass the manager to the method
                        Uri uri = manager.getUriForDownloadedFile(downloadId);
                        if (uri != null) {
                            installApk(uri);
                        } else {
                            Log.e(TAG, "Failed to get URI for downloaded file");
                        }
                    } else {
                        Log.e(TAG, "Received download ID does not match the enqueued ID");
                    }
                    context.unregisterReceiver(this);
                }
            };

            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else {
            Log.e(TAG, "DownloadManager is null");
        }
    }

    private void installApk(Uri apkUri) {
        Log.e(TAG, "Inside installApk function");

        // Check if the file exists before trying to install
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "bookblossom.apk");
        if (file.exists()) {
            Log.e(TAG, "APK file exists: " + file.getAbsolutePath());

            Uri contentUri = FileProvider.getUriForFile(context, "com.example.bookblossom.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            Log.e(TAG, "APK file does not exist.");
        }
    }

    private void checkDownloadStatus(DownloadManager manager, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = manager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.e(TAG, "Download completed successfully.");
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.e(TAG, "Download failed.");
                    break;
                case DownloadManager.STATUS_PAUSED:
                    Log.e(TAG, "Download paused.");
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.e(TAG, "Download pending.");
                    break;
            }
        } else {
            Log.e(TAG, "Failed to query download status.");
        }
        cursor.close();
    }

}
