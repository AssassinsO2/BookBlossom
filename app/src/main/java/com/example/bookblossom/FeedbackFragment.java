package com.example.bookblossom;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FeedbackFragment extends DialogFragment {

    public FeedbackFragment(){
        //empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        showFeedbackDialog();
        return view;
    }

    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Feedback about BookBlossom")
                .setMessage("If you have questions, suggestions or require troubleshooting, write to us at support@bookblossom.org and we will try to figure it all out.")
                .setPositiveButton("WRITE AN EMAIL", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "support@bookblossom.org", null));
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                })
                .setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
