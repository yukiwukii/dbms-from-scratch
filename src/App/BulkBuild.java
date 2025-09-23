package App;

import IO.FileManager;
import storage.HeapFile;
import Bplustree.BulkLoader;
import Bplustree.PageLayout;

import java.util.Arrays;

public class BulkBuild {
    public static void main(String[] args) throws Exception {
        // No reset
        try (FileManager fm = new FileManager("db.data", false)) {
            HeapFile heap = new HeapFile(fm);

            BulkLoader.Stats s = BulkLoader.build(fm, heap);

            int n = PageLayout.internalCapacity() + 1;     // n = max children per internal node
            int totalNodes = s.leafCount + s.internalCount;

            System.out.println("Parameter n of the B+ tree: " + n);
            System.out.println("Number of nodes of the B+ tree: " + totalNodes);
            System.out.println("Number of levels of the B+ tree: " + s.height);
            System.out.println("Content of the root node (only the keys): " + Arrays.toString(s.rootKeys));
        }
    }
}


