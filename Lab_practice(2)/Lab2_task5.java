package comm.dbms.lab;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * lab2_task5 - Database page with debug visualization (Task 5)
 * Implements: printPage() for detailed internal state inspection
 * 
 * Page Size: 512 bytes | Header: 12 bytes | Slot: 8 bytes (offset + length)
 * 
 * Features:
 * • Header metadata display
 * • Slot directory with raw values
 * • Record content with printable conversion
 * • ASCII memory layout diagram
 * • Tombstone/invalid slot detection
 */
public class Lab2_task5 {

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
    public Lab2_task5(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];
        
        // Initialize header
        setInt(OFF_NUM_SLOTS, 0);
        setInt(OFF_FREE_START, HEADER_SIZE);      // Records start after header (12)
        setInt(OFF_SLOT_END, PAGE_SIZE);          //  Slots start from END (512) - grows backward
    }

    // ==================== Helper: Byte-Level I/O ====================
    private void setInt(int pos, int val) {
        ByteBuffer.wrap(data, pos, 4)
                  .order(ByteOrder.BIG_ENDIAN)
                  .putInt(val);
    }

    public int getInt(int pos) {  //  Public for testing/debugging
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

        return slotNum;
    }

    // ==================== Task 3: Retrieve Record ====================
    public byte[] getRecord(int slotNum) {
        if (slotNum < 0 || slotNum >= getNumSlots()) return null;

        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;
        int offset = getInt(slotPos);
        int length = getInt(slotPos + 4);

        // Check tombstone
        if (offset == -1 && length == 0) return null;
        if (offset < HEADER_SIZE || offset + length > getSlotDirEnd()) return null;

        byte[] rec = new byte[length];
        System.arraycopy(data, offset, rec, 0, length);
        return rec;
    }

    // ==================== Task 4: Delete Record (Tombstone) ====================
    public boolean deleteRecord(int slotNum) {
        if (slotNum < 0 || slotNum >= getNumSlots()) return false;

        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;
        int currentOffset = getInt(slotPos);
        int currentLength = getInt(slotPos + 4);

        // Already deleted?
        if (currentOffset == -1 && currentLength == 0) return false;

        // Apply tombstone
        setInt(slotPos, -1);
        setInt(slotPos + 4, 0);
        return true;
    }

    // ==================== Helper: Check if Deleted ====================
    public boolean isDeleted(int slotNum) {
        if (slotNum < 0 || slotNum >= getNumSlots()) return true;
        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;
        return getInt(slotPos) == -1 && getInt(slotPos + 4) == 0;
    }

    // ====================  Task 5: Print Page State (Debug Visualization) ====================
    /**
     * Print detailed page state for debugging and verification
     * Shows: header metadata, slot directory entries, record contents, memory layout
     */
    public void printPage() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" PAGE " + pageId + " - DEBUG STATE");
        System.out.println("═".repeat(60));
        
        // ─── Header Metadata ───
        System.out.println(" HEADER METADATA:");
        System.out.printf("   %-16s : %d bytes%n", "Page Size", PAGE_SIZE);
        System.out.printf("   %-16s : %d bytes%n", "Header Size", HEADER_SIZE);
        System.out.printf("   %-16s : %d%n", "Num Slots", getNumSlots());
        System.out.printf("   %-16s : %d (records grow ↑)%n", "Free Start", getFreeSpaceStart());
        System.out.printf("   %-16s : %d (slots grow ↓)%n", "Slot Dir End", getSlotDirEnd());
        System.out.printf("   %-16s : %d bytes%n", "Free Bytes", getFreeSpaceBytes());
        System.out.printf("   %-16s : %d bytes%n", "Usable Space", PAGE_SIZE - HEADER_SIZE);
        
        // ─── Slot Directory Details ───
        System.out.println("\n  SLOT DIRECTORY (grows backward from byte " + PAGE_SIZE + "):");
        
        if (getNumSlots() == 0) {
            System.out.println("   (empty - no slots allocated)");
        } else {
            System.out.printf("   %-6s %-8s %-10s %-10s %-20s%n", 
                            "Slot#", "Pos[hex]", "Offset", "Length", "Content");
            System.out.println("   " + "─".repeat(54));
            
            for (int i = 0; i < getNumSlots(); i++) {
                //  Calculate slot position (backward growth formula)
                int slotPos = PAGE_SIZE - (i + 1) * SLOT_SIZE;
                int offset = getInt(slotPos);
                int length = getInt(slotPos + 4);
                
                // Determine status and content
                String status, content;
                if (offset == -1 && length == 0) {
                    status = "🗑";
                    content = "[TOMBSTONE]";
                } else if (offset < HEADER_SIZE || offset + length > PAGE_SIZE) {
                    status = "⚠️";
                    content = "[INVALID]";
                } else {
                    status = "✅";
                    byte[] rec = new byte[length];
                    System.arraycopy(data, offset, rec, 0, length);
                    content = "\"" + toPrintableString(rec) + "\"";
                }
                
                System.out.printf("   %-6d 0x%03X    %-10d %-10d %-20s %s%n", 
                                i, slotPos, offset, length, content, status);
            }
        }
        
        // ─── Memory Layout Diagram ───
        System.out.println("\n MEMORY LAYOUT VISUALIZATION:");
        printMemoryLayout();
        
        // ─── Quick Stats ───
        System.out.println("\n QUICK STATS:");
        int active = 0, tombstones = 0;
        for (int i = 0; i < getNumSlots(); i++) {
            if (isDeleted(i)) tombstones++;
            else if (getRecord(i) != null) active++;
        }
        System.out.printf("   • Active records : %d%n", active);
        System.out.printf("   • Tombstones     : %d%n", tombstones);
        System.out.printf("   • Fragmentation  : %.1f%%%n", 
                        tombstones > 0 ? (tombstones * 100.0 / getNumSlots()) : 0);
        
        System.out.println("═".repeat(60) + "\n");
    }

    /**
     * Helper: Convert byte array to printable string (handle binary/non-ASCII)
     */
    private String toPrintableString(byte[] bytes) {
        if (bytes == null) return "null";
        if (bytes.length == 0) return "(empty)";
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            char c = (char) (b & 0xFF);
            // Show printable ASCII, escape others as hex
            if (c >= 32 && c < 127) {
                sb.append(c);
            } else {
                sb.append(String.format("\\x%02X", b & 0xFF));
            }
            // Truncate long records for display
            if (sb.length() > 30) {
                sb.append("...");
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Helper: Print ASCII art visualization of page memory layout
     */
    private void printMemoryLayout() {
        int freeStart = getFreeSpaceStart();
        int slotEnd = getSlotDirEnd();
        
        // Calculate section sizes
        int headerSize = HEADER_SIZE;
        int recordsSize = Math.max(0, freeStart - HEADER_SIZE);
        int freeSize = Math.max(0, slotEnd - freeStart);
        int slotsSize = Math.max(0, PAGE_SIZE - slotEnd);
        
        // Print scale bar
        System.out.printf("   0         %-8d %-8d %-8d %d%n", 
                        HEADER_SIZE, freeStart, slotEnd, PAGE_SIZE);
        System.out.println("   │---------│---------│----------│---------│");
        
        // Print section labels
        System.out.printf("   │ %-7s │ %-7s │ %-8s │ %-7s │%n", 
                        "HEADER", "RECORDS", "FREE", "SLOTS");
        System.out.printf("   │ %-7d │ %-7s │ %-8s │ %-7s │%n", 
                        headerSize, "↑grow", "space", "↓grow");
        
        // Print visual bar (40 chars wide proportional)
        int total = PAGE_SIZE;
        int headerBar = headerSize * 40 / total;
        int recordsBar = recordsSize * 40 / total;
        int freeBar = freeSize * 40 / total;
        int slotsBar = slotsSize * 40 / total;
        
        // Ensure at least 1 char per section if it exists
        if (headerSize > 0 && headerBar == 0) headerBar = 1;
        if (recordsSize > 0 && recordsBar == 0) recordsBar = 1;
        if (freeSize > 0 && freeBar == 0) freeBar = 1;
        if (slotsSize > 0 && slotsBar == 0) slotsBar = 1;
        
        System.out.print("   ├" + "═".repeat(Math.max(0, headerBar)));
        System.out.print("├" + "█".repeat(Math.max(0, recordsBar)));
        System.out.print("├" + "░".repeat(Math.max(0, freeBar)));
        System.out.print("├" + "▓".repeat(Math.max(0, slotsBar)));
        System.out.println("┤");
        
        // Legend
        System.out.println("   Legend: ═Header █Records ░Free ▓Slots");
    }

    // ====================  Main Method: Comprehensive Demo ====================
    public static void main(String[] args) {
        System.out.println(" lab2_task5: Page Debug Visualization (Task 5)");
        System.out.println("Testing: Insert → Retrieve → Delete → PrintPage\n");
        
        Lab2_task5 page = new Lab2_task5(1);
        
        // ─── 1. Initial Empty State ───
        System.out.println("🔹 [1] Initial Empty Page:");
        page.printPage();
        
        // ─── 2. Insert Records ───
        System.out.println("🔹 [2] Inserting Records:");
        page.insertRecord("CS".getBytes());
        page.insertRecord("Mathematics".getBytes());
        page.insertRecord("Physics".getBytes());
        page.insertRecord("Chemistry".getBytes());
        page.insertRecord("IM".getBytes());
        page.insertRecord("DL".getBytes());
        page.insertRecord("CNS".getBytes());
        page.insertRecord("DBMS".getBytes());
        page.insertRecord("UOSD".getBytes());
        page.insertRecord("DIIJ".getBytes());
        page.printPage();
        
        // ─── 3. Retrieve & Verify ───
        System.out.println("🔹 [3] Retrieving Records:");
        for (int i = 0; i < page.getNumSlots(); i++) {
            byte[] rec = page.getRecord(i);
            System.out.printf("   Slot %d: %s%n", i, 
                rec != null ? "\"" + new String(rec) + "\"" : "null");
        }
        System.out.println();
        
        // ─── 4. Delete Record (Tombstone) ───
        System.out.println("🔹 [4] Deleting Slot 7 (DBMS):");
        page.deleteRecord(7);
        page.printPage();
        
        // ─── 5. Insert After Delete (Fragmentation) ───
        System.out.println("🔹 [5] Insert After Delete (Fragmentation Demo):");
        System.out.printf("   Free space before: %d bytes%n", page.getFreeSpaceBytes());
        page.insertRecord("Bio".getBytes());
        System.out.printf("   Free space after: %d bytes%n", page.getFreeSpaceBytes());
        System.out.println("    Note: Tombstone space NOT reclaimed (no compaction)\n");
        page.printPage();
        
        // ─── 6. Compact Page ───
        System.out.println("[6] Compacting Page:");

        int freeBeforeCompact = page.getFreeSpaceBytes();

        page.compact();

        int freeAfterCompact = page.getFreeSpaceBytes();

        page.printPage();
        
        // ─── 7. Binary Data Test ───
        System.out.println("🔹 [7] Binary/Non-ASCII Data Test:");
        byte[] binary = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x00, (byte)0xFF, (byte)0xFE};
        page.insertRecord(binary);
        System.out.println("   Inserted binary record: " + toPrintableStringStatic(binary));
        page.printPage();
        
        // ─── 8. Edge Cases ───
        System.out.println("🔹 [8] Edge Case Tests:");
        System.out.println("   Get invalid slot 99: " + (page.getRecord(99) == null ? "null " : "ERROR"));
        System.out.println("   Delete invalid slot -1: " + (!page.deleteRecord(-1) ? "rejected " : "ERROR"));
        System.out.println("   Double-delete slot 1: " + (!page.deleteRecord(1) ? "rejected " : "ERROR"));
        
        // ─── Final Summary ───
        System.out.println("\n Task 5 Demo Complete!");
        System.out.println(" Final Summary:");
        System.out.printf("   • Page ID        : %d%n", page.getPageId());
        System.out.printf("   • Total slots    : %d%n", page.getNumSlots());
        System.out.printf("   • Free space     : %d bytes (%.1f%%)%n", 
                        page.getFreeSpaceBytes(), 
                        page.getFreeSpaceBytes() * 100.0 / PAGE_SIZE);
        System.out.printf("   • Slot directory : %d bytes used%n", PAGE_SIZE - page.getSlotDirEnd());
        System.out.printf("   • Free before compaction : %d bytes%n", freeBeforeCompact);
        System.out.printf("   • Free after compaction  : %d bytes%n", freeAfterCompact);

        System.out.println("   • Compaction performed successfully ");
    }
    
    public void compact() {
        try {

            int writePos = HEADER_SIZE;

            for (int i = 0; i < getNumSlots(); i++) {

                byte[] rec = getRecord(i);

                if (rec != null) {

                    int slotPos = PAGE_SIZE - (i + 1) * SLOT_SIZE;
                    int oldOffset = getInt(slotPos);

                    System.arraycopy(data, oldOffset, data, writePos, rec.length);

                    setInt(slotPos, writePos);

                    writePos += rec.length;
                }
            }

            setInt(OFF_FREE_START, writePos);

            System.out.println("Compacted → new free start: " + writePos);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Index is out of Bound Exceeding");
        }
    }
    // Static helper for main method (since toPrintableString is instance method)
    private static String toPrintableStringStatic(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            char c = (char) (b & 0xFF);
            if (c >= 32 && c < 127) sb.append(c);
            else sb.append(String.format("\\x%02X", b & 0xFF));
            if (sb.length() > 30) { sb.append("..."); break; }
        }
        return sb.toString();
    }
}
