package com.marcantony.smbprovider.provider.smb;

import android.webkit.MimeTypeMap;

import java.nio.file.Paths;

public class EntryStats {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public final Long size;
    public final Long lastModifiedMillis;
    public final String mimeType;

    public EntryStats(String name) {
        this(name, null, null);
    }

    public EntryStats(String name, Long size, Long lastModifiedMillis) {
        this.size = size;
        this.lastModifiedMillis = lastModifiedMillis;
        this.mimeType = getMimeTypeFromPath(name);
    }

    private String getMimeTypeFromPath(String path) {
        String name = Paths.get(path).getFileName().toString();
        int extensionPos = name.indexOf('.');

        if (extensionPos == -1) {
            // no "."
            return DEFAULT_MIME_TYPE;
        }

        String extension = name.substring(extensionPos + 1);
        String guessedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return guessedMimeType != null ? guessedMimeType : DEFAULT_MIME_TYPE;
    }

}
