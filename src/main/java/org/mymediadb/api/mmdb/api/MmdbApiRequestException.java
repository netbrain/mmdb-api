package org.mymediadb.api.mmdb.api;

import com.google.gson.JsonPrimitive;
import org.mymediadb.api.mmdb.model.MmdbApiError;

public class MmdbApiRequestException extends MmdbApiException implements MmdbApiError{


    private int status;
    private String text;

    public MmdbApiRequestException(String text, int status) {
        this.status = status;
        this.text = text;
    }


    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getMessage() {
        return "An API call returned in error: status="+getStatus()+" text="+getText();
    }
}
