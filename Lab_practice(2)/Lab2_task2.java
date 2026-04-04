package comm.dbms.lab;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Lab2_task2 {
/**
 * Lab2_Task2 - Database page with variable-length record insertion support
 * Page Size: 512 bytes | Header: 12 bytes | Slot: 8 bytes (offset + length)
 */

    // ==================== Page Configuration ====================
    public static final int PAGE_SIZE = 1024;
    private static final int HEADER_SIZE = 24;
    public static final int SLOT_SIZE = 24;   // offset(4) + length(4)

    // ==================== Page State ====================
    private final int pageId;
    private final byte[] data;

    // ==================== Header Offsets ====================
    private static final int OFF_NUM_SLOTS = 0;
    private static final int OFF_FREE_START = 4;
    private static final int OFF_SLOT_END = 8;

    // ==================== Constructor ====================
    /**
     * Initialize a new database page
     * @param pageId Unique identifier for this page
     */
    public Lab2_task2(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        
        // Initialize header
        setInt(OFF_NUM_SLOTS, 0);
        setInt(OFF_FREE_START, HEADER_SIZE);
        setInt(OFF_SLOT_END, PAGE_SIZE);
    }

    // ==================== Helper: Byte-Level I/O ====================
    private void setInt(int pos, int val) {
        ByteBuffer.wrap(data, pos, 4)
                  .order(ByteOrder.BIG_ENDIAN)
                  .putInt(val);
    }

    private int getInt(int pos) {
        return ByteBuffer.wrap(data, pos, 4)
                         .order(ByteOrder.BIG_ENDIAN)
                         .getInt();
    }

    // ==================== Getter Methods ====================
    public int getPageId() {
        return pageId;
    }

    public int getNumSlots() {
        return getInt(OFF_NUM_SLOTS);
    }

    public int getFreeSpaceStart() {
        return getInt(OFF_FREE_START);
    }

    public int getSlotDirEnd() {
        return getInt(OFF_SLOT_END);
    }

    public int getFreeSpaceBytes() {
        return getSlotDirEnd() - getFreeSpaceStart();
    }

    public byte[] getData() {
        return data;
    }

    // ==================== Insert Record ====================
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
        int slotPos = getSlotDirEnd() - SLOT_SIZE;

        setInt(slotPos, recOffset);
        setInt(slotPos + 4, recLen);

        setInt(OFF_NUM_SLOTS, slotNum + 1);
        setInt(OFF_FREE_START, recOffset + recLen);
        setInt(OFF_SLOT_END, slotPos);

        System.out.println("Inserted slot " + slotNum + " | offset=" + recOffset + " len=" + recLen);
        return slotNum;
    }

    // ==================== Utility ====================
    public void printPageInfo() {
        System.out.println("\n=== Page " + pageId + " Info ===");
        System.out.println("Page Size     : " + PAGE_SIZE + " bytes");
        System.out.println("Num Slots     : " + getNumSlots());
        System.out.println("Free Start    : " + getFreeSpaceStart());
        System.out.println("Slot Dir End  : " + getSlotDirEnd());
        System.out.println("Free Bytes    : " + getFreeSpaceBytes());
        System.out.println("==============================\n");
    }

    // ==================== Test Main Method ====================
    public static void main(String[] args) {

        Lab2_task2 page = new Lab2_task2(1);
        page.printPageInfo();

        // Insert 10 subject records
        page.insertRecord("A".getBytes());
        page.insertRecord("B".getBytes());
        page.insertRecord("C".getBytes());
        page.insertRecord("D".getBytes());
        page.insertRecord("E".getBytes());
        page.insertRecord("F".getBytes());
        page.insertRecord("G".getBytes());
        page.insertRecord("H".getBytes());
        page.insertRecord("I".getBytes());
        page.insertRecord("J".getBytes());

        page.printPageInfo();

        // Verify inserted records
        System.out.println("Inserted Records:");

        for (int i = 0; i < page.getNumSlots(); i++) {
            int slotPos = PAGE_SIZE - (i + 1) * SLOT_SIZE;
            int offset = page.getInt(slotPos);
            int length = page.getInt(slotPos + 4);

            byte[] rec = new byte[length];
            System.arraycopy(page.getData(), offset, rec, 0, length);

            System.out.println("Slot " + i + ": " + new String(rec));
        }
    }
}
