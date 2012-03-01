package org.mymediadb.api.mmdb.api;

import org.mymediadb.api.mmdb.model.MmdbApiError;
import org.mymediadb.api.mmdb.model.OauthError;

public class MmdbApiOauthException extends MmdbApiException {

    public MmdbApiOauthException(OauthError mmdbApiOauthError) {
        super(mmdbApiOauthError);
    }
}
