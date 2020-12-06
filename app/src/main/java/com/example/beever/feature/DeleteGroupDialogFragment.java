package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

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
    private int counter = 0;

    public DeleteGroupDialogFragment(String groupId, List<Object> members) {
        this.groupId = groupId;
        this.members = members;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_groups, null);

        GroupsFragment fragment = new GroupsFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete group?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (Object member: members) {

                            int full = members.size();
                            UserEntry.UpdateUserEntry deleteGroupId = new UserEntry.UpdateUserEntry((String) member,
                                    UserEntry.UpdateUserEntry.FieldChange.GROUPS_REMOVE, groupId, 5000) {
                                @Override
                                public void onPostExecute() {
                                    Log.d("DELETE GROUP ID", "onPostExecute: " + "SUCCESS");

                                    counter++;

                                    if (counter == full) {
                                        fStore.collection("groups").document(groupId).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("DELETE GROUP", "DocumentSnapshot successfully deleted!");
                                                        transaction.replace(R.id.fragment_container, fragment).commit();
                                                        Utils utils = new Utils(v.getContext());
                                                        utils.fadeIn();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("DELETE GROUP", "Error deleting document", e);
                                                    }
                                                });
                                    }
                                }
                            };
                            deleteGroupId.start();
                        }
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
