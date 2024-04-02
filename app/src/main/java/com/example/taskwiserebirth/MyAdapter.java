package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private RealmResults<notes> notesList;

    public MyAdapter(Context context, RealmResults<notes> notesList) {
        this.context = context;
        this.notesList = notesList;

        // Configure Realm
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true) // Enable writes on UI thread
                .build();
        Realm.setDefaultConfiguration(config);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final notes note = notesList.get(position);
        holder.titleOutput.setText(note.getTitle());
        holder.descriptionOutput.setText(note.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(note.getCreatedTime());
        holder.timeOutput.setText(formattedTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item click if needed
            }
        });

        // Add click listener for the menu icon
        holder.mMenus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, adapterPosition);
                }
            }
        });
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.show_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.editText) {
                    int adapterPosition = position;
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        showEditDialog(notesList.get(adapterPosition));
                    }
                    return true;
                } else if (item.getItemId() == R.id.delete) {
                    int adapterPosition = position;
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        deleteNoteDialog(notesList.get(adapterPosition));
                    }
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void showEditDialog(final notes note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Note");

        // Inflate and set the layout for the dialog
        View editView = LayoutInflater.from(context).inflate(R.layout.edit_note_dialog, null);
        final EditText editTitle = editView.findViewById(R.id.userName);
        final EditText editDescription = editView.findViewById(R.id.userNo);
        editTitle.setText(note.getTitle());
        editDescription.setText(note.getDescription());
        builder.setView(editView);

        // Add action buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update note with new data
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        note.setTitle(editTitle.getText().toString());
                        note.setDescription(editDescription.getText().toString());
                        Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void deleteNoteDialog(final notes note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Note");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteFromRealm(note);
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    private void deleteNoteFromRealm(final notes note) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                note.deleteFromRealm();
                notifyDataSetChanged();
                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleOutput;
        TextView descriptionOutput;
        TextView timeOutput;
        ImageView mMenus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleOutput = itemView.findViewById(R.id.titleoutput);
            descriptionOutput = itemView.findViewById(R.id.descriptionoutput);
            timeOutput = itemView.findViewById(R.id.timeoutput);
            mMenus = itemView.findViewById(R.id.mMEnus);

            // Set click listener to mMenus ImageView
            mMenus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        showPopupMenu(v, adapterPosition);
                    }
                }
            });
        }
    }
}
