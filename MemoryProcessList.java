import java.util.LinkedList;
public class MemoryProcessList{
    private LinkedList<MyProcess> processChain;
    private double spaceRemaining;
    private final double MAX_SPACE;

    public MemoryProcessList(double max){
        MAX_SPACE = max;
        spaceRemaining = max;
        processChain = new LinkedList<>();
        processChain.add(new MyProcess(0, max, true));
    }

    private void combine(){
        for(int i = 0; i < processChain.size()-1; i++){
            if(processChain.get(i).isAvailble && processChain.get(i+1).isAvailble){
                MyProcess old = processChain.remove(i);
                processChain.get(i).memoryUse += old.memoryUse;
            }
        }
    }

    public void addProcessAt(int id, double memory, double start){
        int i = 0;
        boolean foundIndex = false;
        double begin = 0.0;
        double end = 0.0;
        while(i < processChain.size() && !foundIndex){
            if(start <= processChain.get(i).memoryUse){
                foundIndex = true;
                end = begin + processChain.get(i).memoryUse;
            }
            else{
                begin += processChain.get(i).memoryUse;
                i++;
            }
        }

        if(!foundIndex){
            System.out.println("Index Not Found");
        }
        else{
            if(start == end){
                processChain.get(i).memoryUse -= memory;
                if(processChain.get(i).memoryUse <= 0){
                    processChain.remove(i);
                }
                spaceRemaining -= memory;
                processChain.add(i+1, new MyProcess(1, memory, false));
            }
            else if (start == begin){
                processChain.get(i).memoryUse -= memory;
                if(processChain.get(i).memoryUse <= 0){
                    processChain.remove(i);
                }
                spaceRemaining -= memory;
                processChain.add(i, new MyProcess(1, memory, false));
            }
            else{
                processChain.get(i).memoryUse -= memory;
                if(processChain.get(i).memoryUse <= 0){
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
                processChain.add(i, new MyProcess(id, pToEnd, true));
            }
            combine();
        }
    }

    public void firstFitAdd(int id, double memory){
        MyProcess newP = new MyProcess(id, memory, false); //Creates new process
        boolean hasFound = false; //Loop Cond
        int i = 0; //Loop Cond

        //Check for the first spot to put in
        while(i < processChain.size() && !hasFound){
            MyProcess cur = processChain.get(i);

            //Check current process. If true, then update
            if(cur.isAvailble && cur.memoryUse >= memory){
                cur.memoryUse -= memory;
                if(cur.memoryUse <= 0){
                    processChain.remove(i);
                }
                spaceRemaining -= memory;
                processChain.add(newP);
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

    public MyProcess getProcessAt(int index){
        return processChain.get(index);
    }

    public int getProcessID(int index){
        return processChain.get(index).PID;
    }

    public double getProcessMemoryUse(int index){
        return processChain.get(index).memoryUse;
    }

    public boolean getProcessAvailblity(int index){
        return processChain.get(index).isAvailble;
    }

    @Override
    public String toString(){
        String list = "[ ";
        for(int i = 0; i < processChain.size()-1; i++){
            list += "("+processChain.get(i).PID+":"+processChain.get(i).memoryUse+":"+processChain.get(i).isAvailble+") , ";
        }
        list += "("+processChain.get(processChain.size()-1).PID+":"+processChain.get(processChain.size()-1).memoryUse+":"+processChain.get(processChain.size()-1).isAvailble+") ]";
        return list;
    }

    class MyProcess {
        int PID;
        double memoryUse;
        boolean isAvailble;

        MyProcess(int id, double memory, boolean bool){
            PID = id;
            memoryUse = memory;
            isAvailble = bool;
        }
    }
}

