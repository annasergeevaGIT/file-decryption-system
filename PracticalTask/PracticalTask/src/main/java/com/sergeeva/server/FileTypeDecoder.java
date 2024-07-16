package com.sergeeva.server;

public interface FileTypeDecoder {

    public byte[] decodeFile(byte[] encodedContent, String key);

}



