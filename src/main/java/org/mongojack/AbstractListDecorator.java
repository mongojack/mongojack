package org.mongojack;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class AbstractListDecorator<E> implements List<E> {

    protected abstract List<E> delegate();

    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return delegate().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate().iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return delegate().toArray(a);
    }

    @Override
    public boolean add(final E e) {
        return delegate().add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return delegate().remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return delegate().containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return delegate().addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return delegate().addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return delegate().removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return delegate().retainAll(c);
    }

    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        delegate().replaceAll(operator);
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        delegate().sort(c);
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public boolean equals(final Object o) {
        return delegate().equals(o);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public E get(final int index) {
        return delegate().get(index);
    }

    @Override
    public E set(final int index, final E element) {
        return delegate().set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
        delegate().add(index, element);
    }

    @Override
    public E remove(final int index) {
        return delegate().remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return delegate().indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return delegate().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return delegate().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return delegate().listIterator(index);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return delegate().spliterator();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return delegate().removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        return delegate().stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return delegate().parallelStream();
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        delegate().forEach(action);
    }
}
