package com.mygroup;

import java.util.LinkedList;
import java.util.Random;

public class MemoryProcessList {
    private LinkedList<MyProcess> processChain;
    private int spaceRemaining;
    private final int MAX_SPACE;
    private LinkedList<MyProcess> disk;
    private final int DEFAULT_HOLE_ID = 0;
    private final int MIN_RAND_PROCESS_SIZE = 150;
    private final int MAX_RAND_PROCESS_SIZE = 500;

    public MemoryProcessList(int max) {
        MAX_SPACE = max;
        spaceRemaining = max;
        processChain = new LinkedList<>();
        processChain.add(new MyProcess(DEFAULT_HOLE_ID, max, true));
        disk = new LinkedList<>();
    }

    public MyProcess removeProcess(int id){
        if(id == 0){
            return null;
        }
        boolean hasFound = false;
        int i = 0;
        MyProcess p = null;
        while(!hasFound && i < processChain.size()){
            if(processChain.get(i).PID == id){
                p = processChain.get(i);
                processChain.get(i).PID = 0;
                processChain.get(i).isAvailble = true;
                combine();
                while(!disk.isEmpty()){
                    MyProcess p1 = disk.remove();
                    firstFitAdd(p1.PID, p1.memoryUse);
                }
                hasFound = true;
            }
            i++;
        }
        return p;
    }

    private void combine() {
        for (int i = 0; i < processChain.size() - 1; i++) {
            if (processChain.get(i).isAvailble && processChain.get(i + 1).isAvailble) {
                MyProcess old = processChain.remove(i);
                processChain.get(i).memoryUse += old.memoryUse;
            }
        }
    }

    public boolean addProcessAt(int id, int memory, int start) {
        if (memory <= spaceRemaining) {
            // find the index of starting
            boolean hasFound = false;
            int curInd = 0;
            int begin = 0;
            int end = 0;
            while (curInd < processChain.size() && !hasFound) {
                end = begin + processChain.get(curInd).memoryUse;
                if (start >= begin && start < end) {
                    hasFound = true;
                } else {
                    begin = end;
                    curInd++;
                }
            }

            // Unsuccessful Search
            if (!hasFound) {
                return false;
            }

            // Not a Hole Case
            if (!processChain.get(curInd).isAvailble) {
                return false;
            }

            // Check if the allocation fits
            if (start + memory > end) {
                return false;
            }

            int leftNode = start - begin;
            int rightNode = end - (start + memory);

            // Check if it is a perfect fit
            if (leftNode == 0 && rightNode == 0) {
                processChain.get(curInd).PID = id;
                processChain.get(curInd).isAvailble = false;
                spaceRemaining -= memory;
                combine();
                return true;
            }

            processChain.remove(curInd); // Remove the node

            // Add the new hole
            if (leftNode > 0) {
                processChain.add(curInd, new MyProcess(DEFAULT_HOLE_ID, leftNode, true));
                curInd++;
            }

            // Add the new process
            processChain.add(curInd, new MyProcess(id, memory, false));
            curInd++;

            // Add the new hole
            if (rightNode > 0) {
                processChain.add(curInd, new MyProcess(DEFAULT_HOLE_ID, rightNode, true));
            }

            return true;

        } else {
            return false;
        }
    }

    public boolean firstFitAdd(int id, int memory) {
        MyProcess newP = new MyProcess(id, memory, false);
        boolean hasFound = false;
        int i = 0;

        while (i < processChain.size() && !hasFound) {
            MyProcess cur = processChain.get(i);

            if (cur.isAvailble && cur.memoryUse >= memory) {
                if (cur.memoryUse == memory) {
                    processChain.set(i, newP);
                } else {
                    cur.memoryUse -= memory;
                    processChain.add(i, newP);
                }

                spaceRemaining -= memory;
                hasFound = true;
            }
            i++;
        }

        combine();
        return hasFound;
    }

    public int getSpaceRemaining() {
        return spaceRemaining;
    }

    public int getMAX_SPACE() {
        return MAX_SPACE;
    }

    public MyProcess getProcessAt(int index) {
        return processChain.get(index);
    }

    public int getProcessID(int index) {
        return processChain.get(index).PID;
    }

    public int getProcessMemoryUse(int index) {
        return processChain.get(index).memoryUse;
    }

    public boolean getProcessAvailblity(int index) {
        return processChain.get(index).isAvailble;
    }

    public LinkedList<MyProcess> getProcessChain() {
        return processChain;
    }

    public void setProcessChain(LinkedList<MyProcess> newChain) {
        this.processChain = newChain;
    }

    public void setSpaceRemaining(int space) {
        this.spaceRemaining = space;
    }

    public String getDiskStr(){
        String list = "[ ";
        for (int i = 0; i < disk.size() - 1; i++) {
            if (disk.get(i).isAvailble) {
                list += "(" + disk.get(i).PID + ":" + disk.get(i).memoryUse + ":Hole) , ";
            } else {
                list += "(" + disk.get(i).PID + ":" + disk.get(i).memoryUse + ":Occupied) , ";

            }
        }
        if (disk.get(disk.size() - 1).isAvailble) {
            list += "(" + disk.get(disk.size() - 1).PID + ":"
                    + disk.get(disk.size() - 1).memoryUse + ":Hole) ]";
        } else {
            list += "(" + disk.get(disk.size() - 1).PID + ":"
                    + disk.get(disk.size() - 1).memoryUse + ":Occupied) ]";
        }
        return list;
    }

    @Override
    public String toString() {
        String list = "[ ";
        for (int i = 0; i < processChain.size() - 1; i++) {
            if (processChain.get(i).isAvailble) {
                list += "(" + processChain.get(i).PID + ":" + processChain.get(i).memoryUse + ":Hole) , ";
            } else {
                list += "(" + processChain.get(i).PID + ":" + processChain.get(i).memoryUse + ":Occupied) , ";

            }
        }
        if (processChain.get(processChain.size() - 1).isAvailble) {
            list += "(" + processChain.get(processChain.size() - 1).PID + ":"
                    + processChain.get(processChain.size() - 1).memoryUse + ":Hole) ]";
        } else {
            list += "(" + processChain.get(processChain.size() - 1).PID + ":"
                    + processChain.get(processChain.size() - 1).memoryUse + ":Occupied) ]";
        }
        return list;
    }

    class MyProcess {
        int PID;
        int memoryUse;
        boolean isAvailble;

        MyProcess(int id, int memory, boolean bool) {
            PID = id;
            memoryUse = memory;
            isAvailble = bool;
        }
    }

    public DefragmentationResult swap(int id, int memory) {
        boolean success = false;
        int movedProcessNum = 0;

        for (int i = 0; i < processChain.size(); i++) {
            if (!processChain.get(i).isAvailble) {
                MyProcess p = processChain.get(i);
                MemoryProcessList copy = makeCopyOfProcessList();
                // set it to a hole
                copy.processChain.get(i).PID = 0;
                copy.processChain.get(i).isAvailble = true;
                copy.combine();
                // Try to add
                boolean tryAdd = copy.firstFitAdd(id, memory);
                if (tryAdd) {
                    this.processChain = copy.processChain;
                    this.disk.add(p);

                    success = true;
                    movedProcessNum = 1;
                    break;
                }
            }
        }

        return new DefragmentationResult(success, movedProcessNum);
    }

    public DefragmentationResult compactToEnd(int processSize) {
        int movedProcessNum = 0;

        // Collect allocated processes in the chain
        LinkedList<MemoryProcessList.MyProcess> allocated = new LinkedList<>();
        for (MemoryProcessList.MyProcess p : processChain) {
            if (!p.isAvailble) {
                allocated.add(p);
            }
        }
        
        // Calculate total used memory
        int totalUsed = 0;
        for (MemoryProcessList.MyProcess p : allocated) {
            totalUsed += p.memoryUse;
        }
        
        // Calculate available space
        int emptySpaceAtStart = MAX_SPACE - totalUsed;
        if (emptySpaceAtStart < processSize)
            return new DefragmentationResult(false, 0);
        
        // Build new process chain
        LinkedList<MemoryProcessList.MyProcess> newChain = new LinkedList<>();
        
        // Add empty block at beginning
        if (emptySpaceAtStart > 0) {
            MemoryProcessList.MyProcess emptyBlock = new MyProcess(0, emptySpaceAtStart, true);
            newChain.add(emptyBlock);
        }
        
        // Add processes to the end
        for (MemoryProcessList.MyProcess p : allocated) {
            newChain.add(p);
            movedProcessNum++;
        }
        
        // Update memory list
        setProcessChain(newChain);
        setSpaceRemaining(emptySpaceAtStart);

        return new DefragmentationResult(true, movedProcessNum);
    }

    public DefragmentationResult compactUntilLargeHole(int processSize) {
        int movedProcessNum = 0;

        // Exit if there isn't enough space for the process
        if (spaceRemaining < processSize)
            return new DefragmentationResult(false, 0);
        
        int accumulatedFree = 0;
        int splitIndex = processChain.size();
        // Scan from right side for free space until combined free space blocks can fit the new process
        for (int i = processChain.size() - 1; i >= 0; i--) {
            MemoryProcessList.MyProcess block = processChain.get(i);
            if (block.isAvailble) {
                accumulatedFree += block.memoryUse;
                splitIndex = i;
                if (accumulatedFree >= processSize) {
                    break;
                }
            }
        }

        // Build new process chain
        LinkedList<MemoryProcessList.MyProcess> newChain = new LinkedList<>();
        
        // Add processes and free space before the split point
        int totalFreeSpace = 0;
        for (int i = 0; i < splitIndex; i++) {
            MemoryProcessList.MyProcess block = processChain.get(i);
            newChain.add(block);
            if (block.isAvailble) {
                totalFreeSpace += block.memoryUse;
            }
            else
                movedProcessNum++;
        }
        totalFreeSpace += accumulatedFree;
        
        // Add the accumulated free space as one block
        newChain.add(new MyProcess(0, accumulatedFree, true));
        
        // Add all processes to the right of the split index after the free space block
        for (int i = splitIndex; i < processChain.size(); i++) {
            if (!processChain.get(i).isAvailble) {
                newChain.add(processChain.get(i));
                movedProcessNum++;
            }
        }
        
        // Update memory
        setProcessChain(newChain);
        setSpaceRemaining(totalFreeSpace);

        return new DefragmentationResult(true, movedProcessNum);
    }

    public DefragmentationResult compactHeuristically(int processSize) {
        // Variables to track best results from moving processes
        int left = -1;
        int numProcesses = Integer.MAX_VALUE;
        int totalProcessSize = Integer.MAX_VALUE;
        LinkedList<Integer> processesToMove = null;
        int bestDestIndex = -1;
        
        // Loop to determine what process to move to gain the largest amount of memory
        for (int i = 0; i < processChain.size(); i++) {
            if (processChain.get(i).isAvailble) {
                int runningTotal = processChain.get(i).memoryUse;
                int processCount = 0;
                int sizeOfProcess = 0;
                
                for (int j = i + 1; j < processChain.size(); j++) {
                    if (processChain.get(j).isAvailble) {
                        runningTotal += processChain.get(j).memoryUse;
                        
                        // If process fits within the total space of the free + processes + free blocks
                        if (runningTotal >= processSize) {

                            // Check if there is a valid destination for these processes
                            int tempDestIndex = -1;
                            for (int k = 0; k < processChain.size(); k++) {
                                if (processChain.get(k).isAvailble) {
                                    // Prevents use of currently used free blocks
                                    if (k >= i && k <= j) {
                                        continue;
                                    }

                                    // Check if free block can fit the processes
                                    if (processChain.get(k).memoryUse >= sizeOfProcess) {
                                        tempDestIndex = k;
                                        break;
                                    }
                                }
                            }
                            
                            // If a valid destination exists
                            if (tempDestIndex != -1) {
                                // Compare with current best
                                if (processCount < numProcesses || (processCount == numProcesses && sizeOfProcess < totalProcessSize)) {
                                    
                                    // Update values
                                    left = i;
                                    numProcesses = processCount;
                                    totalProcessSize = sizeOfProcess;
                                    bestDestIndex = tempDestIndex;
                                    
                                    // Record which processes to move
                                    processesToMove = new LinkedList<>();
                                    for (int k = i + 1; k < j; k++) {
                                        if (!processChain.get(k).isAvailble) {
                                            processesToMove.add(k);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    } else {
                        // Add process values to current values
                        runningTotal += processChain.get(j).memoryUse;
                        processCount++;
                        sizeOfProcess += processChain.get(j).memoryUse;
                    }
                }
            }
        }

        if (left == -1) {
            return new DefragmentationResult(false, 0);
        }
        
        // Look for a free block that can fit the moved processes
        int destIndex = bestDestIndex;
        LinkedList<MemoryProcessList.MyProcess> processedMoved = new LinkedList<>();
        for (int idx : processesToMove) {
            processedMoved.add(processChain.get(idx));
        }
        
        // Remove processes from their original positions
        for (int i = processesToMove.size() - 1; i >= 0; i--) {
            processChain.remove((int)processesToMove.get(i));
        }
        
        // Adjust destIndex if it changed due to removals
        int adjustedDestIndex = destIndex;
        if (destIndex > processesToMove.get(0)) {
            // If destination was after removed processes, adjust index
            for (int idx : processesToMove) {
                if (destIndex > idx) {
                    adjustedDestIndex--;
                }
            }
        }
    
        // Insert moved processes at destination
        int insertIndex = adjustedDestIndex + 1;
        processChain.addAll(insertIndex, processedMoved);

        // Handle the free block at destination
        int remainingFree = processChain.get(adjustedDestIndex).memoryUse - totalProcessSize;
        if (remainingFree > 0) {
            processChain.get(adjustedDestIndex).memoryUse = remainingFree;
        } else {
            processChain.remove(adjustedDestIndex);
        }
        
        // Update memory
        setProcessChain(processChain);
        
        // Recalculate total free space
        int totalFree = 0;
        for (MemoryProcessList.MyProcess p : processChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        setSpaceRemaining(totalFree);

        return new DefragmentationResult(true, processedMoved.size());
    }

    public void addProcessRightAligned(int processId, int processSize) {
        // Find location of free space to place memory
        int freeIndex = -1;
        int freeSize = 0;
        for (int i = 0; i < processChain.size(); i++) {
            MemoryProcessList.MyProcess block = processChain.get(i);
            if (block.isAvailble && block.memoryUse >= processSize) {
                freeIndex = i;
                freeSize = block.memoryUse;
                break;
            }
        }
        
        int remainingFree = freeSize - processSize;
        
        // Align new process to right side of free space
        if (remainingFree > 0) {
            processChain.get(freeIndex).memoryUse = remainingFree;
            processChain.add(freeIndex + 1, new MyProcess(processId, processSize, false));
        } else {
            processChain.set(freeIndex, new MyProcess(processId, processSize, false));
        }
        
        // Calculate total remaining free space
        int totalFree = 0;
        for (MemoryProcessList.MyProcess p : processChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        setSpaceRemaining(totalFree);
    }

    public MemoryProcessList makeCopyOfProcessList() {
        MemoryProcessList copy = new MemoryProcessList(MAX_SPACE);

        // Remove the default hole created by constructor
        copy.processChain.clear();

        // Deep copy processChain
        for (int i = 0; i < this.processChain.size(); i++) {
            MyProcess p = this.processChain.get(i);
            copy.processChain.add(new MyProcess(p.PID, p.memoryUse,p.isAvailble));
        }

        copy.disk.clear();
        for (int i = 0; i < this.disk.size(); i++) {
            MyProcess p = this.disk.get(i);

            copy.disk.add(new MyProcess(
                    p.PID,
                    p.memoryUse,
                    p.isAvailble));
        }

        copy.spaceRemaining = this.spaceRemaining;

        return copy;
    }

    public LinkedList<MyProcess> getDisk() {
        return disk;
    }

    public void setDisk(LinkedList<MyProcess> disk) {
        this.disk = disk;
    }

    public int getDEFAULT_HOLE_ID() {
        return DEFAULT_HOLE_ID;
    }

    // Fronted Stuff
    public LinkedList<LinkedList<Integer>> getDataForFrontend() {
        LinkedList<LinkedList<Integer>> data = new LinkedList<>();
        
        int start = 0;
        for (int i = 0; i < processChain.size(); i++) {
            var process = processChain.get(i);
            if (!process.isAvailble) {
                LinkedList<Integer> processData = new LinkedList<>();
                processData.add(process.PID);
                processData.add(start);
                processData.add((int)process.memoryUse);
                data.add(processData);
            }
            start += process.memoryUse;
        }

        return data;
    }

    public void Randomize() {
        Clear();

        int i = 0;
        int pid_increment = 1;
        Random rand = new Random();
        int r = rand.nextInt(0, 2);
        int spaceHole = 0;

        while (spaceRemaining > 0) {
            int size = rand.nextInt(Math.min(MIN_RAND_PROCESS_SIZE, spaceRemaining), Math.min(MAX_RAND_PROCESS_SIZE, spaceRemaining) + 1);

            boolean b = i % 2 == r;
            if (!b && size < MIN_RAND_PROCESS_SIZE)
                b = true;

            int pid = 0;
            if (!b) {
                pid = pid_increment;
                pid_increment++;
            }
            var process = new MyProcess(pid, size, b);

            processChain.add(process);

            if (b)
                spaceHole += size;
            
            i++;
            spaceRemaining -= size;
        }

        spaceRemaining = spaceHole;
    }

    public void Clear() {
        processChain.clear();
        disk.clear();
        spaceRemaining = MAX_SPACE;
    }
}
