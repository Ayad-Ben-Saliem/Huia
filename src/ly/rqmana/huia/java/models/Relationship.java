package ly.rqmana.huia.java.models;

import com.sun.istack.internal.Nullable;
import ly.rqmana.huia.java.util.Utils;

public enum Relationship {

    SUBSCRIBER(Utils.getI18nString("SUBSCRIBER")),
    FATHER(Utils.getI18nString("FATHER")),
    MOTHER(Utils.getI18nString("MOTHER")),
    SON(Utils.getI18nString("SON")),
    DAUGHTER(Utils.getI18nString("DAUGHTER")),
    HUSBAND(Utils.getI18nString("HUSBAND")),
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

    public static Relationship parse(String relationshipString) {
        if (relationshipString == null || relationshipString.equalsIgnoreCase("Special") || relationshipString.equalsIgnoreCase("Not Exist")) {
            return null;
        } else {
            return valueOf(relationshipString);
        }
    }

    public static Relationship parseArabic(String relationshipString) {

         switch(relationshipString){
                case "المشترك":
                    return SUBSCRIBER;
                case "الزوجة":
                    return WIFE;
                case "الزوج":
                    return HUSBAND;
                case "الإبن":
                    return SON;
                case "الإبنة":
                    return DAUGHTER;
                case "والدة الموظف":
                    return MOTHER;
                case "والد الموظف":
                    return FATHER;
         }
         throw new IllegalArgumentException("Couldn't determine relationship from arabic text: "+ relationshipString);
    }
}
