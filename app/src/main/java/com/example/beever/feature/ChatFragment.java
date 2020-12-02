package com.example.beever.feature;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.beever.database.GroupEntry;
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
    private ArrayList<String> groupMemberIDs;
    private HashMap<String, String> groupMemberNames;
    private HashMap<String, String> groupMemberImgs;
    private BubblesAdapter adapter;

    private ArrayList<String> senderImg = new ArrayList<>();
    private ArrayList<String> texts = new ArrayList<>();
    private ArrayList<String> sender = new ArrayList<>();
    private ArrayList<Timestamp> times = new ArrayList<>();
    private SharedPreferences mSharedPref;
    private GroupEntry groupEntry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        Bundle bundle = this.getArguments();
        groupName = bundle.getString("groupName");
        groupId = bundle.getString("groupId");
        groupImage = bundle.getString("groupImage");
        groupMemberIDs = bundle.getStringArrayList("grpMemberIDs");
        groupMemberNames = (HashMap<String, String>) bundle.getSerializable("grpMemberNames");
        groupMemberImgs = (HashMap<String, String>) bundle.getSerializable("grpMemberImgs");
        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        //Show Chat Bubbles
        RecyclerView layout = rootView.findViewById(R.id.bubbles_area);
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

        GroupEntry.GetGroupEntry getGroupEntry = new GroupEntry.GetGroupEntry(groupId, 5000) {
            @Override
            public void onPostExecute() {
                groupEntry = getResult();
            }
        };
        getGroupEntry.start();;

        GroupEntry.GroupEntryListener groupEntryListener = new GroupEntry.GroupEntryListener(groupId,5000){
            public void onPreListening(){}

            public void onSetupFailure(){
                Toast.makeText(getContext(),"Group entry loading failed, please retry.", Toast.LENGTH_SHORT).show();

            }

            public void onListenerUpdate(){
                if (getStateChange()==StateChange.CHAT) {
                    populateRecyclerView();
                }
            }

        };
        groupEntryListener.start();

        return rootView;
    }

    private void addMessage(String text, Timestamp timestamp) {
        ChatEntry chatEntry = new ChatEntry(userID, text, null, timestamp);
        groupEntry.addChatEntry(chatEntry);

        GroupEntry.SetGroupEntry addMessage = new GroupEntry.SetGroupEntry(groupEntry, groupId, 5000) {
            @Override
            public void onPostExecute() {
                // if (isSuccessful()) Toast.makeText(getContext(), "Chat sent successfully", Toast.LENGTH_SHORT).show();
                // Do we need this? Usually in telegram etc we don't hv anything like this
            }
        };
        addMessage.start();
    }

    @Override
    public void populateRecyclerView() {

        texts.clear();
        senderImg.clear();
        times.clear();
        sender.clear();

        GroupEntry.GetGroupEntry getMessages = new GroupEntry.GetGroupEntry(groupId, 5000) {
            @Override
            public void onPostExecute() {
                ArrayList<ChatEntry> chats = getResult().getGroupChat();
                if (chats != null) {
                    for (ChatEntry entry: chats) {
                        texts.add(entry.getMessage());
                        times.add(entry.getTime());
                        sender.add(groupMemberNames.get(entry.getSender()));
                        senderImg.add(groupMemberImgs.get(entry.getSender()));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };
        getMessages.start();
    }

    class BubblesAdapter extends RecyclerView.Adapter<BubblesAdapter.ViewHolder> {

        String name = mSharedPref.getString("registeredName", "");
        int fromUser = 0;

        Context context;
        LayoutInflater inflater;
        BubblesAdapter(Context c) {
            context = c;
            inflater = LayoutInflater.from(c);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView memberImg;
            TextView text;
            TextView time;
            TextView member;

            public ViewHolder(View view) {
                super(view);
                if (fromUser == 1) {
                    memberImg = view.findViewById(R.id.chat_member_img_left);
                    text = view.findViewById(R.id.bubble_left);
                    time = view.findViewById(R.id.bubble_time_left);
                    member = view.findViewById(R.id.bubble_name_left);
                } else {
                    memberImg = view.findViewById(R.id.chat_member_img);
                    text = view.findViewById(R.id.bubble);
                    time = view.findViewById(R.id.bubble_time);
                    member = view.findViewById(R.id.bubble_name);
                }
            }

            public TextView getMember() {
                return member;
            }

            public ShapeableImageView getMemberImg() {
                return memberImg;
            }

            public TextView getText() {
                return text;
            }

            public TextView getTime() {
                return time;
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (getMemberData(i).equals(name)) {
                fromUser = 1;
            } else {
                fromUser = 0;
            }
            return fromUser;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            if (fromUser == 1) {
                view = inflater.inflate(R.layout.chat_bubbles_left, viewGroup, false);
            } else {
                view = inflater.inflate(R.layout.chat_bubbles, viewGroup, false);
            }
            return new ViewHolder(view);
        }

        public String getImgData(int i) {
            return senderImg.get(i);
        }

        public String getTextData(int i) {
            return texts.get(i);
        }

        public String getTimeData(int i) {
            return times.get(i).toDate().toString().substring(0, 19);
        }

        public String getMemberData(int i) {
            return sender.get(i);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            String img = getImgData(i);
            String text = getTextData(i);
            String time = getTimeData(i);
            String member = getMemberData(i);

            if (img == null) {
                Glide.with(context).load(R.drawable.pink_circle).centerCrop().into(viewHolder.getMemberImg());
            } else {
                Glide.with(context).load(img).centerCrop().into(viewHolder.getMemberImg());
            }
            viewHolder.getText().setText(text);
            viewHolder.getTime().setText(time);
            viewHolder.getMember().setText(member);
        }

        @Override
        public int getItemCount() {
            return texts.size();
        }
    }


}