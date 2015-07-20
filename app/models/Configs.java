package models;

public final class Configs {

    public static Config config(String key) {
        return new ConfigImpl(key);
    }

}

