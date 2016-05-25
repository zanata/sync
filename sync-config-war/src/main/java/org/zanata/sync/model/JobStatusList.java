package org.zanata.sync.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JobStatusList implements List<JobStatus> {

    private List<JobStatus> list;

    public JobStatusList() {
        list  = new ArrayList<>();
    }

    public JobStatusList(List<JobStatus> list) {
        this.list = ImmutableList.copyOf(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<JobStatus> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.<T>toArray(a);
    }

    @Override
    public boolean add(JobStatus jobStatus) {
        return list.add(jobStatus);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends JobStatus> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends JobStatus> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public JobStatus get(int index) {
        return list.get(index);
    }

    @Override
    public JobStatus set(int index, JobStatus element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, JobStatus element) {
        list.add(index, element);
    }

    @Override
    public JobStatus remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<JobStatus> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<JobStatus> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<JobStatus> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobStatusList)) return false;

        JobStatusList that = (JobStatusList) o;

        return !(list != null ? !list.equals(that.list) : that.list != null);

    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }
}
