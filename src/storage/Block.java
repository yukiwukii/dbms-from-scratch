package storage;
import model.GameRecord;

public class Block {
    private GameRecord[] blockGameRecords;
    private int numOfRecordsInBlock;

    public Block(int size) {
        // initialise number of records to be 0
        this.numOfRecordsInBlock = 0;
        // max capacity of records in a block
        this.blockGameRecords = new GameRecord[size];
    }

    public int getNumOfRecordsInBlock() {
        return numOfRecordsInBlock;
    }

    // returns all records
    public GameRecord[] getBlockGameRecords() {
        return blockGameRecords;
    }

    public boolean isBlockEmpty() {
        return numOfRecordsInBlock ==0;
    }

    public boolean isBlockFull() {
        return numOfRecordsInBlock >= blockGameRecords.length;
    }

    // function will be used for search, insertion and deletion of records
    protected boolean isInvalidIndex(int index) {
        return index < 0 || index >= numOfRecordsInBlock;
    }

    // returns specific record given index
    public GameRecord getGameRecord(int index) {
        // check index valid or not
        if (isInvalidIndex(index)) {
            System.err.println("Game record is not found.");
            return null;
        }
        return blockGameRecords[index];
    }

    // insert record sequentially
    public boolean insertGameRecord(GameRecord r) {
        // if there is still at least one available slot
        // insert record at latest index pos
        if (!isBlockFull()) {
            blockGameRecords[numOfRecordsInBlock] = r;
            numOfRecordsInBlock++;
            return true;
        }
        else {
            // block is full so cannot insert
            System.err.println("Block is full, cannot insert game record.");
            return false;
        }
    }

    // delete record using index
    public boolean deleteGameRecord(int index) {
        // if the index is invalid, fail to delete record
        if (isInvalidIndex(index)) {
            System.err.println("Game record is not found, cannot delete record.");
            return false;
        }
        // shifting behind records to left to fill gap of deleted record
        for (int i = index + 1; i < numOfRecordsInBlock; i++) {
            blockGameRecords[i-1] = blockGameRecords[i];
        }
        // set last index position obj as null
        blockGameRecords[numOfRecordsInBlock - 1] = null;
        numOfRecordsInBlock--; // one less record in block
        return true;
    }

    // printing records for debugging
    public void printBlockInformation() {
        System.out.println("Block contains " + numOfRecordsInBlock + " record(s):");
        for (int i = 0; i < numOfRecordsInBlock; i++) {
            System.out.printf("[%d] %s%n", i, blockGameRecords[i].toString());
        }
    }
}
