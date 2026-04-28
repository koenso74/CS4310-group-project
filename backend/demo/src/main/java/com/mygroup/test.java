package com.mygroup;

public class test {
    public static void main(String[] args) {
        MemoryProcessList l = new MemoryProcessList(2000);
        l.Randomize();
        System.out.println(l);
    }
}
