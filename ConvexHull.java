import java.security.cert.LDAPCertStoreParameters;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConvexHull {
    public int n = 0; // The number of points in the dataset
    public int[] x = new int[0]; // The x-coordinates of the points
    public int[] y = new int[0]; // The y-coordinates of the points
    public int MAX_X = 0; // The index of the point with the maximum x-coordinate
    public int MAX_Y = 0; // The index of the point with the maximum y-coordinate
    ExecutorService executor;

    public ConvexHull(int n, int[] x, int[] y) {
        this.n = n;
        this.x = x;
        this.y = y;

        // Find max x and y
        for (int i = 0; i < n; i++) {
            if (x[i] > x[MAX_X]) {
                MAX_X = i;
            }
            if (y[i] > y[MAX_Y]) {
                MAX_Y = i;
            }
        }
    }

    /*
     * Finds the convex hull of the dataset using a sequential algorithm.
     * 
     * @return The convex hull of the dataset
     */
    public IntList findHullSeq() {

        IntList coHull = new IntList();

        // find the min x
        int minX = 0;
        int maxX = 0;
        for (int i = 1; i < n; i++) {
            if (x[i] < x[minX]) {
                minX = i;
            }
            if (x[i] > x[maxX]) {
                maxX = i;
            }
        }

        IntList allPoints = new IntList(n);
        for (int i = 0; i < n; i++) {
            allPoints.add(i);
        }

        IntList sameDistances = new IntList();

        int p3 = findLargestNegativeDistance(allPoints, sameDistances, minX, maxX);
        if (p3 == -1) {
            return coHull;
        }

        IntList bottom = seqRec(maxX, minX, p3, allPoints);

        p3 = findLargestNegativeDistance(allPoints, sameDistances, maxX, minX);
        if (p3 == -1) {
            return coHull;
        }
        IntList top = seqRec(minX, maxX, p3, allPoints);

        coHull.add(maxX);
        coHull.append(top);
        coHull.add(minX);
        coHull.append(bottom);

        return coHull;
    }

    public IntList findHullPar() {
        return findHullPar(Runtime.getRuntime().availableProcessors());
    }

    /*
     * Returns the convex hull of the dataset using a parallel algorithm.
     * 
     * @param numThreads The number of layers of recursion before switching to the
     * sequential algorithm.
     * 
     * @return The convex hull of the dataset
     */
    public IntList findHullPar(int numRecursionLayers) {

        int recursionDepth = (int) Math.floor(numRecursionLayers / 2.0);
        executor = Executors.newFixedThreadPool(numRecursionLayers);
        IntList coHull = new IntList();

        // find the min x
        int minX = 0;
        int maxX = 0;
        for (int i = 1; i < n; i++) {
            if (x[i] < x[minX]) {
                minX = i;
            }
            if (x[i] > x[maxX]) {
                maxX = i;
            }
        }

        IntList allPoints = new IntList(n);
        for (int i = 0; i < n; i++) {
            allPoints.add(i);
        }

        IntList sameDistances = new IntList();
        int p3 = findLargestNegativeDistance(allPoints, sameDistances, minX, maxX);
        if (p3 == -1) {
            return coHull;
        }
        Future<IntList> bottom = parRec(maxX, minX, p3, allPoints, recursionDepth);

        p3 = findLargestNegativeDistance(allPoints, sameDistances, maxX, minX);
        if (p3 == -1) {
            return coHull;
        }

        Future<IntList> top = parRec(minX, maxX, p3, allPoints, recursionDepth);

        try {
            coHull.add(maxX);
            coHull.append(top.get());
            coHull.add(minX);
            coHull.append(bottom.get());
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return coHull;
    }

    /*
     * Returns the convex hull of the dataset slice
     * 
     * @param p1 The index of the first point in the dataset slice
     * 
     * @param p2 The index of the last point in the dataset slice
     * 
     * @param p3 The index of the point with the largest negative distance from the
     * line between p1 and p2
     * 
     * @param m The indices of the points in the dataset slice
     * 
     * @return The convex hull of the dataset slice
     */
    private IntList seqRec(int p1, int p2, int p3, IntList m) {
        // Creates subgroups with points on the left and right side of the line
        IntList leftGroup = findGroup(m, p1, p3);
        IntList rightGroup = findGroup(m, p3, p2);
        // Convex hull in the correct order
        IntList coHull = new IntList();

        // Points to the left, found recursively
        IntList leftCoHull = new IntList();
        IntList rightCoHull = new IntList();

        // If this is the last recursive call, the points on the line between p3 and p1
        // are appended here
        IntList rightLine = new IntList();
        IntList leftLine = new IntList();
        // Lists below are always added, if a leaf node in recursion the respective
        // lists above are appended
        IntList leftOnLine = new IntList();
        IntList rightOnLine = new IntList();

        int p4 = findLargestNegativeDistance(leftGroup, leftLine, p3, p1);

        // if p4 == -1 there are no points outside
        if (p4 != -1) {
            leftCoHull = seqRec(p1, p3, p4, leftGroup);

            leftLine = new IntList();
        } else {
            // Add points on the line to the left
            leftLine.sortByDistanceFrom(p3, x, y);
            leftOnLine.append(leftLine);
        }

        p4 = findLargestNegativeDistance(rightGroup, rightLine, p2, p3);

        if (p4 != -1) {
            rightCoHull = seqRec(p3, p2, p4, rightGroup);

            rightLine = new IntList();
        } else {
            // Add points on the line to the right
            rightLine.sortByDistanceFrom(p2, x, y);
            rightOnLine.append(rightLine);
        }

        coHull.append(rightOnLine);
        coHull.append(rightCoHull);
        coHull.add(p3);
        coHull.append(leftCoHull);
        coHull.append(leftOnLine);

        return coHull;
    }

    /*
     * Returns the convex hull of the dataset slice using a parallel algorithm.
     * 
     * @param p1 The index of the first point in the dataset slice
     * 
     * @param p2 The index of the last point in the dataset slice
     * 
     * @param p3 The index of the point with the largest negative distance from the
     * line between p1 and p2
     * 
     * @param m The indices of the points in the dataset slice
     * 
     * @param depth The current depth of recursion
     * 
     * @return The convex hull of the dataset slice as a Future object
     */
    private Future<IntList> parRec(int p1, int p2, int p3, IntList m, int depth) {
        ParRecWorker callable = new ParRecWorker(p1, p2, p3, m, depth);
        Future<IntList> fCoHull = executor.submit(callable);
        return fCoHull;
    }

    /*
     * Returns the convex hull of the dataset slice.
     * 
     * @param p1 The index of the first point in the dataset slice
     * 
     * @param p2 The index of the last point in the dataset slice
     * 
     * @param p3 The index of the point with the largest negative distance from the
     * line between p1 and p2
     * 
     * @param m The indices of the points in the dataset slice
     * 
     * @param recursionDepth The current depth of recursion
     * 
     * @return The convex hull of the dataset slice as a Future object
     */
    class ParRecWorker implements Callable<IntList> {

        private int p1, p2, p3;
        private IntList m;
        private int recursionDepth;

        public ParRecWorker(int p1, int p2, int p3, IntList m, int recursionDepth) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.m = m;
            this.recursionDepth = recursionDepth - 2;
        }

        @Override
        public IntList call() {
            IntList leftGroup = findGroup(m, p1, p3);
            IntList rightGroup = findGroup(m, p3, p2);

            IntList rightLine = new IntList();
            IntList leftLine = new IntList();
            IntList rightOnLine = new IntList();
            IntList leftOnLine = new IntList();

            IntList coHull = new IntList();

            Future<IntList> leftCohull = CompletableFuture.completedFuture(new IntList());
            Future<IntList> rightCohull = CompletableFuture.completedFuture(new IntList());

            // Add right points
            int p4 = findLargestNegativeDistance(rightGroup, rightLine, p2, p3);
            if (p4 != -1) {

                if (recursionDepth == 0)
                    coHull.append(seqRec(p3, p2, p4, rightGroup));
                else
                    rightCohull = parRec(p3, p2, p4, rightGroup, recursionDepth);
            } else { // Add points on the line to the right

                // Sort the points with distance 0 according to their distance to p2
                rightLine.sortByDistanceFrom(p2, x, y);
                // Add the points to the convex hull
                if (recursionDepth == 0)
                    coHull.append(rightLine);
                else
                    rightOnLine.append(rightLine);
            }

            if (recursionDepth == 0)
                coHull.add(p3);

            // Add left points
            p4 = findLargestNegativeDistance(leftGroup, leftLine, p3, p1);
            if (p4 != -1) {
                if (recursionDepth == 0)
                    coHull.append(seqRec(p1, p3, p4, leftGroup));
                else
                    leftCohull = parRec(p1, p3, p4, leftGroup, recursionDepth);
            } else { // Add points on the line to the left

                // Sort the points with distance 0 according to their distance to p3
                leftLine.sortByDistanceFrom(p3, x, y);
                // Add the points to the convex hull
                if (recursionDepth == 0)
                    coHull.append(leftLine);
                else
                    leftOnLine.append(leftLine);
            }

            // If the recursive calls returns Future objects, wait for them to finish and
            // add the results to coHull
            if (recursionDepth != 0) {
                try {
                    coHull.append(rightOnLine);
                    coHull.append(rightCohull.get());
                    coHull.add(p3);
                    coHull.append(leftCohull.get());
                    coHull.append(leftOnLine);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error in parRecWorker");
                    e.printStackTrace();
                }
            }

            return coHull;
        }

    }

    /*
     * Finds the largest negative distance from the line between p1 and p2 from
     * points in a dataset slice. Adds points with distance 0 to the list
     * pointsOnLine.
     * 
     * @param points The indices of the points in the dataset slice
     * 
     * @param pointsOnLine List to store points with distance 0
     * 
     * @param p1 The index of the first point
     * 
     * @param p2 The index of the second point
     * 
     * @return The index of the point with the largest negative distance from the
     * line between p1 and p2
     */
    private int findLargestNegativeDistance(IntList points, IntList pointsOnLine, int p1, int p2) {
        double max = 0;
        int maxIndex = -1;
        pointsOnLine.clear();
        for (int i = 0; i < points.size(); i++) {
            int p3 = points.get(i);
            if (p3 == p1 || p3 == p2) {
                continue;
            }
            double distance = calculateDistance(p1, p2, p3);

            // System.out.println("Distance from (" + p1 + " - " + p2 + ") to line " + p3 +
            // " is " + distance);
            if (distance == 0) {
                pointsOnLine.add(p3);
            }

            if (distance < max) {
                max = distance;
                maxIndex = p3;
            }
        }

        if (maxIndex == -1) {
            return -1;
        }

        return maxIndex;
    }

    /*
     * Calculates the distance from a point to a line. Is not the actual distance,
     * but can be used to compare what is closer
     * 
     * @param p1 The index of the first point
     * 
     * @param p2 The index of the second point
     * 
     * @param p3 The index of the point to calculate the distance to the line
     * between p1 and p2
     * 
     * @return The calculated distance from p3 to the line between p1 and p2
     */
    private double calculateDistance(int p1, int p2, int p3) {
        double a = y[p1] - y[p2];
        double b = x[p2] - x[p1];
        double c = y[p2] * x[p1] - y[p1] * x[p2];

        return a * x[p3] + b * y[p3] + c;

    }

    /*
     * Finds the points in a dataset slice which are obove the line between p1 and
     * p2
     * 
     * @param points The indices of the points in the dataset slice
     * 
     * @param p1 The index of the first point
     * 
     * @param p2 The index of the second point
     */
    private IntList findGroup(IntList points, int p1, int p2) {
        IntList group = new IntList();
        for (int i = 0; i < points.size(); i++) {
            int p3 = points.get(i);
            double distance = calculateDistance(p1, p2, p3);
            if (p3 == p1 || p3 == p2) {
                continue;
            }
            if (distance >= 0) {
                group.add(p3);
            }
        }
        return group;
    }
}