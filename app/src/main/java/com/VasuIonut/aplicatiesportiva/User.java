package com.VasuIonut.aplicatiesportiva;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String id;
    private String username;
    private String email;
    private String profileImage;
    private int age;
    private String description;
    private String gender;

    public User(String id, String username, String email, String profileImage, int age, String description,String gender) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profileImage = profileImage;
        this.age = age;
        this.description = description;
        this.gender=gender;
    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        email = in.readString();
        profileImage = in.readString();
        age = in.readInt();
        description = in.readString();
        gender=in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public int getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }
    public String getGender() {
        return gender;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(username);
        parcel.writeString(email);
        parcel.writeString(profileImage);
        parcel.writeInt(age);
        parcel.writeString(description);
        parcel.writeString(gender);
    }
}
