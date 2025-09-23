package Bplustree;

import IO.FileManager;
import storage.HeapFile;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LeafPage {
    private final FileManager fm;
    private final int pageId;
    private final ByteBuffer buf;

    private LeafPage(FileManager fm, int pageId, ByteBuffer buf) {
        this.fm = fm; this.pageId = pageId; this.buf = buf;
    }

    public static LeafPage create(FileManager fm) throws IOException {
        int pid = fm.allocatePage();
        ByteBuffer b = ByteBuffer.allocate(FileManager.PAGE_SIZE);
        b.put(PageLayout.OFF_TYPE, PageLayout.TYPE_LEAF);
        b.putShort(PageLayout.OFF_KEYCOUNT, (short) 0);
        b.putInt(PageLayout.OFF_NEXT, -1);
        fm.writePage(pid, b);
        return new LeafPage(fm, pid, b);
    }

    public int pageId() { return pageId; }
    public int keyCount() { return Short.toUnsignedInt(buf.getShort(PageLayout.OFF_KEYCOUNT)); }
    public int capacity() { return PageLayout.leafCapacity(); }
    public boolean isFull() { return keyCount() >= capacity(); }
    public void setNext(int nextPid) { buf.putInt(PageLayout.OFF_NEXT, nextPid); }

    // Append one (key, RID) — bulk loader feeds these in sorted order
    public void add(float key, HeapFile.RecordId rid) {
        int i = keyCount();
        int off = PageLayout.HEADER_BYTES + i * PageLayout.LEAF_ENTRY_BYTES;
        buf.putFloat(off, key);
        buf.putInt(off + 4, rid.pageId);
        buf.putInt(off + 8, rid.slot);
        buf.putShort(PageLayout.OFF_KEYCOUNT, (short) (i + 1));
    }

    public void flush() throws IOException { fm.writePage(pageId, buf); }
}

