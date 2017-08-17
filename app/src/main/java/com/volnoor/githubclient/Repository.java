package com.volnoor.githubclient;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eugene on 17.08.2017.
 */

public class Repository implements Parcelable {
    private String name;
    private String description;

    public Repository(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected Repository(Parcel in) {
        name = in.readString();
        description = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
    }

    public static final Creator<Repository> CREATOR = new Creator<Repository>() {
        @Override
        public Repository createFromParcel(Parcel in) {
            return new Repository(in);
        }

        @Override
        public Repository[] newArray(int size) {
            return new Repository[size];
        }
    };
}
