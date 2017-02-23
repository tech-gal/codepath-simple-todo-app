package com.techgal.simpletodo;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TodoActivityFilePersistence extends AppCompatActivity {

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
        populateArrayItems();
        lvItems.setAdapter(itemsAdapter);
        etNewItem = (EditText) findViewById(R.id.etNewItem);

        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                items.remove(position);
                writeItems();
                itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent = new Intent(TodoActivityFilePersistence.this, EditItemActivity.class);
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
            } else {
                items.set(position, etUpdatedItem);
            }
            writeItems();
            itemsAdapter.notifyDataSetChanged();
        }
    }

    public void populateArrayItems() {
        readItems();
        itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);

    }

    private void readItems() {
        File filesDir = getFilesDir();
        File file = new File(filesDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(file));
        } catch (IOException ioe) {

        }

    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File file = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(file, items);
        } catch (IOException ioe) {

        }
    }

    public void onAddItem(View view) {
        if (!etNewItem.getText().toString().trim().isEmpty())
            itemsAdapter.add(etNewItem.getText().toString());
        writeItems();
        etNewItem.setText("");
    }


}
