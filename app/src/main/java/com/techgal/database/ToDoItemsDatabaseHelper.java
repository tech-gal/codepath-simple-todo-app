package com.techgal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class ToDoItemsDatabaseHelper extends SQLiteOpenHelper {

    private static ToDoItemsDatabaseHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "itemsDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_ITEMS = "items";

    // Item Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM = "item";
    private static final String KEY_ITEM_DUE_DATE = "due_date";
    private static final String KEY_ITEM_COMPLETION_DATE = "completion_date";
    private static final String KEY_SOFT_DELETE = "soft_delete";


    public static synchronized ToDoItemsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you 
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ToDoItemsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public ToDoItemsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " VARCHAR PRIMARY KEY," + // Define a primary key
                KEY_ITEM + " VARCHAR," +
                KEY_ITEM_DUE_DATE + " INTEGER," +
                KEY_ITEM_COMPLETION_DATE + " INTEGER," +
                KEY_SOFT_DELETE + " INTEGER" +
                ")";


        db.execSQL(CREATE_POSTS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    // Insert a item into the database
    public void addItem(Item item) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The item might already exist in the database (i.e. the same item created multiple items).
            long itemId = addOrUpdateItem(item);

            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_ID, item.id == null ? UUID.randomUUID().toString() : item.id);
            values.put(KEY_ITEM, item.text);
            values.put(KEY_ITEM_DUE_DATE, item.due_date == 0L ? System.currentTimeMillis() + (24 * 60 * 60 * 1000) : item.due_date);
            values.put(KEY_ITEM_COMPLETION_DATE, item.completion_date);
            values.put(KEY_SOFT_DELETE, item.soft_delete > 0 ? 1 : 0);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add item to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a item in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // item already exists) optionally followed by an INSERT (in case the item does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the item's primary key if we did an update.
    public long addOrUpdateItem(Item item) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_ID, item.id == null ? UUID.randomUUID().toString() : item.id);
            values.put(KEY_ITEM, item.text);
            values.put(KEY_ITEM_DUE_DATE, item.due_date == 0L ? System.currentTimeMillis() + (24 * 60 * 60 * 1000) : item.due_date);
            values.put(KEY_ITEM_COMPLETION_DATE, item.completion_date);
            values.put(KEY_SOFT_DELETE, item.soft_delete > 0 ? 1 : 0);

            // First try to update the item in case the item already exists in the database
            // This assumes items are unique
            int rows = db.update(TABLE_ITEMS, values, KEY_ITEM + "= ?", new String[]{item.text});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the item we just updated
                String itemsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_ITEM_ID, TABLE_ITEMS, KEY_ITEM);
                Cursor cursor = db.rawQuery(itemsSelectQuery, new String[]{String.valueOf(item.id)});
                try {
                    if (cursor.moveToFirst()) {
                        itemId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // item with this itemName did not already exist, so insert new item
                itemId = db.insertOrThrow(TABLE_ITEMS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update item");
        } finally {
            db.endTransaction();
        }
        return itemId;
    }

    public void deleteItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_ITEMS, "where id=" + item.id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete item with value " + item.text);
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAllItems(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_ITEMS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all items");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Item> getAllItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ArrayList<Item> allItems = new ArrayList<>();
        try {
            // Get the primary key of the item we just updated
            String itemsSelectQuery = String.format("SELECT * FROM %s WHERE %s != true",
                    TABLE_ITEMS, KEY_SOFT_DELETE);
            Cursor cursor = db.rawQuery(itemsSelectQuery, null);
            try {
                if (cursor.moveToFirst()) {
                    allItems.add(new Item(cursor.getString(cursor.getColumnIndex(KEY_ITEM_ID)), cursor.getString(cursor.getColumnIndex(KEY_ITEM)), cursor.getInt(cursor.getColumnIndex(KEY_ITEM_DUE_DATE)),
                            cursor.getInt(cursor.getColumnIndex(KEY_ITEM_COMPLETION_DATE)), cursor.getInt(cursor.getColumnIndex(KEY_SOFT_DELETE))));
                    db.setTransactionSuccessful();
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all items");
        } finally {
            db.endTransaction();
        }
        return allItems;

    }
}
    

