import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class IntList {
    int[] data;
    int len = 0;

    IntList(int len) {
        data = new int[Math.max(2, len)];
    }

    IntList() {
        data = new int[16];
    }

    void add(int elem) {
        if (len == data.length) {
            int b[] = new int[data.length * 2];
            System.arraycopy(data, 0, b, 0, len);
            data = b;
        }
        data[len++] = elem;
    }

    // adds element at given position. if position is larger than position of
    // last element it adds it at the end of the list.
    void add(int elem, int pos) {
        if (pos >= len)
            add(elem);
        else
            data[pos] = elem;
    }

    void append(IntList other) {
        if (len + other.len > data.length) {
            int newLen = Math.max(2 * len, len + 2 * other.len);
            int[] b = new int[newLen];
            System.arraycopy(data, 0, b, 0, len);
            data = b;
        }

        System.arraycopy(other.data, 0, data, len, other.len);
        len += other.len;
    }

    void clear() {
        len = 0;
    }

    int get(int pos) {
        if (pos > len - 1)
            return -1;
        else
            return data[pos];
    }

    int getAndRemoveLast() {
        len--;
        return data[len];
    }

    // Not necessary when data and length aren't private...
    int size() {
        return len;
    }

    void print() {
        for (int i = 0; i < len; i++) {
            if (i % 15 == 0)
                System.out.println("");

            System.out.print(data[i] + "\t");
        }
        System.out.println("");
    }

    // This function sorts the list by closest to relativePoint with
    // 'distanceBetweenTwo'
    void sortByDistanceFrom(int relativePoint, int[] x, int[] y) {
        // Create a list of integer indices that correspond to the elements in the
        // IntList

        // Sort the indices based on the distances from the relative point
        Comparator<Integer> comp = new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                double distance1 = distanceBetweenTwo(i1, relativePoint, x, y);
                double distance2 = distanceBetweenTwo(i2, relativePoint, x, y);
                return Double.compare(distance1, distance2);
            }
        };
        quicksort(0, len - 1, comp);

    }

    private double distanceBetweenTwo(int p1, int p2, int[] x, int[] y) {
        int x1 = x[p1];
        int x2 = x[p2];
        int y1 = y[p1];
        int y2 = y[p2];

        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    private void quicksort(int low, int high, Comparator<Integer> comp) {
        if (low < high) {
            int pivotIndex = partition(low, high, comp);
            quicksort(low, pivotIndex - 1, comp);
            quicksort(pivotIndex + 1, high, comp);
        }
    }

    private int partition(int low, int high, Comparator<Integer> comp) {
        int pivot = data[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comp.compare(data[j], pivot) <= 0) {
                i++;
                swap(i, j);
            }
        }
        swap(i + 1, high);
        return i + 1;
    }

    private void swap(int i, int j) {
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }
}