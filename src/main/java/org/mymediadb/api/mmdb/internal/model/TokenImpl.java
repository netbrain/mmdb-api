package org.mymediadb.api.mmdb.internal.model;

import org.mymediadb.api.mmdb.model.Token;

public class TokenImpl implements Token {

    private TokenType token_type;
    private String access_token;

    @Override
    public String getAccessToken() {
        return this.access_token;
    }

    @Override
    public TokenType getTokenType() {
        return this.token_type;
    }
}
