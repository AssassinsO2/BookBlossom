package com.example.bookblossom;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookblossom.models.AppFileData;


public class AppInfoFragment extends Fragment {

    private static final String TAG = "AppInfoFragment";

    public AppInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppFileData pFileInfoOnLocal = getLocalAppInfo();
        // Get the TextView for version and set the version using String.format
        TextView textAppVersion = view.findViewById(R.id.textAppVersion);
        String versionName = pFileInfoOnLocal.getVersionName();
        textAppVersion.setText(String.format("Version %s", versionName));
    }

    private AppFileData getLocalAppInfo() {
        AppFileData pReturn = new AppFileData();

        final PackageManager pm = requireContext().getPackageManager();
        final String strPackageName = requireContext().getPackageName();

        try {
            PackageInfo pPI = pm.getPackageInfo(strPackageName, 0);
            pReturn.setVersionName(pPI.versionName);
            pReturn.setVersionCode(pPI.versionCode);
        } catch (Exception e) {
            Log.e(TAG, "getLocalAppInfo().Exception : " + e.getMessage());
        }

        return pReturn;
    }
}
