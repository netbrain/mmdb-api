package org.mymediadb.api.mmdb.api;

import org.mymediadb.api.mmdb.internal.model.OauthErrorImpl;
import org.mymediadb.api.mmdb.model.MmdbApiError;

public class MmdbApiException extends RuntimeException {

    private MmdbApiError mmdbApiError;

    public MmdbApiException(MmdbApiError mmdbApiError) {
        this.mmdbApiError = mmdbApiError;
    }

    @Override
    public String getMessage() {
        return mmdbApiError.getError();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
