public class Swapping {
    public static void main(String[] args) {
        MemoryProcessList l = new MemoryProcessList(2000);
        System.out.println(l);
        System.out.println(l.addProcessAt(1, 1000, 1000));
        System.out.println(l);
        System.out.println(l.addProcessAt(2, 300, 0));
        System.out.println(l);
        System.out.println(l.swap(3, 800));
        System.out.println(l);
        l.removeProcess(2);
        System.out.println(l);
    }

    
}
