package com.littlefox.app.foxschool.object.result.main;

import android.os.Parcel;
import android.os.Parcelable;

public class MyBookshelfResult implements Parcelable
{
    private String id = "";
    private String name = "";
    private String color = "";
    private int contents_count = 0;

    public MyBookshelfResult(String id, String name, String color)
    {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    protected MyBookshelfResult(Parcel in) {
        id = in.readString();
        name = in.readString();
        color = in.readString();
        contents_count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(color);
        dest.writeInt(contents_count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyBookshelfResult> CREATOR = new Creator<MyBookshelfResult>() {
        @Override
        public net.littlefox.lf_app_fragment.object.result.main.MyBookshelfResult createFromParcel(Parcel in) {
            return new net.littlefox.lf_app_fragment.object.result.main.MyBookshelfResult(in);
        }

        @Override
        public net.littlefox.lf_app_fragment.object.result.main.MyBookshelfResult[] newArray(int size) {
            return new net.littlefox.lf_app_fragment.object.result.main.MyBookshelfResult[size];
        }
    };

    public String getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getContentsCount()
    {
        return contents_count;
    }

    public void setcontentsCount(int count)
    {
        contents_count = count;
    }

    public String getColor()
    {
        return color;
    }
}
