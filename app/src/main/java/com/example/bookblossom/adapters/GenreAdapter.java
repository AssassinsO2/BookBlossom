package com.example.bookblossom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookblossom.models.Genre;
import com.example.bookblossom.R;

import java.util.ArrayList;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    private final Context context;
    private final ArrayList<Genre> genreList;
    private OnItemClickListener onItemClickListener;

    public GenreAdapter(Context context, ArrayList<Genre> genreList) {
        this.context = context;
        this.genreList = genreList;
    }

    // Interface for click listener
    public interface OnItemClickListener {
        void onItemClick(String genreId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genreList.get(position);
        holder.bind(genre.getGenre());

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(genre.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {

        private final TextView genreNameTextView;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreNameTextView = itemView.findViewById(R.id.category_name);
        }

        public void bind(String genre) {
            genreNameTextView.setText(genre);
        }
    }

    public void updateGenres(ArrayList<Genre> newGenres) {
        this.genreList.clear();
        this.genreList.addAll(newGenres);
        notifyDataSetChanged();
    }
}
