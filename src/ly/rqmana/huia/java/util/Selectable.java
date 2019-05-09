package ly.rqmana.huia.java.util;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Collection;

public interface Selectable<T> {

    ObservableList<OnSelectListener> onSelectListeners = FXCollections.observableArrayList();

    default void setOnSelectListener(OnSelectListener<T> onSelectListener) {
        onSelectListeners.clear();
        onSelectListeners.add(onSelectListener);
    }

    default void addOnSelectListener(OnSelectListener<T> onSelectListener) {
        onSelectListeners.add(onSelectListener);
    }

    default void addOnSelectListeners(Collection<OnSelectListener<T>> onSelectListeners) {
        this.onSelectListeners.addAll(onSelectListeners);
    }

    default void addOnSelectListeners(OnSelectListener<T>... onSelectListeners) {
        addOnSelectListeners(Arrays.asList(onSelectListeners));
    }

}
