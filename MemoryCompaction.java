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
        
        // Add processes and free space before the split point
        double totalFreeSpace = 0;
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

    public static void compactHeuristically(MemoryProcessList memoryList, double processSize) {
        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
    
        // Variables to track best results from moving processes
        int left = -1;
        int numProcesses = Integer.MAX_VALUE;
        double totalProcessSize = Double.MAX_VALUE;
        LinkedList<Integer> processesToMove = null;
        int bestDestIndex = -1;
        
        // Loop to determine what process to move to gain the largest amount of memory
        for (int i = 0; i < currentChain.size(); i++) {
            if (currentChain.get(i).isAvailble) {
                double runningTotal = currentChain.get(i).memoryUse;
                int processCount = 0;
                double sizeOfProcess = 0;
                
                for (int j = i + 1; j < currentChain.size(); j++) {
                    if (currentChain.get(j).isAvailble) {
                        runningTotal += currentChain.get(j).memoryUse;
                        
                        // If process fits within the total space of the free + processes + free blocks
                        if (runningTotal >= processSize) {

                            // Check if there is a valid destination for these processes
                            int tempDestIndex = -1;
                            for (int k = 0; k < currentChain.size(); k++) {
                                if (currentChain.get(k).isAvailble) {
                                    // Prevents use of currently used free blocks
                                    if (k >= i && k <= j) {
                                        continue;
                                    }

                                    // Check if free block can fit the processes
                                    if (currentChain.get(k).memoryUse >= sizeOfProcess) {
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
                                        if (!currentChain.get(k).isAvailble) {
                                            processesToMove.add(k);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    } else {
                        // Add process values to current values
                        runningTotal += currentChain.get(j).memoryUse;
                        processCount++;
                        sizeOfProcess += currentChain.get(j).memoryUse;
                    }
                }
            }
        }

        if (left == -1) {
            System.out.println("No best fitting locations");
            return;
        }
        
        // Look for a free block that can fit the moved processes
        int destIndex = bestDestIndex;
        LinkedList<MemoryProcessList.MyProcess> processedMoved = new LinkedList<>();
        for (int idx : processesToMove) {
            processedMoved.add(currentChain.get(idx));
        }
        
        // Remove processes from their original positions
        for (int i = processesToMove.size() - 1; i >= 0; i--) {
            currentChain.remove((int)processesToMove.get(i));
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
        currentChain.addAll(insertIndex, processedMoved);

        // Handle the free block at destination
        double remainingFree = currentChain.get(adjustedDestIndex).memoryUse - totalProcessSize;
        if (remainingFree > 0) {
            currentChain.get(adjustedDestIndex).memoryUse = remainingFree;
        } else {
            currentChain.remove(adjustedDestIndex);
        }
        
        // Update memory
        memoryList.setProcessChain(currentChain);
        
        // Recalculate total free space
        double totalFree = 0;
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        memoryList.setSpaceRemaining(totalFree);
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
        System.out.println("(3) Compact using heuristics");
        int choice = scanner.nextInt();
        
        switch(choice) {
            case 1:
                compactToEnd(memory);
                break;
            case 2:
                compactUntilLargeHole(memory, 200);
                break;
            case 3:
                compactHeuristically(memory, 200);
                break;
            default:
                System.out.println("Invalid choice! Please enter 1, 2, or 3");
        }

        addProcessRightAligned(memory, 4, 200);
        displayMemoryLayout(memory);

        scanner.close();
    }

    public static void addProcessRightAligned(MemoryProcessList memoryList, int processId, double processSize) {
        // Get process chain after compaction
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        // Find location of free space to place memory
        int freeIndex = -1;
        double freeSize = 0;
        for (int i = 0; i < currentChain.size(); i++) {
            MemoryProcessList.MyProcess block = currentChain.get(i);
            if (block.isAvailble && block.memoryUse >= processSize) {
                freeIndex = i;
                freeSize = block.memoryUse;
                break;
            }
        }
        
        double remainingFree = freeSize - processSize;
        
        // Align new process to right side of free space
        if (remainingFree > 0) {
            currentChain.get(freeIndex).memoryUse = remainingFree;
            currentChain.add(freeIndex + 1, memoryList.new MyProcess(processId, processSize, false));
        } else {
            currentChain.set(freeIndex, memoryList.new MyProcess(processId, processSize, false));
        }
        
        // Calculate total remaining free space
        double totalFree = 0;
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        memoryList.setSpaceRemaining(totalFree);
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
