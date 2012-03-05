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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.mymediadb.api.mmdb.api.*;
import org.mymediadb.api.mmdb.internal.model.*;
import org.mymediadb.api.mmdb.model.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MmdbApiImpl implements MmdbApi {
    public final static Logger log = Logger.getLogger(MmdbApiImpl.class);

    public final static int API_PORT = 8080;
    public final static String API_SCHEMA = "http";
    public final static String API_HOST = "dev.mymediadb.org";
    public final static String API_PATH = "/mymediadb/api/0.2";

    public final static String OAUTH_AUTHORIZE_ENDPOINT = "/oauth/authorize";
    public final static String OAUTH_TOKEN_ENDPOINT = "/oauth/token";
    public final static String USER_ENDPOINT = "/user";

    private final static Header ACCEPT_HEADER = new BasicHeader("Accept","application/json");

    private static final HttpHost targetHost = new HttpHost(API_HOST, API_PORT, API_SCHEMA);
    private static final DefaultHttpClient httpClient;
    private static final Gson gson;
    private static final JsonParser jsonParser = new JsonParser();

    private static MmdbApi instance = null;

    private static final int HTTP_CONNECTION_TIMEOUT = 240000;
    private static final int HTTP_MAX_CONNECTIONS = 100;
    private static final int HTTP_MAX_CONNECTIONS_PER_ROUTE = 16;

    private String clientId;
    private String clientSecret;
    private Token accessToken;



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
        clientId = System.getProperty("mmdb.api.clientId");
        clientSecret = System.getProperty("mmdb.api.clientSecret");
    }

    public static MmdbApi getInstance() {
        if (instance == null) {
            instance = new MmdbApiImpl();
        }
        return instance;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public URI getAuthorizeEndpoint(String redirectUri, String state){
        String queryParams = "client_id="+ clientId +"&response_type=token";

        if(redirectUri != null){
            queryParams += "&redirect_uri="+redirectUri;
        }

        if(state != null){
            queryParams += "&state="+state;
        }

        return getUri(OAUTH_AUTHORIZE_ENDPOINT,queryParams);
    }

    @Override
    public Token getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Token getAccessToken(String username, String password){
        URI uri = getUri(OAUTH_TOKEN_ENDPOINT);
        UrlEncodedFormEntity postParameters = createUrlEncodedFormParameters(
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("grant_type", "password"),
                new BasicNameValuePair("username", username),
                new BasicNameValuePair("password", password)
        );
        HttpPost post = new HttpPost(uri);
        post.setEntity(postParameters);
        HttpResponse response = sendRequest(post);
        if(!isResponseStatusOK(response)){
            JsonObject error = jsonParser.parse(getEntityAsString(response)).getAsJsonObject();
            throw new MmdbApiOauthException(error.getAsJsonPrimitive("error").getAsString(),response.getStatusLine().getStatusCode());
        }else{
            return gson.fromJson(getEntityAsString(response), TokenImpl.class);
        }
    }


    @Override
    public User getUser() {
        return getUser(null);
    }

    @Override
    public User getUser(String username) {
        String endpoint = USER_ENDPOINT;
        if(username != null){
            endpoint += "/"+username;
        }else{
            if(isAccessTokenSet()){
                throw new MmdbApiException("Access token is required on this request.");
            }
        }
        URI uri = getUri(endpoint);
        HttpGet get = new HttpGet(uri);
        get.setHeader(ACCEPT_HEADER);
        HttpResponse response = sendRequest(get);
        if(isResponseStatusError(response)){
            JsonObject jsonObject = jsonParser.parse(getEntityAsString(response)).getAsJsonObject();
            throw new MmdbApiRequestException(jsonObject.getAsJsonPrimitive("text").getAsString(),response.getStatusLine().getStatusCode());
        }else{
            return gson.fromJson(getEntityAsString(response), UserImpl.class);
        }
    }

    @Override
    public Collection<User> getUserFriends(String username) {
        URI uri = getUri(USER_ENDPOINT+"/"+username+"/friends");
        HttpGet get = new HttpGet(uri);
        get.setHeader(ACCEPT_HEADER);
        HttpResponse response = sendRequest(get);
        if(isResponseStatusError(response)){
            JsonObject jsonObject = jsonParser.parse(getEntityAsString(response)).getAsJsonObject();
            throw new MmdbApiRequestException(jsonObject.getAsJsonPrimitive("text").getAsString(),response.getStatusLine().getStatusCode());
        }else{
            Collection<User> friends = gson.fromJson(getEntityAsString(response), new TypeToken<Collection<UserImpl>>() {}.getType());
            return friends != null ? friends : (Collection<User>) Collections.EMPTY_LIST;
        }
    }

    public <T> List<T> getLibrary(String username, Class<T> type) {
        String libraryType;
        Type deserializeType;
        if(type.equals(Movie.class)){
            libraryType = "movie";
            deserializeType = new TypeToken<List<MovieImpl>>(){}.getType();
        }else if(type.equals(Series.class)){
            libraryType = "series";
            deserializeType = new TypeToken<List<SeriesImpl>>(){}.getType();
        }else if(type.equals(Episode.class)){
            libraryType = "episode";
            deserializeType = new TypeToken<List<EpisodeImpl>>(){}.getType();
        }else{
            throw new IllegalArgumentException("Illegal type argument!");
        }

        URI uri = getUri(USER_ENDPOINT+"/"+username+"/library/"+libraryType+"/list");
        HttpGet get = new HttpGet(uri);
        get.setHeader(ACCEPT_HEADER);
        HttpResponse response = sendRequest(get);
        if(isResponseStatusError(response)){
            JsonObject jsonObject = jsonParser.parse(getEntityAsString(response)).getAsJsonObject();
            throw new MmdbApiRequestException(jsonObject.getAsJsonPrimitive("text").getAsString(),response.getStatusLine().getStatusCode());
        }else{
            return gson.fromJson(getEntityAsString(response),deserializeType);
        }
    }

    private boolean isResponseStatusError(HttpResponse response) {
        return !(isResponseStatusOK(response) || isResponseStatusNoContent(response));
    }

    private boolean isResponseStatusNoContent(HttpResponse response) {
        return response.getStatusLine().getStatusCode() == 204;
    }

    private boolean isResponseStatusOK(HttpResponse response) {
        return response.getStatusLine().getStatusCode() == 200;
    }

    private String getEntityAsString(HttpResponse response) {
        try {
            if(response.getEntity() == null){
                return "";
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.fatal("error occurred",e);
            throw new RuntimeException(e);
        }
    }

    private HttpResponse sendRequest(HttpRequestBase req) {
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

    private URI getUri(String endpoint) {
        return getUri(endpoint,null);
    }

    private URI getUri(String endpoint,String queryParams) {
        try {
            if(!isAccessTokenSet()){
                endpoint = "/!"+this.accessToken.getAccessToken()+endpoint;
            }
            return URIUtils.createURI(API_SCHEMA, API_HOST, API_PORT, API_PATH + endpoint, queryParams, null);
        } catch (URISyntaxException e) {
            log.fatal("something wrong with uri syntax", e);
            throw new RuntimeException(e);
        }
    }

    private boolean isAccessTokenSet() {
        return accessToken == null;
    }

}
