package edu.iit.bluetoothbeacon.models;

import java.util.HashMap;

public class Masterpiece {

    private String dvcName;
    private HashMap<String, Translation> translations;

    public Masterpiece(String dvcName, HashMap<String, Translation> translations){
        this.dvcName = dvcName;
        this.translations = translations;
    }

    public String getDvcName() {
        return dvcName;
    }

    public HashMap<String, Translation> getTranslations() {
        return translations;
    }

    public Translation getOneTranslation(String languageCode){
        return translations.get(languageCode);
    }
}
