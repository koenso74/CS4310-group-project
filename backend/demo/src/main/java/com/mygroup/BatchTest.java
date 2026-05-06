package com.mygroup;

import java.util.Random;

public class BatchTest {
    private final int TEST_SIZE = 100;

    public static void main(String[] args) {
        var test = new BatchTest();
        test.RunTest();
    }

    public static class BatchTestResults {
        public int[] successCounts;
        public float[] avgMovedProcessNums;

        public BatchTestResults(int[] successCounts, float[] avgMovedProcessNums) {
            this.successCounts = successCounts;
            this.avgMovedProcessNums = avgMovedProcessNums;
        }
    }

    public BatchTestResults RunTest() {
        int[] successCounts = new int[4];
        float[] avgMovedProcessNums = new float[4];
        Random rand = new Random();

        MemoryProcessList l = new MemoryProcessList(2000);
        for (int i = 0; i < TEST_SIZE; i++) {
            l.Randomize();

            var temp = l.makeCopyOfProcessList();

            int memorySize = rand.nextInt(500, 1000);

            var r1 = l.swap(100 + i, memorySize);
            if (r1.success) {
                successCounts[0]++;
                avgMovedProcessNums[0] += r1.movedProcessNum;
            }
            l.setProcessChain(temp.getProcessChain());

            var r2 = l.compactToEnd(memorySize);
            if (r2.success) {
                successCounts[1]++;
                avgMovedProcessNums[1] += r2.movedProcessNum;
            }
            l.setProcessChain(temp.getProcessChain());

            var r3 = l.compactUntilLargeHole(memorySize);
            if (r3.success) {
                successCounts[2]++;
                avgMovedProcessNums[2] += r3.movedProcessNum;
            }
            l.setProcessChain(temp.getProcessChain());

            // var r4 = l.compactHeuristically(memorySize);
            // if (r4.success) {
            //     successCounts[3]++;
            //     avgMovedProcessNums[3] += r4.movedProcessNum;
            // }
        }

        for (int i = 0; i < 3; i++) {
            avgMovedProcessNums[i] /= successCounts[i];
            System.out.println(successCounts[i] + ", " + avgMovedProcessNums[i]);
        }

        return new BatchTestResults(successCounts, avgMovedProcessNums);
    }
}