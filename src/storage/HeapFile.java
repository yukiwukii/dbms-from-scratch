package storage;

import IO.FileManager;
import Util.FixedRecordSize;
import model.GameRecord;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HeapFile {
    public static final int HEADER_BYTES = 4; // usedCount at offset 0

    private final FileManager fm;
    private final int recordsPerPage;

    public static final class RecordId {
        public final int pageId;
        public final int slot; // 0-based
        public RecordId(int pageId, int slot) { this.pageId = pageId; this.slot = slot; }
        @Override public String toString() { return "(" + pageId + "," + slot + ")"; }
    }

    public HeapFile(FileManager fm) {
        this.fm = fm;
        this.recordsPerPage = (FileManager.PAGE_SIZE - HEADER_BYTES) / FixedRecordSize.RECORD_SIZE;
    }

    public int getRecordsPerPage() { return recordsPerPage; }

    public RecordId append(GameRecord r) throws IOException {
        int totalPages = (int) fm.pageCount();
        if (totalPages == 0) {
            fm.allocatePage();     // superblock page 0
            totalPages = 1;
        }

        // ensure we have at least one data page
        int pageId = totalPages - 1;
        if (pageId == 0) {
            pageId = fm.allocatePage();
        }

        ByteBuffer page = fm.readPage(pageId);
        int used = page.getInt(0);
        if (used >= recordsPerPage) {
            pageId = fm.allocatePage();
            page = fm.readPage(pageId);
            used = 0;
            page.putInt(0, 0);
        }

        int offset = HEADER_BYTES + used * FixedRecordSize.RECORD_SIZE;
        page.position(offset);
        FixedRecordSize.write(r, page);
        page.putInt(0, used + 1);
        fm.writePage(pageId, page);
        return new RecordId(pageId, used);
    }

    public List<RecordWithId> scanAll() throws IOException {
        List<RecordWithId> out = new ArrayList<>();
        int rpb = this.recordsPerPage; // max records per heap page
        long pages = fm.pageCount();

        for (int pid = 1; pid < pages; pid++) {
            ByteBuffer page = fm.readPage(pid);
            int used = page.getInt(0);

            if (used < 0 || used > rpb) break;

            for (int s = 0; s < used; s++) {
                int offset = HEADER_BYTES + s * FixedRecordSize.RECORD_SIZE;

                // extra safety: don't read past page end
                if (offset + FixedRecordSize.RECORD_SIZE > FileManager.PAGE_SIZE) break;

                page.position(offset);
                GameRecord r = FixedRecordSize.read(page);
                out.add(new RecordWithId(r, new RecordId(pid, s)));
            }
        }
        return out;
    }

    public static final class RecordWithId {
        public final GameRecord rec;
        public final RecordId rid;
        public RecordWithId(GameRecord rec, RecordId rid) { this.rec = rec; this.rid = rid; }
    }

    public static final class Appender implements AutoCloseable {
        private final IO.FileManager fm;
        private final int recordsPerPage;
        private int pageId = -1;
        private java.nio.ByteBuffer page;
        private int used;

        public Appender(IO.FileManager fm) throws java.io.IOException {
            this.fm = fm;
            this.recordsPerPage = (IO.FileManager.PAGE_SIZE - HEADER_BYTES) / Util.FixedRecordSize.RECORD_SIZE;

            // ensure superblock
            if (fm.pageCount() == 0) fm.allocatePage();

            // open last page if exists, else create new data page
            int last = (int) fm.pageCount() - 1;
            if (last <= 0) {
                pageId = fm.allocatePage();
                page = java.nio.ByteBuffer.allocate(IO.FileManager.PAGE_SIZE);
                used = 0;
                page.putInt(0, 0);
            } else {
                pageId = last;
                page = fm.readPage(pageId);      // one read total
                used = page.getInt(0);
                if (used < 0 || used > recordsPerPage) { // uninitialized page safeguard
                    used = 0;
                    page.clear();
                    page.putInt(0, 0);
                }
            }
        }

        public RecordId add(model.GameRecord r) throws java.io.IOException {
            if (used >= recordsPerPage) {
                fm.writePage(pageId, page);      // one write per full page
                pageId = fm.allocatePage();
                page = java.nio.ByteBuffer.allocate(IO.FileManager.PAGE_SIZE);
                used = 0;
                page.putInt(0, 0);
            }
            int offset = HEADER_BYTES + used * Util.FixedRecordSize.RECORD_SIZE;
            page.position(offset);
            Util.FixedRecordSize.write(r, page);
            page.putInt(0, ++used);
            return new RecordId(pageId, used - 1);
        }

        @Override public void close() throws java.io.IOException {
            if (page != null) fm.writePage(pageId, page); // final flush once
        }
    }

}

