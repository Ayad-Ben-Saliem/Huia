package ly.rqmana.huia.java.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class ObservableArrayListSet<E> implements ObservableList<E> {

    private final ObservableList<E> observableList = FXCollections.observableArrayList();

    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        observableList.addListener(listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        observableList.addListener(listener);
    }

    @SafeVarargs
    @Override
    public final boolean addAll(E... elements) {
        boolean result = true;
        for (E element : elements) {
            if (!contains(element))
                result = observableList.add(element);
            else
                result = false;
        }
        return result;
    }

    @SafeVarargs
    @Override
    public final boolean setAll(E... elements) {
        observableList.clear();
        return addAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        observableList.clear();
        return addAll(col);
    }

    @SafeVarargs
    @Override
    public final boolean removeAll(E... elements) {
        return observableList.removeAll(elements);
    }

    @SafeVarargs
    @Override
    public final boolean retainAll(E... elements) {
        return observableList.retainAll(elements);
    }

    @Override
    public void remove(int from, int to) {
        observableList.remove(from, to);
    }

    @Override
    public int size() {
        return observableList.size();
    }

    @Override
    public boolean isEmpty() {
        return observableList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return observableList.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return observableList.iterator();
    }

    @Override
    public Object[] toArray() {
        return observableList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return observableList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if ((contains(e))) return false;
        return observableList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return observableList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return observableList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean result = true;
        for (E element : collection) {
            if (!contains(element))
                result = observableList.add(element);
            else
                result = false;
        }
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        boolean result = true;
        for (E element : collection) {
            if (!contains(element))
                observableList.add(index++, element);
            else
                result = false;
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return observableList.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return observableList.retainAll(collection);
    }

    @Override
    public void clear() {
        observableList.clear();
    }

    @Override
    public E get(int index) {
        return observableList.get(index);
    }

    @Override
    public E set(int index, E element) {
        if (contains(element) && !element.equals(get(index))) {
            return null;
        }
        return observableList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        if (!contains(element))
            observableList.add(index, element);
    }

    @Override
    public E remove(int index) {
        return observableList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return observableList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new NotImplementedException();
    }

    @Override
    public ListIterator<E> listIterator() {
        return observableList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return observableList.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return observableList.subList(fromIndex, toIndex);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observableList.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observableList.addListener(listener);
    }
}
