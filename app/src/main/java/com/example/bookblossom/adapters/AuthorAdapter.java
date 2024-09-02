package com.example.bookblossom.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookblossom.models.Author;
import com.example.bookblossom.R;

import java.util.List;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {

    private final List<Author> authors;
    private OnItemClickListener listener;

    public AuthorAdapter(List<Author> authors) {
        this.authors = authors;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_author, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        Author author = authors.get(position);
        holder.textViewAuthorName.setText(author.getAuthorName());

        holder.itemView.setOnClickListener(v ->{
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return authors.size();
    }

    public static class AuthorViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAuthorName;

        public AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAuthorName = itemView.findViewById(R.id.author_name);
        }
    }
}
