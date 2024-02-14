# Parallelizing the convex hull algorithm

## Introduction
This is a way to modify the algorithm to find the convex hull of a set of points in a 2d graph. This is a recursive divide and conquer algorithm. This was a part of an assignment in the subject _IN3030 - Effective Parallel Programming_. I implemented both a sequential version and a parallel version and compared the speedups for different hull sizes.

More information and testing results, including speedups with parallelization, can be found in _report.pf_. To solve it I used the Future class, Callable interface and ExecutionerService from the java concurrency library.

## User guide

To run the program, you use the command: java Oblig4 *[flags] <n> [seed] 
* The flags are optional, to include them separate them with spaces between before <n>. A list of the flags: 
    - -w: writes the results t file using the precode. 
    - -d: draws a graphical presentation of the results. One for every run. (not recommended when running test program). Uses the precode. 
* n: the number of points. If n is set to -1 the test program is run, this is explained in the 
measurements secƟon. 
* seed: determines the seed used for generaƟng the points. 

## Simple desciption of the algorithm
*After running this algorithm you get a list containing all the points in the convex hull of the datapoints. Because the recursive steps happens before adding the points, the points wil be ordered starting from the one furthest to the right, in a counterclockwise order. This had to be taken into consideration when parallellizing*

### Main algorithm steps:
1. Find the point furthest to the left and the point furthest to the right call them **a** and **b**. 
2. Add **b** to the hull.
3. Imagine a line between **a** and **b**. Find the point above the line furthest away (calculated with another line from the point and 90 degrees from the line between **a** and **b**). This point is **c**
4. Add **b** to the hull, do the **Recursive steps** between **b** and **c**. 
5. Add **c** to the hull. Do the **Recursive steps** between **a** and **c**.
6. Add **a** to the hull.
7. **a** is now **b** and the previous **b** is now **a**.
8. Repeat step 3, 4 and 5

### Recursive steps:
*Input: _p1_, _p2_* 

1. Draw a line between _p1_ and _p2_
2. Find the point _p3_ outside the line (away from the center of the graph area), furthest away from the line.
3. Do recursive steps between _p3_ and _p2_ (In new recursive call _p3_ is now _p1_).
4. Add _p3_ to hull
5. Add points which are directly inbetween _p3_ and _p2_, in order of closest to _p2_. I made a modified version of quick sort.


## Quick summary of implementation
*More details in report*

### Precode
I implemented the whole algorithm and test programs by myself. However I used precode to generate the 2d points and print the results to file. The only thing I did not implement was the small class: 'NPunkter17'

### Implementation
#### Sequential
I followed the algorithm. In order to make it more efficient I made my own implementation of 'ArrayList' This is because 'ArrayList' takes longer to create as it is a much more complex class. I could also implement my own sort() function where I made a modified version of quick sort which sorts based on the euclidean distance between the point and a reference point (in the algorithm it was _p2_).

#### Parallell
In order to parallellize it I used an Executioner Service to manage a pool of threads which does jobs in a round robin fashion. I used the Callable class to return Future objects in order to make worker classes to do the recursive steps. In this way I ensured all the points were added in the correct order.

When referencing the _Initial steps_ parts in the explanation above I gave half the allocated threads to the recursive steps on step 4 and 5, and half to the ones on step 8. I made an int _recursionDepth_ which was halved for each recursion. When it reaches 0 the rest of the algorithm was run sequentially. The reason I only made this many recursive calls is because it is a little bit computationally expensive to create new threads and switch between them in a round robin fashion. In this implementation there is a litte more threads than the number of cores, which usually is optimal for parallellization in divide and conquer algorithms.
