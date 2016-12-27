package com.allco.flickrsearch;

import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSource;
import okio.Okio;

import static com.squareup.okhttp.internal.Util.closeQuietly;

public class AssetHelper {

    /**
     * Loads file to String
     *
     * @param filename file path under app/src/test/resources
     * @return
     * @throws IOException
     */
    public static String readAssetFile(final String filename) throws IOException {
        BufferedSource buffer = null;
        try {
            InputStream resourceAsStream = AssetHelper.class.getClassLoader().getResourceAsStream(filename);
            buffer = Okio.buffer(Okio.source(resourceAsStream));
            return buffer.readUtf8();
        }  finally {
            closeQuietly(buffer);
        }
    }
}
