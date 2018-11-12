package pmikolajczyk.keyholder.keystore;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Scanner;

public class Key implements Serializable, Parcelable {
    public String name = "";
    public String value = "";
    public int valueLength = 0;
    public String username = "";
    public String loginPage = "";

    public Key() {}

    public Key(String name, String value, int valueLength, String username, String loginPage) {
        this.name = name;
        this.value = value;
        this.valueLength = valueLength;
        this.username = username;
        this.loginPage = loginPage;
    }

    private Key(Parcel in) {
        name = in.readString();
        value = in.readString();
        valueLength = in.readInt();
        username = in.readString();
        loginPage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeInt(valueLength);
        dest.writeString(username);
        dest.writeString(loginPage);
    }

    public static final Creator<Key> CREATOR = new Creator<Key>() {
        @Override
        public Key createFromParcel(Parcel in) {
            return new Key(in);
        }

        @Override
        public Key[] newArray(int size) {
            return new Key[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("q+++%sq+++%sq+++%dq+++%sq+++%sq+++", name, value, valueLength, username, loginPage);
    }

    public static Key fromString(String description){
        Scanner scanner = new Scanner(description);
        scanner.useDelimiter("q\\+\\+\\+");
        String name = scanner.next();
        String value = scanner.next();
        int valueLength = scanner.nextInt();
        String username = scanner.next();
        String loginPage = scanner.next();
        return new Key(name, value, valueLength, username, loginPage);
    }
}
