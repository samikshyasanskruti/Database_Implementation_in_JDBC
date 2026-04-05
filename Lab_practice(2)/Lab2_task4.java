package comm.dbms.lab;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * alb3_task4 - Database page with tombstone-based record deletion (Task 4)
 * Implements: deleteRecord(slotNum) using soft-delete pattern
 * 
 * Page Size: 512 bytes | Header: 12 bytes | Slot: 8 bytes (offset + length)
 * 
 * Tombstone Pattern: offset=-1, length=0 marks a deleted slot
 * Benefits: RID stability, O(1) deletion, no immediate compaction
 */
public class Lab2_task4 {

    // ==================== Page Configuration ====================
    public static final int PAGE_SIZE = 512;
    private static final int HEADER_SIZE = 12;
    public static final int SLOT_SIZE = 8;   // offset(4) + length(4)

    // ==================== Page State ====================
    private final int pageId;
    private final byte[] data;

    // ==================== Header Offsets ====================
    private static final int OFF_NUM_SLOTS = 0;
    private static final int OFF_FREE_START = 4;
    private static final int OFF_SLOT_END = 8;

    // ==================== Constructor ====================
    public Lab2_task4(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        
        // Initialize header
        setInt(OFF_NUM_SLOTS, 0);
        setInt(OFF_FREE_START, HEADER_SIZE);      // Records start after header (12)
        setInt(OFF_SLOT_END, PAGE_SIZE);          // Slots start from END (512) - grows backward
    }

    // ==================== Helper: Byte-Level I/O ====================
    private void setInt(int pos, int val) {
        ByteBuffer.wrap(data, pos, 4)
                  .order(ByteOrder.BIG_ENDIAN)
                  .putInt(val);
    }

    public int getInt(int pos) {  //  Public for testing
        return ByteBuffer.wrap(data, pos, 4)
                         .order(ByteOrder.BIG_ENDIAN)
                         .getInt();
    }

    // ==================== Getter Methods ====================
    public int getPageId() { return pageId; }
    public int getNumSlots() { return getInt(OFF_NUM_SLOTS); }
    public int getFreeSpaceStart() { return getInt(OFF_FREE_START); }
    public int getSlotDirEnd() { return getInt(OFF_SLOT_END); }
    public int getFreeSpaceBytes() { return getSlotDirEnd() - getFreeSpaceStart(); }
    public byte[] getData() { return data; }

    // ==================== Task 2: Insert Record ====================
    public int insertRecord(byte[] record) {
        if (record == null || record.length == 0) {
            System.out.println("Error: Cannot insert null or empty record");
            return -1;
        }

        int recLen = record.length;
        int needed = recLen + SLOT_SIZE;

        if (getFreeSpaceBytes() < needed) {
            System.out.println("Page full (needed " + needed + ", free " + getFreeSpaceBytes() + ")");
            return -1;
        }

        int recOffset = getFreeSpaceStart();
        System.arraycopy(record, 0, data, recOffset, recLen);

        int slotNum = getNumSlots();
        int slotPos = getSlotDirEnd() - SLOT_SIZE;  //  Backward growth

        setInt(slotPos, recOffset);
        setInt(slotPos + 4, recLen);

        setInt(OFF_NUM_SLOTS, slotNum + 1);
        setInt(OFF_FREE_START, recOffset + recLen);
        setInt(OFF_SLOT_END, slotPos);

        System.out.println("Inserted slot " + slotNum + " | offset=" + recOffset + " len=" + recLen);
        return slotNum;
    }

    // ==================== Task 3: Retrieve Record ====================
    public byte[] getRecord(int slotNum) {
        if (slotNum < 0 || slotNum >= getNumSlots()) {
            System.out.println("Error: Invalid slot number " + slotNum);
            return null;
        }

        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;  //  Backward formula
        int offset = getInt(slotPos);
        int length = getInt(slotPos + 4);

        //  Check tombstone (deleted record)
        if (offset == -1 && length == 0) {
            return null;  // Soft-deleted
        }

        if (offset < HEADER_SIZE || offset + length > getSlotDirEnd()) {
            System.out.println("Error: Invalid record bounds");
            return null;
        }

        byte[] rec = new byte[length];
        System.arraycopy(data, offset, rec, 0, length);
        return rec;
    }

    // ==================== Task 4: Delete Record (Tombstone) ====================
    /**
     * Delete a record by marking its slot as a tombstone (soft delete)
     * @param slotNum The slot number to delete (0-based)
     * @return true if deletion succeeded, false if slot was invalid or already deleted
     */
    public boolean deleteRecord(int slotNum) {
        // Validate slot number bounds
        if (slotNum < 0 || slotNum >= getNumSlots()) {
            System.out.println("Error: Invalid slot number " + slotNum + 
                             " (valid: 0 to " + (getNumSlots() - 1) + ")");
            return false;
        }

        //  Calculate slot position (slots grow BACKWARD from page end)
        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;

        //  Read current slot values
        int currentOffset = getInt(slotPos);
        int currentLength = getInt(slotPos + 4);

        //  Check if already deleted (tombstone pattern)
        if (currentOffset == -1 && currentLength == 0) {
            System.out.println("Warning: Slot " + slotNum + " is already deleted (tombstone)");
            return false;  // Already deleted - avoid redundant operation
        }

        // === Apply Tombstone Pattern ===
        // Mark slot as deleted: offset=-1, length=0
        // This preserves the slot number (RID stability) while marking content invalid
        setInt(slotPos, -1);        // Invalid offset signals deletion
        setInt(slotPos + 4, 0);     // Zero length confirms tombstone

        //  Note: We do NOT reclaim space here (no compaction)
        // Space will be reused when new inserts fit in the free gap
        // This keeps deletion O(1) and maintains RID stability

        System.out.println("Deleted slot " + slotNum + " (tombstone: offset=-1, len=0)");
        return true;
    }

    // ==================== Helper: Check if Slot is Deleted ====================
    /**
     * Check if a slot contains a deleted record (tombstone)
     * @param slotNum The slot number to check
     * @return true if the slot is marked as deleted
     */
    public boolean isDeleted(int slotNum) {
        if (slotNum < 0 || slotNum >= getNumSlots()) return true;
        
        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;
        int offset = getInt(slotPos);
        int length = getInt(slotPos + 4);
        
        return (offset == -1 && length == 0);
    }

    // ==================== Debug: Print Page Info ====================
    public void printPageInfo() {
        System.out.println("\n=== Page " + pageId + " State ===");
        System.out.println("Num Slots     : " + getNumSlots());
        System.out.println("Free Start    : " + getFreeSpaceStart() + " (records ↑)");
        System.out.println("Slot Dir End  : " + getSlotDirEnd() + " (slots ↓)");
        System.out.println("Free Bytes    : " + getFreeSpaceBytes());
        System.out.println("Tombstones    : " + countTombstones());
        
        System.out.println("\nSlot Directory:");
        for (int i = 0; i < getNumSlots(); i++) {
            if (isDeleted(i)) {
                System.out.println("  Slot " + i + ":  [DELETED]");
            } else {
                byte[] rec = getRecord(i);
                if (rec != null) {
                    System.out.println("  Slot " + i + ":  \"" + new String(rec) + "\"");
                }
            }
        }
        System.out.println("=======================\n");
    }

    // ==================== Helper: Count Tombstones ====================
    private int countTombstones() {
        int count = 0;
        for (int i = 0; i < getNumSlots(); i++) {
            if (isDeleted(i)) count++;
        }
        return count;
    }

    // ====================  Main Method: Test Task 4 ====================
    public static void main(String[] args) {
        System.out.println(" Testing alb3_task4: Tombstone Deletion (Task 4)\n");
        
        Lab2_task4 page = new Lab2_task4(1);
        
        // --- Setup: Insert Records ---
        System.out.println(" Step 1: Insert Test Records");
        page.insertRecord("CS".getBytes());           // Slot 0
        page.insertRecord("Math".getBytes());         // Slot 1  
        page.insertRecord("Physics".getBytes());      // Slot 2
        System.out.println();
        
        // --- Verify Initial State ---
        System.out.println(" Step 2: Verify All Records retrievable");
        for (int i = 0; i < page.getNumSlots(); i++) {
            byte[] rec = page.getRecord(i);
            System.out.println("   Slot " + i + ": " + 
                (rec != null ? "\"" + new String(rec) + "\"" : "null"));
        }
        System.out.println();
        
        // --- Task 4: Delete Record ---
        System.out.println(" Step 3: Delete Slot 1 (Math)");
        boolean result1 = page.deleteRecord(1);
        System.out.println("   Delete result: " + (result1 ? " Success" : " Failed"));
        System.out.println();
        
        // --- Verify Deletion ---
        System.out.println(" Step 4: Verify Slot 1 is Deleted");
        byte[] deletedRec = page.getRecord(1);
        System.out.println("   Get slot 1: " + (deletedRec != null ? new String(deletedRec) : "null "));
        System.out.println("   isDeleted(1): " + page.isDeleted(1) );
        System.out.println();
        
        // --- Test Double-Delete ---
        System.out.println("Step 5: Test Double-Delete Protection");
        boolean result2 = page.deleteRecord(1);  // Try delete again
        System.out.println("   Second delete result: " + (result2 ? " Should fail" : " Correctly rejected"));
        System.out.println();
        
        // --- Verify Other Slots Still Work ---
        System.out.println(" Step 6: Verify Other Slots Unaffected");
        System.out.println("   Slot 0: \"" + new String(page.getRecord(0)) + "\" ");
        System.out.println("   Slot 2: \"" + new String(page.getRecord(2)) + "\" ");
        System.out.println();
        
        // --- Insert After Delete (Fragmentation Demo) ---
        System.out.println("Step 7: Insert After Delete (Shows Fragmentation)");
        System.out.println("   Free space before: " + page.getFreeSpaceBytes() + " bytes");
        page.insertRecord("Chem".getBytes());  // Slot 3
        System.out.println("   Free space after: " + page.getFreeSpaceBytes() + " bytes");
        System.out.println("   Note: Tombstone space NOT reclaimed (no compaction)");
        System.out.println();
        
        // --- Final State ---
        System.out.println(" Step 8: Final Page State");
        page.printPageInfo();
        
        // --- Summary ---
        System.out.println(" Task 4 Demo Complete!");
        System.out.println(" Summary:");
        System.out.println("   • Total slots: " + page.getNumSlots());
        System.out.println("   • Active records: " + (page.getNumSlots() - page.countTombstones()));
        System.out.println("   • Tombstones: " + page.countTombstones());
        System.out.println("   • RID stability: Slot numbers never change after delete ");
    }
}
