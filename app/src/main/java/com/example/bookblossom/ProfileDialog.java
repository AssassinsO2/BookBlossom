package com.example.bookblossom;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileDialog extends Dialog {

    private static final String TAG = "ProfileDialog";
    private final String userEmail;
    private final String userName;
    private final String imageUrl;

    public ProfileDialog(Context context, String userEmail, String userName, String imageUrl) {
        super(context, R.style.CustomDialogTheme);
        this.userEmail = userEmail;
        this.userName = userName;
        this.imageUrl = imageUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_profile);

        TextView emailTextView = findViewById(R.id.profile_email);
        TextView nameTextView = findViewById(R.id.profile_name);
        ImageView profileImageView = findViewById(R.id.profile_image);

        Log.e(TAG, "Profile picture" + imageUrl);
        emailTextView.setText(userEmail);
        nameTextView.setText(getContext().getString(R.string.profile_name_template, userName));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.account_circle_black)
                    .error(R.drawable.account_circle_black)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.account_circle_black);
        }

        Button manageAccount = findViewById(R.id.manage_account_button);
        Button signOut = findViewById(R.id.sign_out_button);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 30; // Set the x-position
            params.y = 30; // Set the y-position
            window.setAttributes(params);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        manageAccount.setOnClickListener(v -> {
            Context context = getContext();
            Intent intent = new Intent(context, ManageAccount.class);
            intent.putExtra("userEmail", userEmail);
            context.startActivity(intent);
        });

        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Context context = getContext();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            dismiss();
        });
    }
}
