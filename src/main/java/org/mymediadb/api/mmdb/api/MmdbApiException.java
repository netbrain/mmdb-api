package org.mymediadb.api.mmdb.api;

import org.mymediadb.api.mmdb.model.MmdbApiError;

public class MmdbApiException extends RuntimeException {

    public MmdbApiException() {
        super();
    }

    public MmdbApiException(String s) {
        super(s);
    }

    public MmdbApiException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
