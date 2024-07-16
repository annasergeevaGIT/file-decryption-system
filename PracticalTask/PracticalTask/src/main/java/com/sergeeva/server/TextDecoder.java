package com.sergeeva.server;

import com.netconomy.encryption.suite.Encryptor;

public class TextDecoder implements FileTypeDecoder {

    @Override
    public byte[] decodeFile(byte[] encodedContent, String key) {

        String endocdedString = new String(encodedContent);

        return Encryptor.decrypt(key, endocdedString).getBytes();
    }
}
