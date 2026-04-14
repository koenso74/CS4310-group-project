import java.util.LinkedList;
import java.util.Scanner;

public class MemoryCompaction {

    public static void compactToEnd(MemoryProcessList memoryList) {
        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        // Collect allocated processes in the chain
        LinkedList<MemoryProcessList.MyProcess> allocated = new LinkedList<>();
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (!p.isAvailble) {
                allocated.add(p);
            }
        }
        
        // Calculate total used memory
        double totalUsed = 0;
        for (MemoryProcessList.MyProcess p : allocated) {
            totalUsed += p.memoryUse;
        }
        
        // Calculate available space at beginning
        double emptySpaceAtStart = memoryList.getMAX_SPACE() - totalUsed;
        
        // Build new process chain
        LinkedList<MemoryProcessList.MyProcess> newChain = new LinkedList<>();
        
        // Add empty block at beginning
        if (emptySpaceAtStart > 0) {
            MemoryProcessList.MyProcess emptyBlock = memoryList.new MyProcess(0, emptySpaceAtStart, true);
            newChain.add(emptyBlock);
        }
        
        // Add processes to the end
        for (MemoryProcessList.MyProcess p : allocated) {
            newChain.add(p);
        }
        
        // Update memory list
        memoryList.setProcessChain(newChain);
        memoryList.setSpaceRemaining(emptySpaceAtStart);
    }

    public static void compactUntilLargeHole(MemoryProcessList memoryList, double processSize) {

        if (memoryList.getSpaceRemaining() < processSize) {
            System.out.println("Not enough total free space");
            return;
        }
        
        // Find current largest hole
        double largestHole = findLargestHole(memoryList);
        
        // Hole is large enough, exit
        if (largestHole >= processSize) { 
            return;
        }
        
        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        // Collect allocated processes in the chain
        LinkedList<MemoryProcessList.MyProcess> allocated = new LinkedList<>();
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (!p.isAvailble) {
                allocated.add(p);
            }
        }

        // Calculate total used memory
        double totalUsed = 0;
        for (MemoryProcessList.MyProcess p : allocated) {
            totalUsed += p.memoryUse;
        }
        
        double freeSpaceAtStart = memoryList.getMAX_SPACE() - totalUsed;
        
        // Build new compacted layout
        LinkedList<MemoryProcessList.MyProcess> newChain = new LinkedList<>();
        
        if (freeSpaceAtStart > 0) {
            MemoryProcessList.MyProcess freeBlock = memoryList.new MyProcess(0, freeSpaceAtStart, true);
            newChain.add(freeBlock);
        }
        
        newChain.addAll(allocated);
        
        memoryList.setProcessChain(newChain);
        memoryList.setSpaceRemaining(freeSpaceAtStart);
    }

    private static double findLargestHole(MemoryProcessList memory) {
        double largest = 0;
        
        for (MemoryProcessList.MyProcess p : memory.getProcessChain()) {
            if (p.isAvailble && p.memoryUse > largest) {
                largest = p.memoryUse;
            }
        }
        
        return largest;
    }

    public static void main(String[] args) {
        MemoryProcessList memory = new MemoryProcessList(1000);
        memory.addProcessAt(1, 200, 0);
        memory.addProcessAt(2, 300, 300);
        memory.addProcessAt(3, 150, 700);
        
        displayMemoryLayout(memory);

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Which memory compaction algorithm to use?: ");
        System.out.println("(1) Compact to end");
        System.out.println("(2) Compact until a large enough hole");
        System.out.println("(3) Compact");
        int choice = scanner.nextInt();
        
        switch(choice) {
            case 1:
                compactToEnd(memory);
                break;
            case 2:
                compactUntilLargeHole(memory, 400);
                break;
            case 3:
                System.out.println("You selected option 3");
                break;
            default:
                System.out.println("Invalid choice! Please enter 1, 2, or 3");
        }

        memory.firstFitAdd(4, 400);
        displayMemoryLayout(memory);
    }

    public static void displayMemoryLayout(MemoryProcessList memory) {
        double currentAddress = 0;
        System.out.println("Memory Layout (address: process/free space size):");
        System.out.println("   Address Range     | Type    | Size");
        System.out.println("   -----------------|---------|-------");
        
        for (MemoryProcessList.MyProcess p : memory.getProcessChain()) {
             String type = p.isAvailble ? "FREE     " : "PROCESS " + p.PID;
            System.out.printf("   [%5.0f - %5.0f] | %s | %5.0f\n", currentAddress, currentAddress + p.memoryUse, type, p.memoryUse);
            currentAddress += p.memoryUse;
        }
        
        System.out.println("   -----------------|---------|-------");
        System.out.printf("   Total: %.0f units | Free: %.0f units\n", memory.getMAX_SPACE(), memory.getSpaceRemaining());
    }

}
