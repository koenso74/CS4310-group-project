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
        
        // Calculate available space
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
        // Exit if there isn't enough space for the process
        if (memoryList.getSpaceRemaining() < processSize) {
            System.out.println("Not enough total free space");
            return;
        }

        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        double accumulatedFree = 0;
        int splitIndex = currentChain.size();
        // Scan from right side for free space until combined free space blocks can fit the new process
        for (int i = currentChain.size() - 1; i >= 0; i--) {
            MemoryProcessList.MyProcess block = currentChain.get(i);
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
        
        double totalFreeSpace = 0;
        // Add processes and free space before the split point
        for (int i = 0; i < splitIndex; i++) {
            MemoryProcessList.MyProcess block = currentChain.get(i);
            newChain.add(block);
            if (block.isAvailble) {
                totalFreeSpace += block.memoryUse;
            }
        }
        totalFreeSpace += accumulatedFree;
        
        // Add the accumulated free space as one block
        newChain.add(memoryList.new MyProcess(0, accumulatedFree, true));
        
        // Add all processes to the right of the split index after the free space block
        for (int i = splitIndex; i < currentChain.size(); i++) {
            if (!currentChain.get(i).isAvailble) {
                newChain.add(currentChain.get(i));
            }
        }
        
        // Update memory
        memoryList.setProcessChain(newChain);
        memoryList.setSpaceRemaining(totalFreeSpace);
    }

    public static void main(String[] args) {
        MemoryProcessList memory = new MemoryProcessList(1000);
        memory.addProcessAt(1, 200, 0);
        memory.addProcessAt(2, 300, 300);
        memory.addProcessAt(3, 100, 700);
        
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
