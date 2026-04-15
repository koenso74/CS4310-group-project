public class Swapping {
    public static void main(String[] args) {
        MemoryProcessList l = new MemoryProcessList(2000);
        System.out.println(l);
        l.addProcessAt(1, 1000, 500);
        System.out.println(l);
        l.addProcessAt(2, 100, 300);
        System.out.println(l);
        l.addProcessAt(3, 200, 300);
        System.out.println(l);
        l.addProcessAt(4, 200, 300);
        System.out.println(l);
    }

    
}
