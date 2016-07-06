package edu.iit.bluetoothbeacon.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Masterpiece implements Parcelable {

    private String dvcName;
    private HashMap<String, Translation> translations;

    public Masterpiece(String dvcName, HashMap<String, Translation> translations){
        this.dvcName = dvcName;
        this.translations = translations;
    }

    protected Masterpiece(Parcel in) {
        dvcName = in.readString();
    }

    public static final Creator<Masterpiece> CREATOR = new Creator<Masterpiece>() {
        @Override
        public Masterpiece createFromParcel(Parcel in) {
            return new Masterpiece(in);
        }

        @Override
        public Masterpiece[] newArray(int size) {
            return new Masterpiece[size];
        }
    };

    public String getDvcName() {
        return dvcName;
    }

    public HashMap<String, Translation> getTranslations() {
        return translations;
    }

    public Translation getOneTranslation(String languageCode){
        return translations.get(languageCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dvcName);
    }
}
