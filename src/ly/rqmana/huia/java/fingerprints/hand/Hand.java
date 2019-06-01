package ly.rqmana.huia.java.fingerprints.hand;

import com.sun.istack.internal.NotNull;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ly.rqmana.huia.java.fingerprints.activity.DuplicateFingersException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Hand {

    private final HandType type;

    private final Finger thumb;
    private final Finger index;
    private final Finger middle;
    private final Finger ring;
    private final Finger little;

    public Hand(@NotNull HandType type){
        this(type, new ArrayList<>());
    }

    /**
     *
     * @param type
     * @param fingers used only to update this {@code Hand} fingers and not replace them;
     */
    public Hand(@NotNull HandType type, List<Finger> fingers){
        this.type = type;

        this.thumb = new Finger(type == HandType.RIHGT? FingerID.R_THUMB : FingerID.L_THUMB, "", null);
        this.index = new Finger(type == HandType.RIHGT? FingerID.R_INDEX : FingerID.L_INDEX, "", null);
        this.middle = new Finger(type == HandType.RIHGT? FingerID.R_MIDDLE : FingerID.L_MIDDLE, "", null);
        this.ring = new Finger(type == HandType.RIHGT? FingerID.R_RING : FingerID.L_RING, "", null);
        this.little = new Finger(type == HandType.RIHGT? FingerID.R_LITTLE : FingerID.L_LITTLE, "", null);
        updateFingers(fingers);
    }

    private void updateFingers(List<Finger> fingers){

        for (Finger finger : fingers) {

            if (finger.isThumb())
                thumb.fill(finger);
            else if (finger.isIndex())
                index.fill(finger);
            else if (finger.isMiddle())
                middle.fill(finger);
            else if (finger.isRing())
                ring.fill(finger);
            else if (finger.isLittle())
                little.fill(finger);
        }
    }

    public ObservableList<Finger> getFingersUnmodifiable(){

        return FXCollections.unmodifiableObservableList(
                FXCollections.observableArrayList(
                        thumb,
                        index,
                        middle,
                        ring,
                        little));
    }

    public boolean isValidFinger(Finger finger){

        boolean con1 = finger.getId().isRightFinger() && getType() == HandType.RIHGT;
        boolean con2 = ! finger.getId().isRightFinger() && getType() == HandType.LEFT;

        return con1 || con2;
    }

    public HandType getType() {
        return type;
    }

    public Finger getThumb() {
        return this.thumb;
    }

    public Finger getIndex() {
        return this.index;
    }

    public Finger getMiddle() {
       return this.middle;
    }

    public Finger getRing() {
        return this.ring;
    }

    public Finger getLittle() {
        return this.little;
    }
}
