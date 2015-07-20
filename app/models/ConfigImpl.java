package models;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

public class ConfigImpl implements Config, Serializable {

    private final String value;

    public ConfigImpl(String key) {
        this.value = Objects.requireNonNull(key, "value");
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Config.class;
    }

    @Override
    public int hashCode() {
        return ((127 * "value".hashCode()) ^ value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Config)) {
            return false;
        }
        Config other = (Config) o;
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + Config.class.getName() + "(value=" + value + ")";
    }
}