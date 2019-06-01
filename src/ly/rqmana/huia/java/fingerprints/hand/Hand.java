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

    private final ObservableList<Finger> fingers = FXCollections.observableArrayList();

    public Hand(@NotNull HandType type){
        this(type, new ArrayList<>());
    }

    public Hand(@NotNull HandType type, List<Finger> fingers){
        this.type = type;
        setFingers(fingers);
    }

    public Finger getFinger(FingerID fingerID){
        for (Finger finger : this.fingers) {
            if (finger.getId() == fingerID)
                return finger;
        }
        return null;
    }

    public void addFinger(Finger finger){
        isValidFinger(finger);
        fingers.add(finger);
    }

    public void addFingers(Collection<Finger> fingers){
        for (Finger finger : fingers) {
            isValidFinger(finger);
        }

        this.fingers.addAll(fingers);
    }

    public void setFingers(Collection<Finger> fingers){
        for (Finger finger : fingers) {
            isValidFinger(finger);
        }

        this.fingers.addAll(fingers);
    }

    public boolean fingerExists(Finger finger){
        return fingerExists(finger.getId());
    }

    public boolean fingerExists(FingerID fingerID){
        for (Finger finger : fingers) {
            return finger.getId() == fingerID;
        }
        return false;
    }

    public ObservableList<Finger> getFingersUnmodifiable(){
        return FXCollections.unmodifiableObservableList(this.fingers);
    }

    private void isValidFinger(Finger finger){

        boolean valid;

        boolean con1 = finger.getId().isRightFinger() && getType() == HandType.RIHGT;
        boolean con2 = ! finger.getId().isRightFinger() && getType() == HandType.LEFT;

        valid = con1 || con2;

        if (! valid || fingerExists(finger)) {
            String info = String.format("%s [%d]", finger.getId().name(), finger.getId().index());
            throw new DuplicateFingersException("finger not valid duplicate may exists or unknown finger, FingerID: " + info);
        }
    }

    public HandType getType() {
        return type;
    }

    public Finger getThumbFinger() {
        if (getType().equals(HandType.RIHGT))
            return getFinger(FingerID.R_THUMB);
        else
            return getFinger(FingerID.L_THUMB);
    }

    public Finger getIndexFinger() {
        if (getType().equals(HandType.RIHGT))
            return getFinger(FingerID.R_INDEX);
        else
            return getFinger(FingerID.L_INDEX);
    }

    public Finger getMiddleFinger() {
        if (getType().equals(HandType.RIHGT))
            return getFinger(FingerID.R_MIDDLE);
        else
            return getFinger(FingerID.L_MIDDLE);
    }

    public Finger getRingFinger() {
        if (getType().equals(HandType.RIHGT))
            return getFinger(FingerID.R_RING);
        else
            return getFinger(FingerID.L_RING);
    }

    public Finger getLittleFinger() {
        if (getType().equals(HandType.RIHGT))
            return getFinger(FingerID.R_LITTLE);
        else
            return getFinger(FingerID.L_LITTLE);
    }
}
