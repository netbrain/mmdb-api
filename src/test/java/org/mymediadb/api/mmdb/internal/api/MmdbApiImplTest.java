package org.mymediadb.api.mmdb.internal.api;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mymediadb.api.mmdb.api.MmdbApiException;
import org.mymediadb.api.mmdb.model.Token;

import java.net.URI;

import static junit.framework.Assert.*;
import static org.mymediadb.api.mmdb.internal.api.MmdbApiImpl.*;

public class MmdbApiImplTest {

    private final static String MMDB_API_URL = API_SCHEMA+"://"+API_HOST+":"+API_PORT+API_PATH;
    private final static String CLIENT_ID = System.getProperty("org.mymediadb.api.mmdb.test.CLIENT_ID");
    private final static String CLIENT_SECRET = System.getProperty("org.mymediadb.api.mmdb.test.CLIENT_SECRET");
    private final static String VALID_USERNAME = System.getProperty("org.mymediadb.api.mmdb.test.VALID_USERNAME");
    private final static String VALID_PASSWORD = System.getProperty("org.mymediadb.api.mmdb.test.VALID_PASSWORD");

    private MmdbApiImpl mmdbApi;

    @Before
    public void setup() {
        mmdbApi = (MmdbApiImpl) MmdbApiImpl.getInstance();
        mmdbApi.setClientId(CLIENT_ID);
        mmdbApi.setClientSecret(CLIENT_SECRET);
    }

    @Test
    public void testAuthorizationUrlWhereNoRedirectUriOrStateIsSpecified() {
        URI url = mmdbApi.getAuthorizeEndpoint(null,null);
        assertEquals(MMDB_API_URL+"/oauth/authorize?client_id="+CLIENT_ID+"&response_type=token",url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereNoStateIsSpecified() throws Exception {
        String redirect = "http://www.example.com/callback";
        URI url = mmdbApi.getAuthorizeEndpoint(redirect,null);
        assertEquals(MMDB_API_URL+"/oauth/authorize?client_id="+CLIENT_ID+"&response_type=token&redirect_uri="+redirect,url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereNoRedirectIsSpecified() throws Exception {
        String state = "my-state=??&!()";
        URI url = mmdbApi.getAuthorizeEndpoint(null,state);
        assertEquals(MMDB_API_URL+"/oauth/authorize?client_id="+CLIENT_ID+"&response_type=token&state="+state,url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereRedirectAndStateIsSpecified() throws Exception {
        String redirect = "http://www.example.com/callback";
        String state = "my-state=??&!()";
        URI url = mmdbApi.getAuthorizeEndpoint(redirect,state);
        assertEquals(MMDB_API_URL+"/oauth/authorize?client_id="+CLIENT_ID+"&response_type=token&redirect_uri="+redirect+"&state="+state,url.toString());
    }

    @Test
    public void testGetTokenWithInvalidClientId() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        mmdbApi.setClientId("invalid");
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiException x){
            assertEquals("unauthorized_client",x.getMessage());
        }
    }

    @Test
    public void testGetTokenWithInvalidClientSecret() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        mmdbApi.setClientSecret("invalid");
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiException x){
            assertEquals("unauthorized_client",x.getMessage());
        }
    }

    @Test
    public void testGetTokenWithInvalidUsername() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiException x){
            assertEquals("invalid_request",x.getMessage());
        }
    }

    @Test
    public void testGetTokenWithNonExistingUsernameAndPassword() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        try{
            mmdbApi.getAccessToken("user-doesnt-exist","password");
            fail("exception was not thrown!");
        }catch (MmdbApiException x){
            assertEquals("invalid_grant",x.getMessage());
        }
    }

    public void testGetTokenWithExistingUsernameButIncorrectPassword() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET,VALID_USERNAME);
        try{
            mmdbApi.getAccessToken(VALID_USERNAME,"invalid-password");
            fail("exception was not thrown!");
        }catch (MmdbApiException x){
            assertEquals("invalid_grant",x.getMessage());
        }
    }

    @Test
    public void testGetTokenWithValidUsernameAndPasswordCredentials() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET,VALID_PASSWORD,VALID_USERNAME);
        Token token = mmdbApi.getAccessToken(VALID_USERNAME,VALID_PASSWORD);
        assertNotNull(token);
        assertNotNull(token.getAccessToken());
        assertEquals(Token.TokenType.bearer,token.getTokenType());
    }
}
