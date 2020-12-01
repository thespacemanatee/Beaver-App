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

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.database.ChatEntry;
import com.example.beever.database.UserEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment implements Populatable{

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private String userID = fAuth.getUid();

    private String groupName;
    private String groupId;
    private String groupImage;
    private BubblesAdapter adapter;

    private ArrayList<String> senderImg = new ArrayList<>();
    private ArrayList<String> texts = new ArrayList<>();
    private ArrayList<String> sender = new ArrayList<>();
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
                    Log.d("CHECK NEW TEXT", newText);
                    addMessage(newText, new Timestamp(new Date()));
                    editText.setText("");
                }
            }
        });

        return rootView;
    }

    private void addMessage(String text, Timestamp timestamp) {
        ChatEntry chatEntry = new ChatEntry(userID, text, null, timestamp);
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
        senderImg.clear();
        times.clear();
        sender.clear();

        DocumentReference documentReference = fStore.collection("groups").document(groupId);
        Log.d("testId",groupId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("test2",document.toString());
                        List<Map<String, Object>> chatList = (ArrayList<Map<String, Object>>) document.get("chat");
                        if (chatList != null) {
                            Log.d("test",chatList.toString());
                            for (Object o: chatList) {
                                ChatEntry chatEntry = new ChatEntry(o);
                                texts.add(chatEntry.getMessage());
                                times.add(chatEntry.getTime());
                                senderImg.add("https://firebasestorage.googleapis.com/v0/b/beaver-app-7998c.appspot.com/o/groups%2FH8DKr5zp34Sf5xHVhwD6TJljIWh2CreateWorks%2Fgroup_image.jpg?alt=media&token=ae707bd1-ccb1-4154-a254-01f3b1d99b0f");
                                sender.add(mSharedPref.getString("registeredName", ""));

//                                UserEntry.GetUserEntry userGetter = new UserEntry.GetUserEntry(chatEntry.getSender(), 5000) {
//                                    @Override
//                                    public void onPostExecute() {
//                                        Log.d("ENTERS USER ENTRY", "please");
//                                        Log.d("USER ENTRY", getResult().toString());
//                                        if (getResult().getDisplay_picture() == null) {
//                                            senderImg.add("null");
//                                        } else {
//                                            senderImg.add(getResult().getDisplay_picture());
//                                        }
//                                        sender.add(getResult().getName());
//                                    }
//                                };
//                                userGetter.start();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }


    class BubblesAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;
        BubblesAdapter(Context c) {
            context = c;
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() { return texts.size(); }

        @Override
        public Object getItem(int i) { return i; }

        @Override
        public long getItemId(int i) { return i; }

        String prevSender = "";
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            //ViewHolder for smoother scrolling
           BubblesViewHolder viewHolder;

            //Set variables to allow multiple access of same image and text
            String img = senderImg.get(i);
            String txt = texts.get(i);
            // String timestamp = times.get(i).toDate().toString().substring(0, 19);
            String timestamp = new Timestamp(new Date()).toString();
            String senderName = sender.get(i);
            if (senderName == null) {
                senderName = "NULL";
            }

            String name = mSharedPref.getString("registeredName", "");
            if (prevSender != senderName) {
                //If view (View to populate GridView cells) not loaded before,
                //create new ViewHolder to hold view
                viewHolder = new BubblesViewHolder();

                //Inflate the layout for GridView cells (created as a Fragment)
                if (senderName.equals(name)) {
                    view = inflater.inflate(R.layout.chat_bubbles_left, null);

                    //Get ImageButton and TextView to populate
                    viewHolder.memberImg = view.findViewById(R.id.chat_member_img_left);
                    viewHolder.text = view.findViewById(R.id.bubble_left);
                    viewHolder.time = view.findViewById(R.id.bubble_time_left);
                    viewHolder.member = view.findViewById(R.id.bubble_name_left);

                } else {
                    view = inflater.inflate(R.layout.chat_bubbles, null);

                    //Get ImageButton and TextView to populate
                    viewHolder.memberImg = view.findViewById(R.id.chat_member_img);
                    viewHolder.text = view.findViewById(R.id.bubble);
                    viewHolder.time = view.findViewById(R.id.bubble_time);
                    viewHolder.member = view.findViewById(R.id.bubble_name);
                }

            } else {
                //If view loaded before, get view's tag and cast to ViewHolder
                viewHolder = (BubblesViewHolder)view.getTag();
            }

            //setImageResource for ImageButton and setText for TextView
            if (img.equals("null")) {
                Glide.with(context).load(R.drawable.pink_circle).centerCrop().into(viewHolder.memberImg);
            } else {
                Glide.with(context).load(img).centerCrop().into(viewHolder.memberImg);
            }
            viewHolder.text.setText(txt);
            viewHolder.time.setText(timestamp);
            viewHolder.member.setText(senderName);

            return view;
        }

        //To reduce reloading of same layout
        class BubblesViewHolder {
            ShapeableImageView memberImg;
            TextView text;
            TextView time;
            TextView member;
        }
    }


}