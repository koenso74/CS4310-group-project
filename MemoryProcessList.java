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

    public void addProcessAt(int id, double memory, double start) {
        int i = 0;
        boolean foundIndex = false;
        double begin = 0.0;
        double end = 0.0;
        while (i < processChain.size() && !foundIndex) {
            if (start <= processChain.get(i).memoryUse) {
                foundIndex = true;
                end = begin + processChain.get(i).memoryUse;
            } else {
                begin += processChain.get(i).memoryUse;
                i++;
            }
        }

        if (!foundIndex) {
            System.out.println("Index Not Found");
        } else {
            if (start == end) {
                processChain.get(i).memoryUse -= memory;
                if (processChain.get(i).memoryUse <= 0) {
                    processChain.remove(i);
                }
                spaceRemaining -= memory;
                processChain.add(i + 1, new MyProcess(1, memory, false));
            } else if (start == begin) {
                processChain.get(i).memoryUse -= memory;
                if (processChain.get(i).memoryUse <= 0) {
                    processChain.remove(i);
                }
                spaceRemaining -= memory;
                processChain.add(i, new MyProcess(1, memory, false));
            } else if (start > begin && start < end) {
                processChain.get(i).memoryUse -= memory;
                if (processChain.get(i).memoryUse <= 0) {
                    processChain.remove(i);

                }
                spaceRemaining -= memory;
                double startToP = start;
                double pToEnd = end - (start + memory);
                processChain.add(i, new MyProcess(id, startToP, true));
                i++;
                processChain.get(i).isAvailble = false;
                processChain.get(i).memoryUse = memory;
                i++;
                processChain.add(i, new MyProcess(DEFAULT_HOLE_ID, pToEnd, true));
            }
            else{
                firstFitAdd(id, memory);
            }
            combine();
        }
    }

    public void firstFitAdd(int id, double memory) {
        MyProcess newP = new MyProcess(id, memory, false);
        boolean hasFound = false;
        int i = 0;

        while (i < processChain.size() && !hasFound) {
            MyProcess cur = processChain.get(i);

            if (cur.isAvailble && cur.memoryUse >= memory) {
                if (cur.memoryUse == memory) {
                    processChain.set(i, newP);
                }
                else {
                    cur.memoryUse -= memory;
                    processChain.add(i, newP);
                }

                spaceRemaining -= memory;
                hasFound = true;
            }
            i++;
        }

        combine();
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

    public void swap(int id, double memory){
        //Find best fitting index
        LinkedList<Integer> bestFits = new LinkedList<>(); 
        LinkedList<Integer> largeFits = new LinkedList<>(); 
        for(int i = 0; i < processChain.size(); i++){
            MyProcess p = processChain.get(i);
            LinkedList<Integer> curset = new LinkedList<>();
            curset.add(i);
            double total = p.memoryUse;
            if(total > memory){
                largeFits = curset;
            }
            else if (total == memory){
                bestFits = curset;
            }

            if(p.isAvailble){
                for(int j = i+1; j < processChain.size(); j++){
                    curset.add(j);
                    total += processChain.get(j).memoryUse;
                    if(total > memory){
                        largeFits.put(total, curset);
                    }
                    else if (total == memory){
                        bestFits.add(curset);
                    }
                }
            }
            else{
                int k = i+1;
                while(k < processChain.size() && !processChain.get(k).isAvailble){
                    curset.add(k);
                    k++;
                }

                for(int j = k; j < processChain.size(); j++){
                    curset.add(j);
                    total += processChain.get(j).memoryUse;
                    if(total > memory){
                        largeFits.put(total, curset);
                    }
                    else if (total == memory){
                        bestFits.add(curset);
                    }
                }
            }
        }

        if(!bestFits.isEmpty()){

        }
        else{

        }
    }

    public void compact(){

    }
}
