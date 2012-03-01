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
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.mymediadb.api.mmdb.api.MmdbApi;
import org.mymediadb.api.mmdb.internal.model.MovieImpl;
import org.mymediadb.api.mmdb.internal.model.UserImpl;
import org.mymediadb.api.mmdb.internal.model.UserMediaImpl;
import org.mymediadb.api.mmdb.model.Media;
import org.mymediadb.api.mmdb.model.Movie;
import org.mymediadb.api.mmdb.model.User;
import org.mymediadb.api.mmdb.model.UserMedia;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MmdbApiImpl implements MmdbApi {
    public final static Logger log = Logger.getLogger(MmdbApiImpl.class);

    private final static int API_PORT = 80;
    private final static String API_SCHEMA = "http";
    private final static String API_HOST = "www.mymediadb.org";
    private final static String API_PATH = "/api/0.1";

    private static final HttpHost targetHost = new HttpHost(API_HOST, API_PORT, API_SCHEMA);
    private static final DefaultHttpClient httpClient = new DefaultHttpClient();
    private static final BasicHttpContext localContext = new BasicHttpContext();
    private static final AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());

    private static MmdbApi instance = null;

    static {
        //initialize cookie handling
        httpClient.setCookieStore(new BasicCookieStore());

        //set basic authentication cache
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
    }

    private MmdbApiImpl() {
    }

    @Override
    public void setBasicAuthentication(String username, String password) {
        httpClient.getCredentialsProvider().setCredentials(authScope, new UsernamePasswordCredentials(username, password));
    }

    @Override
    public List<? extends Media> search(MediaType mediaType, String searchQuery) {
        if (searchQuery == null)
            return null;

        searchQuery = searchQuery.trim();
        if (searchQuery.length() == 0)
            return null;

        try {
            String url = API_PATH + "/search?mediaType=" + mediaType + "&searchQuery=" + URLEncoder.encode(searchQuery, "UTF-8");
            String response = sendHttpGETRequest(url);

            switch (mediaType) {
                case MOVIE:
                    return convertFromJsonToObjectOfType(response, new TypeToken<List<MovieImpl>>() {
                    });
                default:
                    throw new IllegalArgumentException(mediaType + " is stub");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }

    @Override
    public User getUser() {
        String url = API_PATH + "/user";
        String response = sendHttpGETRequest(url);
        return convertFromJsonToObjectOfType(response, new TypeToken<UserImpl>() {
        });
    }

    @Override
    public Media getMedia(MediaType mediaType, IdType idType, Object id) {
        switch (mediaType) {
            case MOVIE:
                return getMedia(Movie.class, idType, id);
            default:
                throw new IllegalArgumentException(mediaType + " is stub");
        }
    }

    @Override
    public <T> T getMedia(Class<T> classObj, IdType idType, Object id) {

        MediaType mediaType = null;
        TypeToken typeToken = null;

        if (classObj.equals(Movie.class)) {
            mediaType = MediaType.MOVIE;
            typeToken = new TypeToken<MovieImpl>() {
            };
        } else {
            throw new IllegalArgumentException("stub");
        }

        String url = API_PATH + "/media?mediaType=" + mediaType + "&idType=" + idType + "&id=" + id;
        String response = sendHttpGETRequest(url);
        return (T) convertFromJsonToObjectOfType(response, typeToken);
    }

    @Override
    public UserMedia putUserMedia(MediaType mediaType, IdType idType, Object id, UserMedia media) {
        String url = API_PATH + "/userMedia?mediaType=" + mediaType + "&idType=" + idType + "&id=" + id;
        String payload = convertFromObjectToJson(media);
        String response = sendHttpPUTRequest(url, payload);
        return convertFromJsonToObjectOfType(response, new TypeToken<UserMediaImpl>() {
        });
    }

    @Override
    public UserMedia getUserMedia(MediaType mediaType, IdType idType, Object id) {
        String url = API_PATH + "/userMedia?mediaType=" + mediaType + "&idType=" + idType + "&id=" + id;
        String response = sendHttpGETRequest(url);
        return convertFromJsonToObjectOfType(response, new TypeToken<UserMediaImpl>() {
        });
    }

    @Override
    public UserMedia getEmptyUserMedia() {
        return new UserMediaImpl();
    }

    private String sendHttpGETRequest(String url) {
        if (!isBasicAuthenticationSet()) {
            throw new RuntimeException("no basic authentication is set!");
        }

        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "text/json");
            httpGet.setHeader("Content-Type", "text/json");
            HttpResponse response = httpClient.execute(targetHost, httpGet, localContext);
            String responseData = EntityUtils.toString(response.getEntity());
            return responseData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sendHttpPUTRequest(String url, String data) {
        if (!isBasicAuthenticationSet()) {
            throw new RuntimeException("no basic authentication is set!");
        }

        try {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Accept", "text/json");
            httpPut.setHeader("Content-Type", "text/json");
            httpPut.setEntity(new StringEntity(data));
            HttpResponse response = httpClient.execute(targetHost, httpPut, localContext);
            String responseData = EntityUtils.toString(response.getEntity());
            return responseData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private <T> T convertFromJsonToObjectOfType(String response, TypeToken<T> type) {
        try {
            T result = (T) new Gson().fromJson(response, type.getType());
            return result;
        } catch (JsonParseException x) {
            log.warn("could not parse json", x);
        } catch (Exception x) {
            log.error("Unhandled exception!", x);
        }
        return null;
    }

    private String convertFromObjectToJson(Object obj) {
        return new Gson().toJson(obj);
    }

    private boolean isBasicAuthenticationSet() {
        return httpClient.getCredentialsProvider().getCredentials(authScope) != null;
    }

    public static MmdbApi getInstance() {
        if (instance == null) {
            instance = new MmdbApiImpl();
        }
        return instance;
    }

}
