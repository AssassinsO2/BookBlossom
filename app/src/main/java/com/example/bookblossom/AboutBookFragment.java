package com.example.bookblossom;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutBookFragment extends Fragment {

    private static final String TAG = "AboutBookFragment";

    private String pdfUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_book, container, false);

        TextView tvBookName = view.findViewById(R.id.tvBookName);
        TextView tvAuthor = view.findViewById(R.id.tvAuthor);
        TextView tvGenre = view.findViewById(R.id.tvGenre);
        TextView tvChapters = view.findViewById(R.id.tvChapters);
        TextView tvYear = view.findViewById(R.id.tvYear);
        TextView tvPages = view.findViewById(R.id.tvPages);
        TextView tvDescription = view.findViewById(R.id.tvDescription);

        Button btnReadMore = view.findViewById(R.id.startReading);

        Bundle args = getArguments();
        if (args != null) {
            tvBookName.setText(args.getString("bookName"));
            tvAuthor.setText(args.getString("authorName"));
            tvGenre.setText(args.getString("genre"));
            tvChapters.setText(getString(R.string.number_of_chapters, args.getString("chapters")));
            tvPages.setText(getString(R.string.number_of_pages, args.getString("pages")));
            tvYear.setText(getString(R.string.year_released, args.getString("year")));
            tvDescription.setText(args.getString("description"));
            pdfUrl = args.getString("pdfUrl");
        } else {
            Log.d(TAG, "Arguments are null");
        }

        btnReadMore.setOnClickListener(v -> {
            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                Intent chooser = Intent.createChooser(intent, "Open PDF");

                try {
                    startActivity(chooser);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), R.string.no_pdf_app_found, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.pdf_not_available, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
