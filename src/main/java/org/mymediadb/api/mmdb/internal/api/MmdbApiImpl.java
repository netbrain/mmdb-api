/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mymediadb.api.mmdb.internal.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.mymediadb.api.mmdb.api.MmdbApi;
import org.mymediadb.api.mmdb.api.MmdbApiException;
import org.mymediadb.api.mmdb.internal.model.OauthErrorImpl;
import org.mymediadb.api.mmdb.internal.model.TokenImpl;
import org.mymediadb.api.mmdb.model.Token;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;

public class MmdbApiImpl implements MmdbApi {
    public final static Logger log = Logger.getLogger(MmdbApiImpl.class);

    private final static int API_PORT = 80;
    private final static String API_SCHEMA = "http";
    private final static String API_HOST = "test.mymediadb.org";
    private final static String API_PATH = "/api/0.2";

    private static final HttpHost targetHost = new HttpHost(API_HOST, API_PORT, API_SCHEMA);
    private static final DefaultHttpClient httpClient;
    private static final Gson gson;

    private static MmdbApi instance = null;

    private static final int HTTP_CONNECTION_TIMEOUT = 240000;
    private static final int HTTP_MAX_CONNECTIONS = 100;
    private static final int HTTP_MAX_CONNECTIONS_PER_ROUTE = 16;

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;

    static {
        //Initialize httpclient
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_CONNECTION_TIMEOUT);

        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();
        connectionManager.setMaxTotal(HTTP_MAX_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(HTTP_MAX_CONNECTIONS_PER_ROUTE);

        httpClient = new ContentEncodingHttpClient(connectionManager, params);

        //Initialize GSON
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private MmdbApiImpl() {
        CLIENT_ID = System.getProperty("mmdb.api.clientId");
        CLIENT_SECRET = System.getProperty("mmdb.api.clientSecret");
    }

    public static MmdbApi getInstance() {
        if (instance == null) {
            instance = new MmdbApiImpl();
        }
        return instance;
    }

    @Override
    public URI getAuthorizeEndpoint(String redirectUri, String state){
        String queryParams = "client_id="+CLIENT_ID+"&response_type=token";

        if(redirectUri != null){
            queryParams += "&redirect_uri="+redirectUri;
        }

        if(state != null){
            queryParams += "&state="+state;
        }

        return getUri("/oauth/authorize",queryParams);
    }

    @Override
    public Token getAccessToken(String username, String password){
        URI uri = getUri("/oauth/token",null);
        UrlEncodedFormEntity postParameters = createUrlEncodedFormParameters(
                new BasicNameValuePair("client_id", CLIENT_ID),
                new BasicNameValuePair("client_secret", CLIENT_ID),
                new BasicNameValuePair("grant_type", "password"),
                new BasicNameValuePair("username", username),
                new BasicNameValuePair("password", password)
        );
        HttpPost post = new HttpPost(uri);
        post.setEntity(postParameters);
        HttpResponse response = sendRequest(post);
        if(response.getStatusLine().getStatusCode() != 200){
            throw new MmdbApiException(gson.fromJson(getEntityAsString(response), OauthErrorImpl.class));
        }else{
            return gson.fromJson(getEntityAsString(response), TokenImpl.class);
        }
    }

    private String getEntityAsString(HttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.fatal("error occurred",e);
            throw new RuntimeException(e);
        }
    }

    private HttpResponse sendRequest(HttpEntityEnclosingRequestBase req) {
        try {
            return httpClient.execute(req);
        } catch (IOException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }

    private UrlEncodedFormEntity createUrlEncodedFormParameters(NameValuePair ... nameValuePairs) {
        try {
            UrlEncodedFormEntity formParameters = new UrlEncodedFormEntity(
                    Arrays.asList(nameValuePairs)
            ,"UTF-8");
            return formParameters;
        } catch (UnsupportedEncodingException e) {
            log.fatal("error when creating form parameters",e);
            throw new RuntimeException(e);
        }
    }

    private URI getUri(String endpoint,String queryParams) {
        try {
            return URIUtils.createURI(API_SCHEMA, API_HOST, API_PORT, API_PATH + endpoint, queryParams, null);
        } catch (URISyntaxException e) {
            log.fatal("something wrong with uri syntax", e);
            throw new RuntimeException(e);
        }
    }



}
