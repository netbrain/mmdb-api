package org.mymediadb.api.mmdb.api;

import org.mymediadb.api.mmdb.model.MmdbApiError;

public class MmdbApiOauthException extends MmdbApiException implements MmdbApiError {

    private String error;
    private int status;

    public MmdbApiOauthException(String error, int status) {
        this.error = error;
        this.status = status;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getText() {
        return this.error;
    }

    @Override
    public String getMessage() {
        return "OAuth call returned in error: status="+getStatus()+" text="+getText();
    }

}
