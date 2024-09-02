package com.example.bookblossom;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.bookblossom.databinding.DialogCustomUpdateBinding;
import com.example.bookblossom.models.AppFileData;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AppUpdateService {
    private static final String TAG = "AppUpdateService";
    private Context context = null;
    private String strApkFileUrl = "";

    public void DoUpdate(Context context) {
        this.context = context;
        String strServerAddress = "147.185.221.21";
        int port = 45578;
        String strUpdateUrl = String.format(Locale.getDefault(), "http://%s:%d", strServerAddress, port);
        Log.d(TAG, "AppUpdateService.DoUpdate(): " + strUpdateUrl);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(strUpdateUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IAppUpdateService intAppUpdate = retrofit.create(IAppUpdateService.class);

        Call<AppFileData> pServerFileInfo = intAppUpdate.getLatestAppInfo("extraKey", "extraValue");

        pServerFileInfo.enqueue(new Callback<AppFileData>() {
            @Override
            public void onResponse(@NonNull Call<AppFileData> call, @NonNull Response<AppFileData> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "AppUpdateService.DoUpdate().onResponse() : " + response.code());
                    return;
                }
                AppFileData pFileInfoOnServer = response.body();
                AppFileData pFileInfoOnLocal = getLocalAppInfo();

                strApkFileUrl = String.format(Locale.getDefault(), "http://%s:%d/%s", strServerAddress, port, "bookblossom.apk");

                if (pFileInfoOnServer != null && (!pFileInfoOnServer.getVersionName().equals(pFileInfoOnLocal.getVersionName()) ||
                        pFileInfoOnServer.getVersionCode() > pFileInfoOnLocal.getVersionCode())) {
                    Log.d(TAG, "AppUpdateService.DoUpdate().onResponse() : Update Start ==========>");
                    DownloadAppDialog();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppFileData> call, @NonNull Throwable throwable) {
                Log.e(TAG, "AppUpdateService.DoUpdate().onFailure() : " + throwable.getMessage());
            }
        });
    }

    private AppFileData getLocalAppInfo() {
        AppFileData pReturn = new AppFileData();

        final PackageManager pm = this.context.getPackageManager();
        final String strPackageName = this.context.getPackageName();

        try {
            PackageInfo pPI = pm.getPackageInfo(strPackageName, 0);
            pReturn.setVersionName(pPI.versionName);
            pReturn.setVersionCode(pPI.versionCode);
        } catch (Exception e) {
            Log.e(TAG, "getLocalAppInfo().Exception : " + e.getMessage());
        }

        return pReturn;
    }

    private void DownloadAppDialog() {
        DialogCustomUpdateBinding customUpdateBinding = DialogCustomUpdateBinding.inflate(LayoutInflater.from(context));

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.UpdateDialog);
        builder.setView(customUpdateBinding.getRoot());

        AlertDialog alertDialog = builder.setCancelable(false).create();

        // Find and set the button click listener
        customUpdateBinding.dialogButtonUpdate.setOnClickListener(v -> {
            // Start DownloadActivity with the download URL
            redirectStore(strApkFileUrl);
            Toast.makeText(context, "Please Wait Download Started...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "AppUpdateService.DownloadAppDialog() : Click Update");
            //alertDialog.dismiss(); // Dismiss the dialog after clicking update
        });

        // Show the dialog
        alertDialog.show();
    }

    private void redirectStore(String strUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(strUrl));
        request.setTitle("BookBlossom Update");
        request.setDescription("Downloading new version...");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bookblossom.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    // Retrofit interface for fetching the latest app information
    interface IAppUpdateService {
        @GET("version.json")
        Call<AppFileData> getLatestAppInfo(@Query("extraKey") String strExtraKey, @Query("extraValue") String strExtraValue);
    }
}


