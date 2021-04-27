package com.littlefox.app.foxschool.object.result.main;

import android.os.Parcel;
import android.os.Parcelable;

import net.littlefox.lf_app_fragment.enumitem.VocabularyType;

public class MyVocabularyResult implements Parcelable
{
    /**
     * 보케블러리 책장 ID
     */
    private String id = "";
    private String name = "";
    private String color = "";
    private int words_count = 0;

    /**
     * 해당 컨텐츠 ID
     */
    private String mContentsID = "";
    private VocabularyType mVocabularyType;

    public MyVocabularyResult(String id, String name, String color, VocabularyType vocabularyType)
    {
        this.mContentsID = id;
        this.name       = name;
        this.color      = color;
        mVocabularyType = vocabularyType;
    }

    public MyVocabularyResult(String id, String name, VocabularyType vocabularyType)
    {
        this.mContentsID = id;
        this.name       = name;
        mVocabularyType = vocabularyType;
    }


    protected MyVocabularyResult(Parcel in) {
        id = in.readString();
        mContentsID = in.readString();
        name = in.readString();
        color = in.readString();
        words_count = in.readInt();
        mVocabularyType = (VocabularyType) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mContentsID);
        dest.writeString(name);
        dest.writeString(color);
        dest.writeInt(words_count);
        dest.writeSerializable(mVocabularyType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyVocabularyResult> CREATOR = new Creator<MyVocabularyResult>() {
        @Override
        public net.littlefox.lf_app_fragment.object.result.main.MyVocabularyResult createFromParcel(Parcel in) {
            return new net.littlefox.lf_app_fragment.object.result.main.MyVocabularyResult(in);
        }

        @Override
        public net.littlefox.lf_app_fragment.object.result.main.MyVocabularyResult[] newArray(int size) {
            return new net.littlefox.lf_app_fragment.object.result.main.MyVocabularyResult[size];
        }
    };

    public String getID()
    {
        return id;
    }

    public String getContentID()
    {
        return mContentsID;
    }

    public String getName()
    {
        return name;
    }

    public int getWordCount()
    {
        return words_count;
    }

    public void setWordCount(int count)
    {
        words_count = count;
    }

    public String getColor()
    {
        return color;
    }

    public VocabularyType getmVocabularyType()
    {
        return mVocabularyType;
    }

    public void setVocabularyType(VocabularyType type)
    {
        mVocabularyType = type;
    }
}
