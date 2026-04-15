import java.util.HashMap;
import java.util.LinkedList;

public class MemoryProcessList {
    private LinkedList<MyProcess> processChain;
    private double spaceRemaining;
    private final double MAX_SPACE;
    private LinkedList<MyProcess> disk;
    private final int DEFAULT_HOLE_ID = 0;

    public MemoryProcessList(double max) {
        MAX_SPACE = max;
        spaceRemaining = max;
        processChain = new LinkedList<>();
        processChain.add(new MyProcess(DEFAULT_HOLE_ID, max, true));
        disk = new LinkedList<>();
    }

    private void combine() {
        for (int i = 0; i < processChain.size() - 1; i++) {
            if (processChain.get(i).isAvailble && processChain.get(i + 1).isAvailble) {
                MyProcess old = processChain.remove(i);
                processChain.get(i).memoryUse += old.memoryUse;
            }
        }
    }

    public boolean addProcessAt(int id, double memory, double start) {
        if (memory <= spaceRemaining) {
            // find the index of starting
            boolean hasFound = false;
            int curInd = 0;
            double begin = 0;
            double end = 0;
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

            double leftNode = start - begin;
            double rightNode = end - (start + memory);

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

    public boolean firstFitAdd(int id, double memory) {
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

    public double getSpaceRemaining() {
        return spaceRemaining;
    }

    public double getMAX_SPACE() {
        return MAX_SPACE;
    }

    public MyProcess getProcessAt(int index) {
        return processChain.get(index);
    }

    public int getProcessID(int index) {
        return processChain.get(index).PID;
    }

    public double getProcessMemoryUse(int index) {
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

    public void setSpaceRemaining(double space) {
        this.spaceRemaining = space;
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
        double memoryUse;
        boolean isAvailble;

        MyProcess(int id, double memory, boolean bool) {
            PID = id;
            memoryUse = memory;
            isAvailble = bool;
        }
    }

    public boolean swap(int id, double memory) {
        for (int i = 0; i < processChain.size(); i++) {
            if (!processChain.get(i).isAvailble) {
                MemoryProcessList copy = makeCopyOfProcessList();
                // set it to a hole
                copy.processChain.get(i).PID = 0;
                copy.processChain.get(i).isAvailble = true;
                copy.combine();
                // Try to add
                boolean tryAdd = copy.firstFitAdd(id, memory);
                if (tryAdd) {
                    this.processChain = copy.processChain;
                    this.disk.add(processChain.get(i));
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

    
}
