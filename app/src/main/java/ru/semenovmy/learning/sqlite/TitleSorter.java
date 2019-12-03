package ru.semenovmy.learning.sqlite;

class TitleSorter {

    private final int mIndex;
    private final String mTitle;

    TitleSorter(int mIndex, String mTitle) {
        this.mIndex = mIndex;
        this.mTitle = mTitle;
    }

    int getIndex() {
        return mIndex;
    }

    String getTitle() {
        return mTitle;
    }
}
