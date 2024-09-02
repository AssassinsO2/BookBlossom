package com.example.bookblossom;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);

        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

            if (statusIndex != -1 && localUriIndex != -1) {
                int status = cursor.getInt(statusIndex);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    String fileUri = cursor.getString(localUriIndex);
                    if (fileUri != null) {
                        File file = new File(Uri.parse(fileUri).getPath());
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        context.startActivity(installIntent);
                    }
                } else {
                    Log.e(TAG, "Download failed with status: " + status);
                }
            } else {
                Log.e(TAG, "Required columns not found in the cursor");
            }
            cursor.close();
        } else {
            Log.e(TAG, "Cursor is null or empty");
        }
    }
}
