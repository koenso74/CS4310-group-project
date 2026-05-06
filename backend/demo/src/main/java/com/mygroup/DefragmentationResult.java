package com.mygroup;

public class DefragmentationResult {
    public boolean success;
    public int movedProcessNum;

    public DefragmentationResult(boolean success, int movedProcessNum) {
        this.success = success;
        this.movedProcessNum = movedProcessNum;
    }
}
