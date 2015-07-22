package models;

public final class Configs {

    public static Config configured(String key) {
        return new ConfigImpl(key);
    }

}

