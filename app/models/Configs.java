package models;

public class Configs {

    public static Config config(String key) {
        return new ConfigImpl(key);
    }

}

