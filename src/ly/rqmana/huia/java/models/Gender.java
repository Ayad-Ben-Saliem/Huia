package ly.rqmana.huia.java.models;

import ly.rqmana.huia.java.util.Utils;

public enum  Gender {

    MALE("MALE"),
    FEMALE("FEMALE"),
    ;
    final String stringKey;

    Gender(String stringKey) {
        this.stringKey = stringKey;
    }

    @Override
    public String toString() {
        return Utils.getI18nString(stringKey);
    }

    public String getKey() {
        return stringKey;
    }
}
