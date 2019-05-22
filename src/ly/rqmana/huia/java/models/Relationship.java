package ly.rqmana.huia.java.models;

import ly.rqmana.huia.java.util.Utils;

public enum Relationship {

    EMPLOYEE(Utils.getI18nString("THE_EMPLOYEE")),
    FATHER(Utils.getI18nString("THE_FATHER")),
    MOTHER(Utils.getI18nString("THE_MOTHER")),
    SUN(Utils.getI18nString("SUN")),
    DAUGHTER(Utils.getI18nString("DAUGHTER")),
    HUSBAND(Utils.getI18nString("THE_HUSBAND")),
    WIFE(Utils.getI18nString("WIFE")),
    ;

    private final String name;

    Relationship(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
