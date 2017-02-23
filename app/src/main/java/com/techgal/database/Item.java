package com.techgal.database;


public class Item  {

    public String id;
    public String text;
    public long due_date;
    public long completion_date;
    public int soft_delete;

    // Make sure to define this constructor (with no arguments)
    // If you don't querying will fail to return results!
    public Item() {
        super();
    }

    // Be sure to call super() on additional constructors as well
    public Item(String id, String item, long due_date,  long completion_date, int soft_delete){
        super();
        this.id = id;
        this.text = text;
        this.due_date = due_date;
        this.completion_date = completion_date;
        this.soft_delete = soft_delete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem() {
        return text;
    }

    public void setItem(String item) {
        this.text = text;
    }

    public long getDue_date() {
        return due_date;
    }

    public void setDue_date(long due_date) {
        this.due_date = due_date;
    }

    public long getCompletion_date() {
        return completion_date;
    }

    public void setCompletion_date(long completion_date) {
        this.completion_date = completion_date;
    }

    public int isSoft_delete() {
        return soft_delete;
    }

    public void setSoft_delete(int soft_delete) {
        this.soft_delete = soft_delete;
    }
}
