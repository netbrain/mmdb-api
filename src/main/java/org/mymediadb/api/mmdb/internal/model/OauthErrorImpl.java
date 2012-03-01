package org.mymediadb.api.mmdb.internal.model;

import org.mymediadb.api.mmdb.model.OauthError;

public class OauthErrorImpl implements OauthError{

    private String error;

    @Override
    public String getError() {
        return this.error;
    }
}
