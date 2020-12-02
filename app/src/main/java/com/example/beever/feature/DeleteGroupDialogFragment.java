package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.beever.R;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.UserEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeleteGroupDialogFragment extends DialogFragment {

    private static final String TAG = "DIALOG";
    private String groupId;
    private List<Object> members;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public DeleteGroupDialogFragment(String groupId, List<Object> members) {
        this.groupId = groupId;
        this.members = members;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete group?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (Object member: members) {
                            UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry((String) member, 5000) {
                                @Override
                                public void onPostExecute() {
                                    UserEntry userEntry = getResult();
                                    userEntry.removeGroupId(groupId);
                                    UserEntry.SetUserEntry deleteGroupId = new UserEntry.SetUserEntry(userEntry, (String) member, 5000) {
                                        @Override
                                        public void onPostExecute() {
                                            Log.d("DELETE GROUP ID", "onPostExecute: " + "SUCCESS");
                                        }
                                    };
                                    deleteGroupId.start();
                                }
                            };
                            getUserEntry.start();
                        }

                        fStore.collection("groups").document(groupId).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DELETE GROUP", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DELETE GROUP", "Error deleting document", e);
                                    }
                                });

                        GroupsFragment fragment = new GroupsFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.fragment_container, fragment).commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
