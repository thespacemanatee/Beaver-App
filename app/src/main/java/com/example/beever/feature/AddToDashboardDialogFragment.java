package com.example.beever.feature;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.beever.database.UserEntry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddToDashboardDialogFragment extends DialogFragment {

    private static final String TAG = "DIALOG";
    private String selectedGrpId;
    private String userID;

    public AddToDashboardDialogFragment(String selectedGrpId, String userID) {
        this.selectedGrpId = selectedGrpId;
        this.userID = userID;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Add to favourites?")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "onClick: " + userID);
                        UserEntry.GetUserEntry addToDashboard = new UserEntry.GetUserEntry(userID, 5000) {
                            @Override
                            public void onPostExecute() {
                                List<Object> groups = new ArrayList<>();
                                groups.add(selectedGrpId);
                                UserEntry userEntry = getResult();
                                List<Object> current = userEntry.getDashboard_grps();
                                for (int i = 0; i < current.size(); i++) {
                                    if (current.get(i) != null) {
                                        if (current.get(i).equals(selectedGrpId)) {
                                            break;
                                        }
                                    } else {
                                        userEntry.assignDashboardGrp(i, selectedGrpId);
                                        Log.d(TAG, "onPostExecute: " + groups.toString());
                                        UserEntry.SetUserEntry setUserEntry = new UserEntry.SetUserEntry(userEntry, userID, 5000) {
                                            @Override
                                            public void onPostExecute() {
                                                Log.d(TAG, "onPostExecute: " + "success!");
                                            }
                                        };
                                        setUserEntry.start();
                                        break;
                                    }
                                }

                            }
                        };
                        addToDashboard.start();
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
