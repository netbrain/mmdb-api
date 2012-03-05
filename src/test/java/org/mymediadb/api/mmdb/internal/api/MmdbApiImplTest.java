package org.mymediadb.api.mmdb.internal.api;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mymediadb.api.mmdb.api.MmdbApiException;
import org.mymediadb.api.mmdb.api.MmdbApiOauthException;
import org.mymediadb.api.mmdb.api.MmdbApiRequestException;
import org.mymediadb.api.mmdb.model.Movie;
import org.mymediadb.api.mmdb.model.Token;
import org.mymediadb.api.mmdb.model.User;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mymediadb.api.mmdb.internal.api.MmdbApiImpl.*;

public class MmdbApiImplTest {

    private final static String MMDB_API_URL = API_SCHEMA+"://"+API_HOST+":"+API_PORT+API_PATH;
    private final static String AUTHORIZE_ENDPOINT = MMDB_API_URL+"/oauth/authorize";

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
        mmdbApi.setAccessToken(null);
    }

    @Test
    public void testAuthorizationUrlWhereNoRedirectUriOrStateIsSpecified() {
        URI url = mmdbApi.getAuthorizeEndpoint(null,null);
        assertEquals(AUTHORIZE_ENDPOINT + "?client_id=" +CLIENT_ID+"&response_type=token",url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereNoStateIsSpecified() throws Exception {
        String redirect = "http://www.example.com/callback";
        URI url = mmdbApi.getAuthorizeEndpoint(redirect,null);
        assertEquals(AUTHORIZE_ENDPOINT + "?client_id=" +CLIENT_ID+"&response_type=token&redirect_uri="+redirect,url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereNoRedirectIsSpecified() throws Exception {
        String state = "my-state=??&!()";
        URI url = mmdbApi.getAuthorizeEndpoint(null,state);
        assertEquals(AUTHORIZE_ENDPOINT + "?client_id=" +CLIENT_ID+"&response_type=token&state="+state,url.toString());
    }

    @Test
    public void testAuthorizationUrlWhereRedirectAndStateIsSpecified() throws Exception {
        String redirect = "http://www.example.com/callback";
        String state = "my-state=??&!()";
        URI url = mmdbApi.getAuthorizeEndpoint(redirect,state);
        assertEquals(AUTHORIZE_ENDPOINT + "?client_id=" +CLIENT_ID+"&response_type=token&redirect_uri="+redirect+"&state="+state,url.toString());
    }

    @Test
    public void testGetTokenWithInvalidClientId() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        mmdbApi.setClientId("invalid");
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiOauthException x){
            assertEquals("unauthorized_client",x.getText());
        }
    }

    @Test
    public void testGetTokenWithInvalidClientSecret() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        mmdbApi.setClientSecret("invalid");
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiOauthException x){
            assertEquals("unauthorized_client",x.getText());
        }
    }

    @Test
    public void testGetTokenWithInvalidUsername() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        try{
            mmdbApi.getAccessToken(null,null);
            fail("exception was not thrown!");
        }catch (MmdbApiOauthException x){
            assertEquals("invalid_request",x.getText());
        }
    }

    @Test
    public void testGetTokenWithNonExistingUsernameAndPassword() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
        try{
            mmdbApi.getAccessToken("user-doesnt-exist","password");
            fail("exception was not thrown!");
        }catch (MmdbApiOauthException x){
            assertEquals("invalid_grant",x.getText());
        }
    }

    @Test
    public void testGetTokenWithExistingUsernameButIncorrectPassword() throws Exception {
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET,VALID_USERNAME);
        try{
            mmdbApi.getAccessToken(VALID_USERNAME,"invalid-password");
            fail("exception was not thrown!");
        }catch (MmdbApiOauthException x){
            assertEquals("invalid_grant",x.getText());
        }
    }

    @Test
    public void testGetTokenWithValidUsernameAndPasswordCredentials() throws Exception {
        Token token = getAccessToken();
        assertNotNull(token);
        assertNotNull(token.getAccessToken());
        assertEquals(Token.TokenType.bearer,token.getTokenType());
    }

    @Test(expected = MmdbApiException.class)
    public void testGetUserWithoutAccessToken() throws Exception {
        mmdbApi.getUser();
    }

    @Test
    public void testGetUserWithAccessToken() throws Exception {
        mmdbApi.setAccessToken(getAccessToken());
        User user = mmdbApi.getUser();
        assertNotNull(user);
        assertNotNull(user.getUsername());
        assertNotNull(user.getDisplayName());
    }

    @Test
    public void testGetUserByUsernameWithoutAccessToken() throws Exception {
        Assume.assumeNotNull(VALID_USERNAME);
        User user = mmdbApi.getUser(VALID_USERNAME);
        assertNotNull(user);
    }

    @Test
    public void testGetUserByUsernameWithAccessToken() throws Exception {
        Assume.assumeNotNull(VALID_USERNAME);
        mmdbApi.setAccessToken(getAccessToken());
        User user = mmdbApi.getUser(VALID_USERNAME);
        assertNotNull(user);
    }

    @Test
    public void testGetUserFriendsWithNullParameter() throws Exception {
        try{
            mmdbApi.getUserFriends(null);
            fail("exception was not thrown!");
        }catch (MmdbApiRequestException x){
            assertEquals(401,x.getStatus());
        }
    }

    @Test
    public void testGetUserFriendsWithInvalidNonExistantUser() throws Exception {
        try{
            mmdbApi.setAccessToken(getAccessToken());
            mmdbApi.getUserFriends("non-existant");
            fail("exception was not thrown!");
        }catch (MmdbApiRequestException x){
            assertEquals(400,x.getStatus());
        }
    }

    @Test
    public void testGetUserFriendsWithValidUser() throws Exception {
        mmdbApi.setAccessToken(getAccessToken());
        Collection<User> friends = mmdbApi.getUserFriends(VALID_USERNAME);
        assertNotNull(friends);

    }

    @Test
    public void testGetLibraryList() throws Exception{
        mmdbApi.setAccessToken(getAccessToken());
        List<Movie> library = mmdbApi.getLibrary(VALID_USERNAME, Movie.class);
        assertNotNull(library);
    }

    private Token getAccessToken() {
        Assume.assumeNotNull(CLIENT_ID, CLIENT_SECRET, VALID_PASSWORD, VALID_USERNAME);
        return mmdbApi.getAccessToken(VALID_USERNAME,VALID_PASSWORD);
    }
}
