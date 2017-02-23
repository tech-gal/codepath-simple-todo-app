package com.techgal.simpletodo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class TodoActivitySQLLiteInline extends AppCompatActivity {

    private static final String DUPLICATE_ITEM_NOT_ALLOWED_MSG = "This task is already present in the list. Duplicates tasks are not supported at this time.";
    private final int REQUEST_CODE = 1;
    private EditText etNewItem;
    private ListView lvItems;
    private ArrayList items;
    private ArrayAdapter itemsAdapter;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList();
        initDB();
        populateArrayItems();
        lvItems.setAdapter(itemsAdapter);
        etNewItem = (EditText) findViewById(R.id.etNewItem);

        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteItem((String) items.get(position));
                items.remove(position);
                readItems();
                itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent = new Intent(TodoActivitySQLLiteInline.this, EditItemActivity.class);
                editIntent.putExtra("editText", items.get(position).toString());
                editIntent.putExtra("position", String.valueOf(position));
                startActivityForResult(editIntent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && REQUEST_CODE == 1) {
            // Extract name value from result extras
            int position = Integer.parseInt(data.getStringExtra("position"));
            String etUpdatedItem = data.getStringExtra("etUpdatedItem");
            if (etUpdatedItem.trim().isEmpty()) {
                items.remove(position);
            } else if (isDuplicateItem(String.valueOf(position)))
                popupAlert(this, DUPLICATE_ITEM_NOT_ALLOWED_MSG);
            else {
                items.set(position, etUpdatedItem);

                writeItems();
                itemsAdapter.notifyDataSetChanged();
            }
        }
    }

    public void populateArrayItems() {
        readItems();
        itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);

    }

    private void deleteItem(String item) {
        String sql = "DELETE FROM  todo WHERE item='" + item + "'";
        db.execSQL(sql);

    }

    private void readItems() {
        items.clear();
        Cursor c = db.rawQuery("SELECT * FROM todo", null);
        if (c.moveToFirst()) {
            do {
                items.add(c.getString(0));
            } while (c.moveToNext());
        }

    }

    private void writeItems() {
        for (Object item : items) {
            String sql = "INSERT INTO todo (item) VALUES ('" + item + "')";
            db.execSQL(sql);

        }
    }

    public boolean isDuplicateItem(String item) {
        Cursor c = db.rawQuery("SELECT * FROM todo where item = '" + item + "'", null);
        if (c.moveToFirst()) {
            return true;
        }
        return false;
    }

    public void onAddItem(View view) {
        if (!etNewItem.getText().toString().trim().isEmpty() && !isDuplicateItem(etNewItem.getText().toString())) {
            itemsAdapter.add(etNewItem.getText().toString());
            writeItems();
        } else {
            popupAlert(this, DUPLICATE_ITEM_NOT_ALLOWED_MSG);
        }
        etNewItem.setText("");
    }


    private void initDB() {
        db = openOrCreateDatabase("codepath_db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS todo(item VARCHAR);");
    }

    private void popupAlert(Context context, String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(msg);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

//        builder1.setNegativeButton(
//                "No",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
