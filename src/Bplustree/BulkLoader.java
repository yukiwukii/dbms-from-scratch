package Bplustree;

import IO.FileManager;
import storage.HeapFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BulkLoader {

    // Pair of (key, RID) for sorting and leaf fill
    public static final class KeyRid {
        public final float key;
        public final HeapFile.RecordId rid;
        public KeyRid(float key, HeapFile.RecordId rid) { this.key = key; this.rid = rid; }
    }

    // Minimal info per child to build parents
    private static final class NodeRef {
        final int pageId;
        final float minKey;
        NodeRef(int pageId, float minKey) { this.pageId = pageId; this.minKey = minKey; }
    }

    public static final class Stats {
        public final int rootPid, height, leafCount, internalCount;
        public final float[] rootKeys;  // separator keys in the root (empty if root is a leaf)
        public Stats(int rootPid, int height, int leafCount, int internalCount, float[] rootKeys) {
            this.rootPid = rootPid; this.height = height; this.leafCount = leafCount;
            this.internalCount = internalCount; this.rootKeys = rootKeys;
        }
    }

    // Read all records from heap, build (key, rid) list, sort by key
    private static List<KeyRid> readAllPairs(HeapFile heap) throws IOException {
        var rows = heap.scanAll(); // returns List<HeapFile.RecordWithId>
        List<KeyRid> out = new ArrayList<>(rows.size());
        for (var rw : rows) out.add(new KeyRid(rw.rec.getFtPctHome(), rw.rid));
        out.sort(Comparator.comparingDouble(p -> p.key));
        return out;
    }

    // Helper: read separator keys from an internal page
    private static float[] readInternalKeys(FileManager fm, int pageId) throws IOException {
        ByteBuffer b = fm.readPage(pageId);
        if (b.get(PageLayout.OFF_TYPE) != PageLayout.TYPE_INTERNAL) return new float[0];
        int k = Short.toUnsignedInt(b.getShort(PageLayout.OFF_KEYCOUNT));
        float[] keys = new float[k];
        int base = PageLayout.HEADER_BYTES + PageLayout.INTERNAL_FIRST_CHILD_BYTES;
        for (int i = 0; i < k; i++) {
            keys[i] = b.getFloat(base + i * PageLayout.INTERNAL_ENTRY_BYTES);
        }
        return keys;
    }

    public static Stats build(FileManager fm, HeapFile heap) throws IOException {
        List<KeyRid> pairs = readAllPairs(heap);

        // drop previous index so we don't append forever
        int rpb = (IO.FileManager.PAGE_SIZE - storage.HeapFile.HEADER_BYTES) / Util.FixedRecordSize.RECORD_SIZE;
        long dataPages = (long) Math.ceil((double) pairs.size() / rpb);
        long keepPages = 1 + Math.max(0, dataPages); // superblock + heap data pages
        fm.truncateToPages(keepPages);

        // 1) Build leaves (linked list), collecting NodeRefs with minKey
        List<NodeRef> current = new ArrayList<>();
        LeafPage leaf = null;
        int leafCount = 0;
        float currentMin = 0;

        for (KeyRid p : pairs) {
            if (leaf == null) {
                leaf = LeafPage.create(fm);
                currentMin = p.key;
                leafCount++;
            }
            if (leaf.isFull()) {
                LeafPage next = LeafPage.create(fm);
                leaf.setNext(next.pageId());
                leaf.flush();
                current.add(new NodeRef(leaf.pageId(), currentMin));

                leaf = next;
                leafCount++;
                currentMin = p.key;
            }
            leaf.add(p.key, p.rid);
        }
        if (leaf == null) { // empty dataset -> one empty leaf
            leaf = LeafPage.create(fm);
            leafCount = 1;
            currentMin = Float.NEGATIVE_INFINITY;
        }
        leaf.flush();
        current.add(new NodeRef(leaf.pageId(), currentMin));

        int height = 1;
        int internalCount = 0;

        // 2) Build parents level-by-level until one root remains
        while (current.size() > 1) {
            List<NodeRef> nextLevel = new ArrayList<>();

            int i = 0;
            while (i < current.size()) {
                InternalPage parent = InternalPage.create(fm);
                internalCount++;

                // group up to (capacity + 1) children per internal node
                int cap = parent.capacity();
                int start = i;
                parent.setFirstChild(current.get(i).pageId);
                float parentMinKey = current.get(i).minKey;
                i++;

                int appended = 0;
                while (i < current.size() && appended < cap) {
                    parent.append(current.get(i).minKey, current.get(i).pageId);
                    i++; appended++;
                }
                parent.flush();

                nextLevel.add(new NodeRef(parent.pageId(), parentMinKey));
            }

            current = nextLevel;
            height++;
        }

        int rootPid = current.get(0).pageId;
        float[] rootKeys = (height == 1) ? new float[0] : readInternalKeys(fm, rootPid);
        return new Stats(rootPid, height, leafCount, internalCount, rootKeys);
    }
}

