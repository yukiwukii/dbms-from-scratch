package Bplustree;

import IO.FileManager;

public final class PageLayout {
    private PageLayout() {}

    // Page header
    public static final byte TYPE_LEAF = 1;
    public static final byte TYPE_INTERNAL = 2;
    public static final int OFF_TYPE = 0;        // byte
    public static final int OFF_KEYCOUNT = 1;    // short (unsigned)
    public static final int OFF_NEXT = 4;        // int (leaf next pointer)
    public static final int HEADER_BYTES = 16;

    // Entry sizes
    // Leaf entry: key(float) + rid.page(int) + rid.slot(int)
    public static final int LEAF_ENTRY_BYTES = 4 + 4 + 4;

    // Internal page: [firstChild(int)] then repeating [sepKey(float), rightChild(int)]
    public static final int INTERNAL_FIRST_CHILD_BYTES = 4;
    public static final int INTERNAL_ENTRY_BYTES = 4 + 4;

    public static int leafCapacity() {
        return (FileManager.PAGE_SIZE - HEADER_BYTES) / LEAF_ENTRY_BYTES;
    }
    public static int internalCapacity() {
        int body = FileManager.PAGE_SIZE - HEADER_BYTES - INTERNAL_FIRST_CHILD_BYTES;
        return body / INTERNAL_ENTRY_BYTES; // number of keys (children = keys + 1)
    }
}

