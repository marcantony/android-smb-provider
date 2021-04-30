package com.marcantony.smbprovider.smb;

public interface File {

    /**
     * Read bytes into a byte buffer.
     * @param offset Offset in bytes from the file head specifying where to start reading.
     * @param size Number of bytes to read.
     * @param data Byte array to store read bytes.
     * @return Number of bytes actually read.
     */
    int read(long offset, int size, byte[] data);

    /**
     * Write bytes from byte buffer.
     * @param offset Offset in bytes from the file head specifying where to write bytes.
     * @param size Number of bytes to write.
     * @param data Byte array of data to write.
     * @return Number of bytes actually written.
     */
    int write(long offset, int size, byte[] data);

    /**
     * @return Number of bytes in the file.
     */
    long size();

    /**
     * Invoked when file should be closed.
     */
    void close();

}
