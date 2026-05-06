package com.mygroup;

import java.util.Collections;
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
        int totalUsed = 0;
        for (MemoryProcessList.MyProcess p : allocated) {
            totalUsed += p.memoryUse;
        }
        
        // Calculate available space
        int emptySpaceAtStart = memoryList.getMAX_SPACE() - totalUsed;
        
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

    public static void compactUntilLargeHole(MemoryProcessList memoryList, int processSize) {
        // Exit if there isn't enough space for the process
        if (memoryList.getSpaceRemaining() < processSize) {
            System.out.println("Not enough total free space");
            return;
        }

        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        int accumulatedFree = 0;
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
        int totalFreeSpace = 0;
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

    public static DefragmentationResult compactHeuristically(MemoryProcessList memoryList, int processSize) {
        // Get current process chain
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();

        // Check if there's already enough contiguous free space
        int largestFree = 0;
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (p.isAvailble && p.memoryUse > largestFree) {
                largestFree = p.memoryUse;
            }
        }
        if (largestFree >= processSize) {
            return new DefragmentationResult(true, 0);
        }

        // Variables to track best results from moving processes
        int left = -1;
        int numProcesses = Integer.MAX_VALUE;
        int totalProcessSize = Integer.MAX_VALUE;
        LinkedList<Integer> processesToMove = null;
        int bestDestIndex = -1;

        // Loop to determine what process to move to gain the largest amount of memory
        for (int i = 0; i < currentChain.size(); i++) {
            if (currentChain.get(i).isAvailble) {
                int runningTotal = currentChain.get(i).memoryUse;
                int processCount = 0;
                int sizeOfProcess = 0;
                
                for (int j = i + 1; j < currentChain.size(); j++) {
                    if (currentChain.get(j).isAvailble) {
                        runningTotal += currentChain.get(j).memoryUse;
                        
                        if (runningTotal >= processSize) {
                            // Check if there is a valid destination for these processes
                            int tempDestIndex = -1;
                            for (int k = 0; k < currentChain.size(); k++) {
                                if (currentChain.get(k).isAvailble && (k < i || k > j)) {
                                    if (currentChain.get(k).memoryUse >= sizeOfProcess) {
                                        tempDestIndex = k;
                                        break;
                                    }
                                }
                            }
                            
                            if (tempDestIndex != -1) {
                                if (processCount < numProcesses || 
                                    (processCount == numProcesses && sizeOfProcess < totalProcessSize)) {
                                    left = i;
                                    numProcesses = processCount;
                                    totalProcessSize = sizeOfProcess;
                                    bestDestIndex = tempDestIndex;
                                    
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
                        runningTotal += currentChain.get(j).memoryUse;
                        processCount++;
                        sizeOfProcess += currentChain.get(j).memoryUse;
                    }
                }
            }
        }

        if (left == -1 || processesToMove == null || processesToMove.isEmpty()) {
            return new DefragmentationResult(false, 0);
        }

        // Sort into descending order
        Collections.sort(processesToMove, Collections.reverseOrder());

        // Collect the processes to be moved
        LinkedList<MemoryProcessList.MyProcess> processedMoved = new LinkedList<>();
        for (int idx : processesToMove) {
            processedMoved.add(currentChain.get(idx));
        }

        // Remove processes from their original positions
        for (int idx : processesToMove) {
            currentChain.remove(idx);
        }

        // Adjust the best destination index if it changed due to removals
        int adjustedDestIndex = bestDestIndex;
        for (int idx : processesToMove) {
            if (bestDestIndex > idx) {
                adjustedDestIndex--;
            }
        }

        // Insert moved processes at destination
        int insertIndex = adjustedDestIndex + 1;
        currentChain.addAll(insertIndex, processedMoved);

        // Handle the free block at destination
        int remainingFree = currentChain.get(adjustedDestIndex).memoryUse - totalProcessSize;
        if (remainingFree > 0) {
            currentChain.get(adjustedDestIndex).memoryUse = remainingFree;
        } else {
            currentChain.remove(adjustedDestIndex);
        }

        // Update memory
        memoryList.setProcessChain(currentChain);

        // Recalculate total free space
        int totalFree = 0;
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        memoryList.setSpaceRemaining(totalFree);

        return new DefragmentationResult(true, numProcesses);
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

    public static void addProcessRightAligned(MemoryProcessList memoryList, int processId, int processSize) {
        // Get process chain after compaction
        LinkedList<MemoryProcessList.MyProcess> currentChain = memoryList.getProcessChain();
        
        // Find location of free space to place memory
        int freeIndex = -1;
        int freeSize = 0;
        for (int i = 0; i < currentChain.size(); i++) {
            MemoryProcessList.MyProcess block = currentChain.get(i);
            if (block.isAvailble && block.memoryUse >= processSize) {
                freeIndex = i;
                freeSize = block.memoryUse;
                break;
            }
        }
        
        int remainingFree = freeSize - processSize;
        
        // Align new process to right side of free space
        if (remainingFree > 0) {
            currentChain.get(freeIndex).memoryUse = remainingFree;
            currentChain.add(freeIndex + 1, memoryList.new MyProcess(processId, processSize, false));
        } else {
            currentChain.set(freeIndex, memoryList.new MyProcess(processId, processSize, false));
        }
        
        // Calculate total remaining free space
        int totalFree = 0;
        for (MemoryProcessList.MyProcess p : currentChain) {
            if (p.isAvailble) {
                totalFree += p.memoryUse;
            }
        }
        memoryList.setSpaceRemaining(totalFree);
    }

    public static void displayMemoryLayout(MemoryProcessList memory) {
        int currentAddress = 0;
        System.out.println("Memory Layout (address: process/free space size):");
        System.out.println("   Address Range     | Type    | Size");
        System.out.println("   -----------------|---------|-------");
        
        for (MemoryProcessList.MyProcess p : memory.getProcessChain()) {
            String type = p.isAvailble ? "FREE     " : "PROCESS " + p.PID;
            System.out.printf("   [%5d - %5d] | %s | %5d\n", currentAddress, currentAddress + p.memoryUse, type, p.memoryUse);
            currentAddress += p.memoryUse;
        }
        
        System.out.println("   -----------------|---------|-------");
        System.out.printf("   Total: %d units | Free: %d units\n", memory.getMAX_SPACE(), memory.getSpaceRemaining());
    }

}
