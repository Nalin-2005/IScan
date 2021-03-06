package com.nalinstudios.iscan;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An activity that will work as a file browser to show all the PDFs scanned.
 * Adapted from http://www.christophbrill.de/en/posts/how-to-create-a-android-file-browser-in-15-minutes/
 * @author Nalin Angrish.
 */
public class ListFileActivity extends ListActivity {

    /** The path to show the files of*/
    private String path;


    /**
     * A function to show the directory contents
     * @param savedInstanceState the saved instance (not used).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        // Use the current directory as title
        path = "/";
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }

        // Read all files sorted into the values-array
        List<String> values = new ArrayList<>();
        File dir = new File(path);
        if (!dir.canRead()) {
            Toast.makeText(this, "Cannot read file contents. Please try again.",Toast.LENGTH_LONG).show();
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);

        getListView().setOnItemLongClickListener(new LongPress());
    }


    /**
     * A function to open a pdf / folder represented by the clicked item
     * @param l the listView
     * @param v the view clicked
     * @param position the position of the clicked item
     * @param id the id of the item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(this, getPackageName()+ ".provider", new File(filename)), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open Using"));
        }
    }


    /**
     * A class to listen for long press events.
     * This would be used to provide the user the options to share or view the pdf
     */
    private class LongPress implements AdapterView.OnItemLongClickListener {

        /**
         * The actual function that shows the menu
         * @param parent the adapterView of the List
         * @param view the view that was actually clicked
         * @param position the index of the item clicked
         * @param id the row id of the item that was clicked
         * @return true
         */
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final String filename = (String) getListAdapter().getItem(position);
            new AlertDialog.Builder(ListFileActivity.this)
                    .setIcon(android.R.drawable.btn_star)
                    .setMessage("What Operation do you want to do with "+ new File(filename).getName())
                    .setNeutralButton("Nothing", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("View", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(FileProvider.getUriForFile(ListFileActivity.this, getPackageName()+ ".provider", new File(path,filename)), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Open Using"));
                        }
                    })
                    .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ListFileActivity.this, getPackageName()+ ".provider", new File(path,filename)));
                            intent.setType("application/pdf");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Share Using"));
                        }
                    })
                    .show();
            return true;
        }
    }
}