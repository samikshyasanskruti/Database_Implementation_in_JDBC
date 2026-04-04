package comm.dbms.lab;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class Lab2_task1 {

/**
 * lab2_task1 - Represents a fixed-size database page with header metadata
 * Page Size: 512 bytes
 * Header: 12 bytes (numSlots, freeStart, slotDirEnd)
 * Slot Directory: 8 bytes per slot (offset + length)
 */
    // Page configuration
    public static final int PAGE_SIZE = 1024;
    private static final int HEADER_SIZE = 24;
    private static final int SLOT_SIZE = 24;   // offset(4) + length(4)

    // Page identity and data
    private final int pageId;
    private final byte[] data;

    // Header offsets (byte positions)
    private static final int OFF_NUM_SLOTS = 0;
    private static final int OFF_FREE_START = 4;
    private static final int OFF_SLOT_END = 8;

    /**
     * Constructor - Initialize a new database page
     * @param pageId Unique identifier for this page
     */
    public Lab2_task1(int pageId) {  //  FIXED: Constructor name matches class name
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        
        // Initialize header
        setInt(OFF_NUM_SLOTS, 0);
        setInt(OFF_FREE_START, HEADER_SIZE);
        setInt(OFF_SLOT_END, HEADER_SIZE);
    }

    /**
     * Write a 4-byte integer at specified position (big-endian)
     * @param pos Byte position in the data array
     * @param val Integer value to write
     */
    private void setInt(int pos, int val) {
        ByteBuffer.wrap(data, pos, 4)
                  .order(ByteOrder.BIG_ENDIAN)
                  .putInt(val);
    }

    /**
     * Read a 4-byte integer from specified position (big-endian)
     * @param pos Byte position in the data array
     * @return Integer value read from the data array
     */
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

    // ==================== Utility Methods ====================

    /**
     * Check if page has enough free space for a record
     * @param recordSize Size of the record to insert
     * @return true if space is available
     */
    public boolean hasSpace(int recordSize) {
        return getFreeSpaceBytes() >= (recordSize + SLOT_SIZE);
    }

    /**
     * Get total usable space (excluding header)
     * @return Available bytes for data
     */
    public int getUsableSpace() {
        return PAGE_SIZE - HEADER_SIZE;
    }

    /**
     * Display page information (for debugging)
     */
    public void printPageInfo() {
        System.out.println("=== Page " + pageId + " Info ===");
        System.out.println("Page Size: " + PAGE_SIZE + " bytes");
        System.out.println("Num Slots: " + getNumSlots());
        System.out.println("Free Space Start: " + getFreeSpaceStart());
        System.out.println("Slot Dir End: " + getSlotDirEnd());
        System.out.println("Free Space Bytes: " + getFreeSpaceBytes());
        System.out.println("==============================");
    }

    // ==================== Test Main Method ====================

    public static void main(String[] args) {
        // Create a new page  FIXED: Use lab2_task1 instead of DatabasePage
        Lab2_task1 page = new Lab2_task1(1);
        
        // Display page information
        page.printPageInfo();
        
        // Test helper methods
        System.out.println("\nHas space for 100 bytes? " + page.hasSpace(100));
        System.out.println("Usable Space: " + page.getUsableSpace() + " bytes");
    }
}