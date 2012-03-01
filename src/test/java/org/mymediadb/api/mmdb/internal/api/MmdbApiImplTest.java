package org.mymediadb.api.mmdb.internal.api;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mymediadb.api.mmdb.api.MmdbApi;
import org.mymediadb.api.mmdb.api.MmdbApiException;
import org.mymediadb.api.mmdb.model.Token;

import java.net.URI;
import java.net.URLEncoder;

import static junit.framework.Assert.*;

public class MmdbApiImplTest {

    private final static String MMDB_API_URL = "http://test.mymediadb.org:80/api/0.2";
    private final static String CLIENT_ID = System.getProperty("mmdb.api.clientId");
    private final static String CLIENT_SECRET = System.getProperty("mmdb.api.clientSecret");

    private MmdbApiImpl mmdbApi;

    @Before
    public void setup() {
        mmdbApi = (MmdbApiImpl) MmdbApiImpl.getInstance();
        Assume.assumeNotNull(CLIENT_ID,CLIENT_SECRET);
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

    @Test(expected = MmdbApiException.class)
    public void testGetTokenWithInvalidClientId() throws Exception {
        System.setProperty("mmdb.api.clientId","invalid");
        try{
            Token token = mmdbApi.getAccessToken(null,null);
        }catch (MmdbApiException x){
            assertEquals("unauthorized_client",x.getMessage());
            throw x;
        }
    }

    @Test(expected = MmdbApiException.class)
    public void testGetTokenWithInvalidClientSecret() throws Exception {
        System.setProperty("mmdb.api.clientSecret","invalid");
        try{
            Token token = mmdbApi.getAccessToken(null,null);
        }catch (MmdbApiException x){
            assertEquals("unauthorized_client",x.getMessage());
            throw x;
        }
    }

    @Test(expected = MmdbApiException.class)
    public void testGetTokenWithInvalidUsername() throws Exception {
        try{
            Token token = mmdbApi.getAccessToken(null,null);
        }catch (MmdbApiException x){
            assertEquals("unauthorized_client",x.getMessage());
            throw x;
        }
    }
}
