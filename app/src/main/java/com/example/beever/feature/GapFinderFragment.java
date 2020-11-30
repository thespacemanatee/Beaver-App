package com.example.beever.feature;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beever.R;
import com.example.beever.admin.UserHelperClass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class GapFinderFragment extends Fragment implements Populatable{

    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private RecyclerView mRecyclerView;
    private String groupName;
    private String groupID;
    private TextInputEditText preferredTimeText;
    private CircularProgressButton searchBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gap_finder, container, false);

        Bundle bundle = this.getArguments();
//        groupImage = bundle.getString("imageUri");
        groupName = bundle.getString("groupName");
        groupID = bundle.getString("groupId");

        mRecyclerView = rootView.findViewById(R.id.gap_finder_recycler);
        preferredTimeText = rootView.findViewById(R.id.preferred_text);
        searchBtn = rootView.findViewById(R.id.search_button);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtn.startAnimation();
            }
        });

        return rootView;
    }

    @Override
    public void populateRecyclerView() {

    }

    static class GapAdapter extends RecyclerView.Adapter<GapAdapter.ViewHolder> {
        private ArrayList<Timestamp> timestamps;
        public static class ViewHolder extends RecyclerView.ViewHolder{
            private TextView timestampTitle;
            private TextView timestampContent;


            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                timestampTitle = itemView.findViewById(R.id.timestamp_text_title);
                timestampContent = itemView.findViewById(R.id.timestamp_text_content);
            }

            public TextView getTimestampTitle() {
                return timestampTitle;
            }

            public TextView getTimestampContent() {
                return timestampContent;
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         *
         * @param timestamps ArrayList<Timestamp> containing the data to populate views to be used
         * by RecyclerView.
         */
        public GapAdapter(ArrayList<Timestamp> timestamps) {

        }

        @NonNull
        @Override
        public GapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gap_finder_cells, parent, false);

            return new GapAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GapAdapter.ViewHolder holder, int position) {
            holder.getTimestampTitle().setText(timestamps.get(position).toString());
            holder.getTimestampContent().setText(timestamps.get(position).toString());

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}