package storage;
import model.GameRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Disk {
    // constants
    private final int diskSize;
    private final int blockSize;
    private final int recordSize;

    private Block[] blocks;
    private int numOfGameRecords; // total number of records
    private int diskAccessCount;

    /* constructor to instantiate disk*/
    public Disk(int diskSize, int blockSize, int recordSize) {
        this.numOfGameRecords = 0; // initialised to 0
        this.diskSize = diskSize;
        this.blockSize = blockSize;
        this.recordSize = recordSize;
        this.blocks = new Block[diskSize / blockSize];
    }

    public int getDiskSize() {
        return diskSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public int getNumOfGameRecords() {
        return numOfGameRecords;
    }

    public int getDiskAccessCount() {
        return diskAccessCount;
    }

    public int getNumOfBlocks() {
        return blocks.length;
    }

    public int getNumOfOccupiedBlocks() {
        int count = 0;
        for (Block b : blocks) {
            if (b != null && !b.isBlockEmpty()) count++;
        }
        return count;
    }

    public int getMaxNumRecordsPerBlock() {
        return blockSize / recordSize; // will take floor function
    }

    public Block getBlock(int index) {
        if (isInvalidBlockIndex(index)) {
            System.err.println("Block not found");
            return null;
        }
        return blocks[index];
    }

    // get all records flattened into list
    public List<GameRecord> getGameRecords() {
        List<GameRecord> gameRecords = new ArrayList<>();
        for (Block b: blocks) {
            // if block is non-null, add the records
            if (b!= null) {
                gameRecords.addAll(Arrays.asList(b.getBlockGameRecords()));
            }
        }
        return gameRecords;
    }

    // insert record into disk block
    public boolean insertRecord(GameRecord r) {
        int blockIndex = numOfGameRecords / getMaxNumRecordsPerBlock();
        // check if block index valid or not
        if (isInvalidBlockIndex(blockIndex)) {
            System.err.println("Cannot find block, game record cannot be inserted.");
            return false;
        }

        // if block to insert record is null, create new block obj
        if (blocks[blockIndex] == null) {
            blocks[blockIndex] = new Block(getMaxNumRecordsPerBlock());
        }

        incrementDiskAccessCount();
        boolean inserted; // flag to check successful insertion
        inserted = blocks[blockIndex].insertGameRecord(r);
        if (inserted) {
            // update total number of records
            numOfGameRecords++;
        }
        return inserted;
    }

    // delete record using block index and record index
    public boolean deleteRecord(int blockIndex, int recordIndex) {
        if (isInvalidBlockIndex(blockIndex)) {
            System.err.println("Cannot find block, so cannot delete record.");
            return false;
        }
        incrementDiskAccessCount();
        boolean deleted; // flag to check successful deletion
        deleted = blocks[blockIndex].deleteGameRecord(recordIndex);
        if (deleted) numOfGameRecords--; // decrement total number of records
        return deleted;
    }

    public GameRecord getRecord(int blockIndex, int recordIndex) {
        if (isInvalidBlockIndex(blockIndex)) {
            System.err.println("Cannot find block.");
            return null;
        }
        else if (blocks[blockIndex] == null) {
            System.err.println("Cannot find records in the block.");
            return null;
        }
        else {
            incrementDiskAccessCount();
            return blocks[blockIndex].getGameRecord(recordIndex);
        }
    }

    protected boolean isInvalidBlockIndex(int blockIndex) {
        return blockIndex < 0 || blockIndex >= getNumOfBlocks();
    }

    private void incrementDiskAccessCount() {
        // reset access count before any operation
        diskAccessCount = 0;
        diskAccessCount++;
    }

    // print disk information for reporting stats for Task 1
    public void printDiskInformation() {
        System.out.println("Record size: " + getRecordSize());
        System.out.println("Number of records: " + getNumOfGameRecords());
        System.out.println("Number of records in a block: " + getMaxNumRecordsPerBlock());
        System.out.println("Number of blocks used to store data: " + getNumOfOccupiedBlocks());
    }

    public void printDiskContents() {
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] != null && !blocks[i].isBlockEmpty()) {
                System.out.println("Block " + i + ":");
                blocks[i].printBlockInformation();
            }
        }
    }
}
