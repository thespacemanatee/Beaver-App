package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beever.R;
import com.example.beever.database.ChatEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment implements Populatable{

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String groupName;
    private String groupId;
    private String groupImage;
    private BubblesAdapter adapter;

    private ArrayList<Integer> grpMemberImg = new ArrayList<>();
    private ArrayList<String> texts = new ArrayList<>();
    private ArrayList<Timestamp> times = new ArrayList<>();
    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        Bundle bundle = this.getArguments();
        groupName = bundle.getString("groupName");
        groupId = bundle.getString("groupId");
        groupImage = bundle.getString("groupImage");
        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        texts.clear();
        grpMemberImg.clear();
        times.clear();

        //Show Chat Bubbles
        ListView layout = rootView.findViewById(R.id.bubbles_area);
        adapter = new BubblesAdapter(getContext());
        layout.setAdapter(adapter);
        populateRecyclerView();

        //Create Send Button
        ImageButton sendButton = rootView.findViewById(R.id.send_button);
        EditText editText = rootView.findViewById(R.id.send_message);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = editText.getText().toString();
                if (!newText.equals("")) {
//                    grpMemberImg.add(R.drawable.beever_logo);
//                    texts.add(newText);
                    Log.d("CHECK NEW TEXT", newText);
//                    times.add(new Timestamp(new Date()));
                    addMessage(newText, new Timestamp(new Date()));
                    editText.setText("");
//                    adapter = new BubblesAdapter(getContext());
//                    layout.setAdapter(adapter);
                }
            }
        });

        return rootView;
    }

    private void addMessage(String text, Timestamp timestamp) {
        String name = mSharedPref.getString("registeredName", "");
        ChatEntry chatEntry = new ChatEntry(name, text, null, timestamp);
        DocumentReference documentReference = fStore.collection("groups").document(groupId);
        documentReference.update("chat", FieldValue.arrayUnion(chatEntry)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getContext(), "Chat sent successfully", Toast.LENGTH_SHORT).show();
                populateRecyclerView();

            }
        });
    }

    @Override
    public void populateRecyclerView() {
        texts.clear();
        grpMemberImg.clear();
        times.clear();
        DocumentReference documentReference = fStore.collection("groups").document(groupId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<Map<String, Object>> chatList = (ArrayList<Map<String, Object>>) document.get("chat");
                        if (chatList != null) {
                            for (Object o: chatList) {
                                ChatEntry chatEntry = new ChatEntry(o);
                                texts.add(chatEntry.getMessage());
                                grpMemberImg.add(R.drawable.pink_circle);
                                times.add(chatEntry.getTime());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }


    class BubblesAdapter extends BaseAdapter {

        LayoutInflater inflater;
        BubblesAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() { return texts.size(); }

        @Override
        public Object getItem(int i) { return i; }

        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            //ViewHolder for smoother scrolling
           BubblesViewHolder viewHolder;

            if (view == null) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new BubblesViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                view = inflater.inflate(R.layout.chat_bubbles, null);

                //Get ImageButton and TextView to populate
                viewHolder.memberImg = view.findViewById(R.id.chat_member_img);
                viewHolder.text = view.findViewById(R.id.bubble);
                viewHolder.time = view.findViewById(R.id.bubble_time);

                //Tag to reference
                view.setTag(viewHolder);

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (BubblesViewHolder)view.getTag();
            }

            //Set variables to allow multiple access of same image and text
            int img = grpMemberImg.get(i);
            String txt = texts.get(i);
            String timestamp = times.get(i).toDate().toString().replace("GMT 2020", "");

            //setImageResource for ImageButton and setText for TextView
            viewHolder.memberImg.setImageResource(img);
            viewHolder.text.setText(txt);
            viewHolder.time.setText(timestamp);

            return view;
        }

        //To reduce reloading of same layout
        class BubblesViewHolder {
            ShapeableImageView memberImg;
            TextView text;
            TextView time;
        }
    }


}