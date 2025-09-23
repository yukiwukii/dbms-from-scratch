package Bplustree;

import IO.FileManager;

import java.io.IOException;
import java.nio.ByteBuffer;

public class InternalPage {
    private final FileManager fm;
    private final int pageId;
    private final ByteBuffer buf;

    private InternalPage(FileManager fm, int pageId, ByteBuffer buf) {
        this.fm = fm; this.pageId = pageId; this.buf = buf;
    }

    public static InternalPage create(FileManager fm) throws IOException {
        int pid = fm.allocatePage();
        ByteBuffer b = ByteBuffer.allocate(FileManager.PAGE_SIZE);
        b.put(PageLayout.OFF_TYPE, PageLayout.TYPE_INTERNAL);
        b.putShort(PageLayout.OFF_KEYCOUNT, (short) 0);
        fm.writePage(pid, b);
        return new InternalPage(fm, pid, b);
    }

    public int pageId() { return pageId; }
    public int keyCount() { return Short.toUnsignedInt(buf.getShort(PageLayout.OFF_KEYCOUNT)); }
    public int capacity() { return PageLayout.internalCapacity(); }
    public boolean isFull() { return keyCount() >= capacity(); }

    public void setFirstChild(int childPid) {
        buf.putInt(PageLayout.HEADER_BYTES, childPid);
    }

    // Append (separatorKey, rightChildPid)
    public void append(float sepKey, int rightChildPid) {
        int i = keyCount();
        int off = PageLayout.HEADER_BYTES + PageLayout.INTERNAL_FIRST_CHILD_BYTES + i * PageLayout.INTERNAL_ENTRY_BYTES;
        buf.putFloat(off, sepKey);
        buf.putInt(off + 4, rightChildPid);
        buf.putShort(PageLayout.OFF_KEYCOUNT, (short) (i + 1));
    }

    public void flush() throws IOException { fm.writePage(pageId, buf); }
}

