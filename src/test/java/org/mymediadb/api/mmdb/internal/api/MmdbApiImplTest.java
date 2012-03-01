package org.mymediadb.api.mmdb.internal.api;

import org.junit.Before;
import org.junit.Test;
import org.mymediadb.api.mmdb.api.MmdbApi;
import org.mymediadb.api.mmdb.api.MmdbApi.MediaType;
import org.mymediadb.api.mmdb.model.Media;
import org.mymediadb.api.mmdb.model.Movie;
import org.mymediadb.api.mmdb.model.User;
import org.mymediadb.api.mmdb.model.UserMedia;

import java.util.List;

import static junit.framework.Assert.*;

public class MmdbApiImplTest {

    private MmdbApi mmdbApi;

    @Before
    public void setup() {
        mmdbApi = MmdbApiImpl.getInstance();
        mmdbApi.setBasicAuthentication(System.getProperty("test.basic.auth.username"), System.getProperty("test.basic.auth.password"));
    }

    @Test
    public void testValidSearch() {
        List<? extends Media> movies = mmdbApi.search(MediaType.MOVIE, "Prince of persia");
        assertNotNull(movies);
    }

    @Test
    public void testInvalidSearch() {
        List<? extends Media> movies = mmdbApi.search(MediaType.MOVIE, "");
        assertNull(movies);
    }

    @Test
    public void testInvalidSearchWithSpaces() {
        List<? extends Media> movies = mmdbApi.search(MediaType.MOVIE, "    ");
        assertNull(movies);
    }

    @Test
    public void testNullSearch() {
        List<? extends Media> movies = mmdbApi.search(MediaType.MOVIE, null);
        assertNull(movies);
    }

    @Test
    public void testGetUser() {
        User user = mmdbApi.getUser();
        assertNotNull(user);
    }

    @Test
    public void testGetMediaByMmdbId() {
        Media media = mmdbApi.getMedia(MmdbApi.MediaType.MOVIE, MmdbApi.IdType.MMDB, 1);
        assertNotNull(media);
        assertNotNull(media.getUserMedia());
    }

    @Test
    public void testGetMovieByMmdbId() {
        Movie movie = mmdbApi.getMedia(Movie.class, MmdbApi.IdType.MMDB, 1);
        assertNotNull(movie);
        assertNotNull(movie.getUserMedia());
    }

    @Test
    public void testGetMovieByTmdbId() {
        Movie movie = mmdbApi.getMedia(Movie.class, MmdbApi.IdType.TMDB, 137);
        assertNotNull(movie);
        assertNotNull(movie.getUserMedia());
    }

    @Test
    public void testGetMovieByImdbId() {
        Movie movie = mmdbApi.getMedia(Movie.class, MmdbApi.IdType.IMDB, "tt0111161");
        assertNotNull(movie);
        assertNotNull(movie.getUserMedia());
    }

    @Test
    public void testGetUserMedia() {
        UserMedia userMedia = mmdbApi.getUserMedia(MmdbApi.MediaType.MOVIE, MmdbApi.IdType.IMDB, "tt0111161");
        assertNotNull(userMedia);
    }

    @Test
    public void testPutUserMedia() {
        UserMedia userMedia = mmdbApi.getUserMedia(MmdbApi.MediaType.MOVIE, MmdbApi.IdType.IMDB, "tt0111161");
        boolean acquired = userMedia.isAcquired();
        userMedia = mmdbApi.getEmptyUserMedia();
        userMedia.setAcquired(!acquired);
        userMedia = mmdbApi.putUserMedia(MmdbApi.MediaType.MOVIE, MmdbApi.IdType.IMDB, "tt0111161", userMedia);
        assertTrue(userMedia.isAcquired() != acquired);
    }
}
