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

package org.mymediadb.api.mmdb.api;

import org.mymediadb.api.mmdb.model.Media;
import org.mymediadb.api.mmdb.model.User;
import org.mymediadb.api.mmdb.model.UserMedia;

import java.util.List;

public interface MmdbApi {

    public enum IdType {
        MMDB,
        TMDB,
        IMDB;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public enum MediaType {
        MOVIE,
        SERIES,
        BOOKS,
        MUSIC;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public List<? extends Media> search(MediaType mediaType, String searchQuery);

    public Media getMedia(MediaType mediaType, IdType idType, Object id);

    public <T> T getMedia(Class<T> mediaType, IdType idType, Object id);

    public User getUser();

    public UserMedia putUserMedia(MediaType mediaType, IdType idType, Object id, UserMedia media);

    public UserMedia getUserMedia(MediaType mediaType, IdType idType, Object id);

    public UserMedia getEmptyUserMedia();

    /**
     * Set authentication information to be used with mmdb api
     *
     * @param username
     * @param password
     */
    public void setBasicAuthentication(String username, String password);
}
