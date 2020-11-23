package com.example.beever.feature;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.admin.UserHelperClass;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> email = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textViewName;
        private final TextView textViewEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.added_name);
            textViewEmail = itemView.findViewById(R.id.added_email);
        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public TextView getTextViewEmail() {
            return textViewEmail;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public UsersAdapter(ArrayList<UserHelperClass> array) {
        for (UserHelperClass user: array) {
            Log.i("HELLLLO MY NAMES NINO", user.getName());
            Log.i("HELLLLO MY EMAILS NINO", user.getEmail());
            this.name.add(user.getName());
            this.email.add(user.getEmail());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_users, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        holder.getTextViewName().setText(name.get(position));
        holder.getTextViewEmail().setText(email.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }
}
