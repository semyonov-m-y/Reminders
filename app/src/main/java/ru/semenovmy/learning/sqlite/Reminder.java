package ru.semenovmy.learning.sqlite;

public class Reminder {

    private int mID;
    private String mTitle;
    private String mActive;

    public Reminder(int ID, String Title, String Active){
        mID = ID;
        mTitle = Title;
        mActive = Active;
    }

    public Reminder(String Title, String Active){
        mTitle = Title;
        mActive = Active;
    }

    public Reminder(){}

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getActive() {
        return mActive;
    }

    public void setActive(String mActive) {
        this.mActive = mActive;
    }
}
