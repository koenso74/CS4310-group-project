package com.mygroup;

import java.util.LinkedList;
import java.util.Random;

public class MemoryProcessList {
    private LinkedList<MyProcess> processChain;
    private int spaceRemaining;
    private final int MAX_SPACE;
    private LinkedList<MyProcess> disk;
    private final int DEFAULT_HOLE_ID = 0;
    private final int MIN_RAND_PROCESS_SIZE = 100;
    private final int MAX_RAND_PROCESS_SIZE = 1000;

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

    public boolean swap(int id, int memory) {
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
                    return true;
                }
            }
        }
        return false;
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
        Random rand = new Random();
        int r = rand.nextInt(0, 2);

        while (spaceRemaining > 0) {
            int size = rand.nextInt(Math.min(MIN_RAND_PROCESS_SIZE, spaceRemaining), Math.min(MAX_RAND_PROCESS_SIZE, spaceRemaining) + 1);

            boolean b = i % 2 == r;
            if (!b && size < MIN_RAND_PROCESS_SIZE)
                b = true;
            var process = new MyProcess(i, size, b);

            processChain.add(process);
            
            i++;
            spaceRemaining -= size;
        }
    }

    public void Clear() {
        processChain.clear();
        disk.clear();
        spaceRemaining = MAX_SPACE;
    }
}
