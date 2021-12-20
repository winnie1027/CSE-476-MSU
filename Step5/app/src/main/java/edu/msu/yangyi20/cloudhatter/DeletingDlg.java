package edu.msu.yangyi20.cloudhatter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import edu.msu.yangyi20.cloudhatter.Cloud.Cloud;
import edu.msu.yangyi20.cloudhatter.Cloud.Models.Hat;

public class DeletingDlg  extends DialogFragment {

    private String catId;
    private String catName;

    private final static String ID = "id";
    private volatile boolean cancel = false;
    public void setCatId(String catID) {
        catId = catID;
    }

    public void setCatName(String catNAME) { catName = catNAME; }

    /* Save the instance state*/
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(ID, catId);
    }

    /* Called when the view is destroyed.*/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancel = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {

        cancel = false;
        if(bundle != null) {
            catId = bundle.getString(ID);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final HatterView view = (HatterView)getActivity().findViewById(R.id.hatterView);

        String areYouSure = getString(R.string.delete_sure);

        builder.setMessage(areYouSure + " " + catName + "?");

        // Set the title
        builder.setTitle(R.string.deleting);

        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // Create a cloud object and get the XML
                                Cloud cloud = new Cloud();
                                final boolean works = cloud.deleteFromCloud(catId);
                                if(!works) {
                                    view.post(new Runnable() {

                                        @Override
                                        public void run() {


                                        }

                                    });
                                }
                            }

                        }).start();
                    }
                });

        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        cancel = false;
                    }
                });

        // Create the dialog box
        final AlertDialog dlg = builder.create();

        return dlg;
    }
}
