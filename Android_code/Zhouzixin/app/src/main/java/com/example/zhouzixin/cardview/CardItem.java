package com.example.zhouzixin.cardview;


public class CardItem {

    private int mTextResource;
    private int mTitleResource;
    private int mButtonResource;

    public CardItem(int title, int text,int button) {
        mTitleResource = title;
        mTextResource = text;
        mButtonResource=button;
    }

    public int getText() {
        return mTextResource;
    }

    public int getTitle() {
        return mTitleResource;
    }

    public int getButton(){
        return mButtonResource;
    }
}
