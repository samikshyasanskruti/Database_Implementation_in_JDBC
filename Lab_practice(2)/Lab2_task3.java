package comm.dbms.lab;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * lab2_task3 - Database page with record retrieval support (Task 3)
 * Implements: getRecord(slotNum) for direct RID-based access
 * 
 * Page Size: 512 bytes | Header: 12 bytes | Slot: 8 bytes (offset + length)
 */
public class Lab2_task3 {

    public static final int PAGE_SIZE = 512;
    private static final int HEADER_SIZE = 12;
    public static final int SLOT_SIZE = 8;

    private final int pageId;
    private final byte[] data;

    private static final int OFF_NUM_SLOTS = 0;
    private static final int OFF_FREE_START = 4;
    private static final int OFF_SLOT_END = 8;

    public Lab2_task3(int pageId) {
        this.pageId = pageId;
        this.data = new byte[PAGE_SIZE];

        setInt(OFF_NUM_SLOTS, 0);
        setInt(OFF_FREE_START, HEADER_SIZE);
        setInt(OFF_SLOT_END, PAGE_SIZE);
    }

    private void setInt(int pos, int val) {
        ByteBuffer.wrap(data, pos, 4)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(val);
    }

    public int getInt(int pos) {
        return ByteBuffer.wrap(data, pos, 4)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
    }

    public int getPageId() { return pageId; }
    public int getNumSlots() { return getInt(OFF_NUM_SLOTS); }
    public int getFreeSpaceStart() { return getInt(OFF_FREE_START); }
    public int getSlotDirEnd() { return getInt(OFF_SLOT_END); }
    public int getFreeSpaceBytes() { return getSlotDirEnd() - getFreeSpaceStart(); }
    public byte[] getData() { return data; }

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

    public byte[] getRecord(int slotNum) {

        if (slotNum < 0 || slotNum >= getNumSlots()) {
            System.out.println("Error: Invalid slot number " + slotNum + " (valid: 0 to " + (getNumSlots()-1) + ")");
            return null;
        }

        int slotPos = PAGE_SIZE - (slotNum + 1) * SLOT_SIZE;

        int offset = getInt(slotPos);
        int length = getInt(slotPos + 4);

        if (offset == -1 && length == 0) {
            System.out.println("Warning: Slot " + slotNum + " is deleted (tombstone)");
            return null;
        }

        if (offset < HEADER_SIZE || offset + length > getSlotDirEnd()) {
            System.out.println("Error: Record bounds invalid (offset=" + offset + ", len=" + length + ")");
            return null;
        }

        byte[] rec = new byte[length];
        System.arraycopy(data, offset, rec, 0, length);
        return rec;
    }

    public static void main(String[] args) {

        System.out.println(" Testing lab2_task3: Record Retrieval (Task 3)\n");

        Lab2_task3 page = new Lab2_task3(1);

        System.out.println(" Task 2: Inserting Records");

        byte[] rec1 = "Apple".getBytes();
        byte[] rec2 = "Banana".getBytes();
        byte[] rec3 = "Cherry".getBytes();
        byte[] rec4 = "Mango".getBytes();
        byte[] rec5 = "Orange".getBytes();
        byte[] rec6 = "Grapes".getBytes();
        byte[] rec7 = "Papaya".getBytes();
        byte[] rec8 = "Guava".getBytes();
        byte[] rec9 = "Pineapple".getBytes();
        byte[] rec10 = "Watermelon".getBytes();

        int slot1 = page.insertRecord(rec1);
        int slot2 = page.insertRecord(rec2);
        int slot3 = page.insertRecord(rec3);
        int slot4 = page.insertRecord(rec4);
        int slot5 = page.insertRecord(rec5);
        int slot6 = page.insertRecord(rec6);
        int slot7 = page.insertRecord(rec7);
        int slot8 = page.insertRecord(rec8);
        int slot9 = page.insertRecord(rec9);
        int slot10 = page.insertRecord(rec10);

        System.out.println();
        System.out.println();

        System.out.println(" Task 3: Retrieving Records by Slot");

        byte[] retrieved1 = page.getRecord(0);
        byte[] retrieved2 = page.getRecord(1);
        byte[] retrieved3 = page.getRecord(2);
        byte[] retrieved4 = page.getRecord(3);
        byte[] retrieved5 = page.getRecord(4);
        byte[] retrieved6 = page.getRecord(5);
        byte[] retrieved7 = page.getRecord(6);
        byte[] retrieved8 = page.getRecord(7);
        byte[] retrieved9 = page.getRecord(8);
        byte[] retrieved10 = page.getRecord(9);

        System.out.println("Retrieved slot 0: " + (retrieved1 != null ? "\"" + new String(retrieved1) + "\"" : "null"));
        System.out.println("Retrieved slot 1: " + (retrieved2 != null ? "\"" + new String(retrieved2) + "\"" : "null"));
        System.out.println("Retrieved slot 2: " + (retrieved3 != null ? "\"" + new String(retrieved3) + "\"" : "null"));
        System.out.println("Retrieved slot 3: " + (retrieved4 != null ? "\"" + new String(retrieved4) + "\"" : "null"));
        System.out.println("Retrieved slot 4: " + (retrieved5 != null ? "\"" + new String(retrieved5) + "\"" : "null"));
        System.out.println("Retrieved slot 5: " + (retrieved6 != null ? "\"" + new String(retrieved6) + "\"" : "null"));
        System.out.println("Retrieved slot 6: " + (retrieved7 != null ? "\"" + new String(retrieved7) + "\"" : "null"));
        System.out.println("Retrieved slot 7: " + (retrieved8 != null ? "\"" + new String(retrieved8) + "\"" : "null"));
        System.out.println("Retrieved slot 8: " + (retrieved9 != null ? "\"" + new String(retrieved9) + "\"" : "null"));
        System.out.println("Retrieved slot 9: " + (retrieved10 != null ? "\"" + new String(retrieved10) + "\"" : "null"));

        System.out.println();

        System.out.println(" Task 3: Testing Invalid Slot Access");

        byte[] invalid99 = page.getRecord(99);
        byte[] invalidNeg = page.getRecord(-1);

        System.out.println("Get slot 99: " + (invalid99 != null ? new String(invalid99) : "null "));
        System.out.println("Get slot -1: " + (invalidNeg != null ? new String(invalidNeg) : "null "));

        System.out.println();
    }
}
