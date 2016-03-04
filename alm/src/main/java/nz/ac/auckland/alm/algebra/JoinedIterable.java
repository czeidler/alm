/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class JoinedIterable<T> implements Iterable<T> {
    private List<Collection<? extends T>> allLists = new ArrayList<Collection<? extends T>>();

    public void addList(Collection<? extends T> list) {
        allLists.add(list);
    }

    public JoinedIterable(Collection<? extends T>... lists) {
        for (Collection<? extends T> list : lists)
            addList(list);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<Collection<? extends T>> allListIterator = allLists.iterator();
            Iterator<? extends T> currentListIterator = null;

            {
                advanceToNextNoneEmptyList();
            }

            private void advanceToNextNoneEmptyList() {
                while (allListIterator.hasNext()) {
                    currentListIterator = allListIterator.next().iterator();
                    if (currentListIterator.hasNext())
                        break;
                }
            }

            @Override
            public boolean hasNext() {
                return currentListIterator != null && currentListIterator.hasNext();
            }

            @Override
            public T next() {
                T next = currentListIterator.next();
                if (!currentListIterator.hasNext())
                    advanceToNextNoneEmptyList();
                return next;
            }

            @Override
            public void remove() {

            }
        };
    }
}
