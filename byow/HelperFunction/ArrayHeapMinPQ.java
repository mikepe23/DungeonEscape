package byow.HelperFunction;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {

    private Pair[] minHeap;
    private HashMap<T, Pair> items; // References to pairs, to changePriority
    private int size;
    private final double UPSIZE_FACTOR = 0.73;
    private final double DOWNSIZE_FACTOR = 0.27;

    private class Pair<T> {
        private T item;
        private double priority;
        private int index;
        Pair(T i, double p, int k) {
            item = i;
            priority = p;
            index = k;
        }
        public String toString() {
            return (String) item;
        }
    }

    public ArrayHeapMinPQ() {
        minHeap = new Pair[16];
        Pair[] a = new Pair[100];
        items = new HashMap<>(16);
        size = 0;
    }

    private void resize(boolean enhance) {
        Pair[] h;
        HashMap<T, Pair> i;
        if (enhance) {
            h = new Pair[minHeap.length * 2];
            i = new HashMap<>(minHeap.length * 2);
        } else if (minHeap.length <= 16) { // Don't reduce size below 16.
            return;
        } else {
            h = new Pair[minHeap.length / 2];
            i = new HashMap<>(minHeap.length / 2);
        }
        System.arraycopy(minHeap, 0, h, 0, size + 1);
        i.putAll(items);
        minHeap = h;
        items = i;
    }

    public boolean contains(T item) {
        return items.containsKey(item);
    }

    public void add(T item, double priority) {
        if (contains(item)) {
            throw new IllegalArgumentException("Cannot add an already existing item" + item);
        }
        minHeap[size + 1] = new Pair(item, priority, size + 1);
        items.put(item, minHeap[size + 1]);
        swim(size + 1);
        size += 1;
        if ((double) size / (double) minHeap.length >= UPSIZE_FACTOR) {
            resize(true);
        }
    }

    public T getSmallest() {
        if (size == 0) {
            throw new NoSuchElementException("No elements in MinHeap");
        }
        return (T) minHeap[1].item;
    }

    public T removeSmallest() {
        if (size == 0) {
            throw new NoSuchElementException("No elements in MinHeap");
        }
        Pair ret = minHeap[1];
        minHeap[1] = minHeap[size];
        minHeap[1].index = 1;
        minHeap[size] = null;
        size -= 1;
        sink(1);
        items.remove(ret.item);
        if ((double) size / (double) minHeap.length <= DOWNSIZE_FACTOR) {
            resize(false);
        }
        return (T) ret.item;
    }

    public int size() {
        return size;
    }

    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new NoSuchElementException("MinHeap does not contain " + item);
        }
        Pair p = items.get(item);
        if (p != null) {
            double temp = p.priority;
            p.priority = priority;
            if (temp > priority) {
                swim(p.index);
            } else {
                sink(p.index);
            }
        }
    }

    /**                  HELPER FUNCTIONS                  **/
    private void swim(int k) {
        if (k > 1 && minHeap[parent(k)] != null && minHeap[k] != null
                && minHeap[parent(k)].priority > minHeap[k].priority) {
            Pair temp = minHeap[parent(k)];
            minHeap[parent(k)] = minHeap[k];
            minHeap[parent(k)].index = parent(k);
            minHeap[k] = temp;
            temp.index = k;
            swim(parent(k));
        }

    }

    private void sink(int k) {
        int smallest = k;
        int left = leftChild(k);
        int right = rightChild(k);
        if (left > 0 && minHeap[left].priority < minHeap[smallest].priority) {
            smallest = left;
        }
        if (right > 0 && minHeap[right].priority < minHeap[smallest].priority) {
            smallest = right;
        }
        if (smallest != k) {
            Pair temp = minHeap[smallest];
            minHeap[smallest] = minHeap[k];
            minHeap[smallest].index = smallest;
            minHeap[k] = temp;
            temp.index = k;
            sink(smallest);
        }
    }

    private int parent(int k) {
        if (k > 3) {
            return k / 2;
        }
        return 1;
    }

    private int leftChild(int k) {
        if ((k * 2) >= minHeap.length || minHeap[(k * 2)] == null) {
            return -1;
        }
        return k * 2;
    }

    private int rightChild(int k) {
        if ((k * 2) + 1 >= minHeap.length || minHeap[(k * 2) + 1] == null) {
            return -1;
        }
        return (k * 2) + 1;
    }

}
