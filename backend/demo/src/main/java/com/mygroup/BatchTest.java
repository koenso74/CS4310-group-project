package com.mygroup;

import java.util.Arrays;
import java.util.List;

public class BatchTest {
    private final int TEST_SIZE = 100;

    public void RunTest() {
        List<Integer> successCounts = Arrays.asList(new Integer[4]);
        List<Float> avgMovedProcessNums = Arrays.asList(new Float[4]);

        MemoryProcessList l = new MemoryProcessList(2000);
        for (int i = 0; i < TEST_SIZE; i++) {
            l.Randomize();
            var temp = l.makeCopyOfProcessList();



        }
    }
}