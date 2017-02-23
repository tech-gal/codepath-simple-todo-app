package com.techgal.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class EditItemActivity extends AppCompatActivity {

    private EditText etUpdatedItem;
    private String position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String editText = getIntent().getStringExtra("editText");
        position = getIntent().getStringExtra("position");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        etUpdatedItem = (EditText) findViewById(R.id.etUpdatedItem);
        etUpdatedItem.setText(editText);
    }

    public void onSaveEdit(View view) {
        // Prepare data intent
        Intent data = new Intent();
        // Pass relevant data back as a result
        data.putExtra("etUpdatedItem", etUpdatedItem.getText().toString());
        data.putExtra("position", position);

        // Activity finished ok, return the data
        setResult(RESULT_OK, data); // set result code and bundle data for response
        finish(); // closes the activity, pass data to parent
    }
}
