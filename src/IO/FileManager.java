package IO;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileManager implements AutoCloseable {
    public static final int PAGE_SIZE = 4096;

    private final RandomAccessFile raf;
    private final FileChannel ch;

    // Default: do NOT reset
    public FileManager(String path) throws IOException {
        this(path, false);
    }

    // New: optional reset (truncate to empty)
    public FileManager(String path, boolean reset) throws IOException {
        this.raf = new RandomAccessFile(path, "rw");
        this.ch = raf.getChannel();

        if (reset) {
            ch.truncate(0); // start fresh
        }
        if (ch.size() == 0) {
            // (re)create superblock at page 0
            ch.write(ByteBuffer.allocate(PAGE_SIZE), 0);
        }
    }

    public long pageCount() throws IOException {
        long size = ch.size();
        return size == 0 ? 0 : size / PAGE_SIZE;
    }

    public int allocatePage() throws IOException {
        int newPageId = (int) pageCount();
        ch.write(ByteBuffer.allocate(PAGE_SIZE), (long) newPageId * PAGE_SIZE);
        return newPageId;
    }

    public ByteBuffer readPage(int pageId) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(PAGE_SIZE);
        ch.read(buf, (long) pageId * PAGE_SIZE);
        buf.flip();
        return buf;
    }

    public void writePage(int pageId, ByteBuffer buf) throws IOException {
        buf.rewind();
        ch.write(buf, (long) pageId * PAGE_SIZE);
    }

    @Override public void close() throws IOException {
        ch.close();
        raf.close();
    }

    public void truncateToPages(long pageCount) throws IOException {
        ch.truncate(pageCount * (long) PAGE_SIZE);
    }

}


