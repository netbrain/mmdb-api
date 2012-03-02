package org.mymediadb.api.mmdb.model;

public interface Token {

    enum TokenType {
        bearer
    }

    String getAccessToken();
    TokenType getTokenType();
}
