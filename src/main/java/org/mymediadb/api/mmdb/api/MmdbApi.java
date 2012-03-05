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


import org.mymediadb.api.mmdb.model.Token;
import org.mymediadb.api.mmdb.model.User;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface MmdbApi {

    URI getAuthorizeEndpoint(String redirectUri, String state);

    Token getAccessToken();

    void setAccessToken(Token accessToken);

    Token getAccessToken(String username, String password);

    User getUser();

    User getUser(String username);

    Collection<User> getUserFriends(String username);
}
