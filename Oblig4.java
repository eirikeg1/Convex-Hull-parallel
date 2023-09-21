import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Oblig4 {

    // runs is 1 if test program is run
    private static int NUMBER_OF_RUNS = 15;

    // Hashmaps to store runtimes
    static HashMap<Integer, ArrayList<Double>> runtimesSeq = new HashMap<>();
    static HashMap<Integer, ArrayList<Double>> runtimesPar = new HashMap<>();
    static HashMap<Integer, ArrayList<Double>> speedups = new HashMap<>();

    // These values of n will be used if n is set to -1
    static int[] nValues = { 1000, 10000, 100000, 1000000, 10000000, 100000000 };
    private static int[] x;
    private static int[] y;
    private static NPunkter17 p;

    public static void main(String[] args) {

        int nValue;
        int SEED;
        boolean writeToFile = false;
        boolean drawGraph = false;
        boolean printResults = false;
        int flagNum = 0;

        // Parse arguments
        try {
            List arguments = Arrays.asList(args);
            if (arguments.contains("-w")) {
                writeToFile = true;
                flagNum++;
            }
            if (arguments.contains("-d")) {
                drawGraph = true;
                flagNum++;
            }
            if (arguments.contains("-p")) {
                printResults = true;
                flagNum++;
            }

            nValue = Integer.parseInt(args[0 + flagNum]);

            if (nValue != -1) {
                nValues = new int[] { nValue };
                NUMBER_OF_RUNS = 1; // runs is 1 by default
            }

            if (args.length > 1)
                SEED = Integer.parseInt(args[1 + flagNum]);
            else
                SEED = 0;

        } catch (Exception e) {
            System.out.println(
                    "Usage: 'java Oblig4 [-w] [-d] <n> [seed]', where <n> is the number of points to generate. Seed and flags are optional.\n * If -w is included, results will be written to file. \n * If -d is included, the graph will be drawn if n < 10 000.");
            return;
        }

        // Print n value(s)
        if (nValues.length > 1)
            System.out.println("Running for n values: " + Arrays.toString(nValues));

        // Run the program
        for (int n : nValues) {
            System.out.println("\nRunning with n = " + n + ":");
            runtimesSeq.put(n, new ArrayList<>());
            runtimesPar.put(n, new ArrayList<>());
            speedups.put(n, new ArrayList<>());

            System.out.println("  ...generating data...");
            x = new int[n];
            y = new int[n];
            p = new NPunkter17(n, SEED);
            p.fyllArrayer(x, y);
            System.out.println("  ...data done!\n");

            for (int i = 0; i < NUMBER_OF_RUNS; i++) {
                runConvexHullSeq(n, SEED, writeToFile, drawGraph, printResults);
            }

            for (int i = 0; i < NUMBER_OF_RUNS; i++) {
                runConvexHullPar(n, SEED, writeToFile, drawGraph, printResults);
            }

        }

        // Calculate and print mean speedups
        calculateSpeedups();
        printMeanSpeedups();
    }

    public static void runConvexHullSeq(int n, int seed, boolean writeToFile, boolean drawGraph, boolean printResults) {
        // Generate points
        // Sequential version
        double start = System.currentTimeMillis();
        ConvexHull ch = new ConvexHull(n, x, y);
        IntList cohull = ch.findHullSeq();
        double end = System.currentTimeMillis();

        System.out.println(" * time: " + (end - start) + " ms (sequential version)");

        Oblig4Precode precode = new Oblig4Precode(ch, cohull);
        if (n <= 10000 && drawGraph)
            precode.drawGraph();

        if (writeToFile)
            precode.writeHullPoints();

        runtimesSeq.get(n).add(end - start);

        if (printResults) {
            cohull.print();
        }
    }

    public static void runConvexHullPar(int n, int seed, boolean writeToFile, boolean drawGraph, boolean printResults) {
        // Parallel version
        double start = System.currentTimeMillis();
        ConvexHull ch = new ConvexHull(n, x, y);
        IntList cohull = ch.findHullPar();
        double end = System.currentTimeMillis();

        System.out.println(" * time: " + (end - start) + " ms (parallel version)");

        Oblig4Precode precode = new Oblig4Precode(ch, cohull);
        if (n <= 10000 && drawGraph)
            precode.drawGraph();

        if (writeToFile)
            precode.writeHullPoints();

        runtimesPar.get(n).add(end - start);

        if (printResults) {
            cohull.print();
        }
    }

    static void initHashMaps() {
        for (int n : nValues) {
            runtimesSeq.put(n, new ArrayList<>());
            runtimesPar.put(n, new ArrayList<>());
            speedups.put(n, new ArrayList<>());
        }
    }

    static void calculateSpeedups() {
        for (int n : nValues) {
            for (int i = 0; i < runtimesSeq.get(n).size(); i++) {
                double speedup = (double) runtimesSeq.get(n).get(i) / (double) runtimesPar.get(n).get(i);
                speedups.get(n).add(speedup);
            }
        }
    }

    static void printMeanSpeedups() {
        System.out.println("\n\nAll mean speedups:");
        for (int n : nValues) {
            Collections.sort(speedups.get(n));
            System.out.println(" * n: " + n + ", speedup: " + (speedups.get(n).get(speedups.get(n).size() / 2)));
        }
    }

}