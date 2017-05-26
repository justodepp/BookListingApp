package com.example.android.booklistingapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {

    ArrayList<Book> mBook;
    private OnItemClickListener mListener;
    MainActivity mContext;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public BookRecyclerAdapter(MainActivity context, ArrayList<Book> book, OnItemClickListener listener){
        mContext = context;
        mBook = book;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookRecyclerAdapter.ViewHolder holder, int position) {
        Book book = mBook.get(position);

        holder.bookTitleTextView.setText(book.getTitleId());
        holder.bookSubtitleTextView.setText(book.getSubtitleId());
        holder.bookAuthorTextView.setText(book.getAuthorId());
        holder.bookPublisherTextView.setText(book.getPublisherId());

        //we use Picasso Library to convert the url from JSONObject imageLinks to a image(@thumbnail)
        Picasso.with(mContext).load(book.getThumbnailId()).into(holder.bookThumbnailImageView);
    }

    @Override
    public int getItemCount() {
        return mBook.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView bookThumbnailImageView;
        TextView bookTitleTextView;
        TextView bookSubtitleTextView;
        TextView bookAuthorTextView;
        TextView bookPublisherTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            bookThumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            bookTitleTextView = (TextView) itemView.findViewById(R.id.book_title);
            bookSubtitleTextView = (TextView) itemView.findViewById(R.id.book_subtitle);
            bookAuthorTextView = (TextView) itemView.findViewById(R.id.author);
            bookPublisherTextView = (TextView) itemView.findViewById(R.id.publisher);
        }

        public void bind(final Book book, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(book);
                }
            });
        }

    }

    public void clear(){
        mBook.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Book> book){
        mBook.addAll(book);
        notifyDataSetChanged();
    }
}
