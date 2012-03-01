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

package org.mymediadb.api.mmdb.internal.model;

import org.mymediadb.api.mmdb.model.Movie;

public class MovieImpl extends MediaImpl implements Movie {
    private long tmdbId;
    private String imdbId;
    private String name;


    @Override
    public long getTmdbId() {
        return this.tmdbId;
    }

    @Override
    public String getImdbId() {
        return this.imdbId;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
