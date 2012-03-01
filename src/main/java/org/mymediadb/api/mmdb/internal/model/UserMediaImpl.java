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

import org.mymediadb.api.mmdb.model.UserMedia;

public class UserMediaImpl implements UserMedia {
    private Boolean acquired;
    private Emotion emotion;
    private Boolean experienced;
    private Boolean wishlisted;

    @Override
    public Boolean isAcquired() {
        return this.acquired;
    }

    @Override
    public void setAcquired(Boolean acquired) {
        this.acquired = acquired;
    }

    @Override
    public Emotion getEmotion() {
        return this.emotion;
    }

    @Override
    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    @Override
    public Boolean isExperienced() {
        return this.experienced;
    }

    @Override
    public void setExperienced(Boolean experienced) {
        this.experienced = experienced;
    }

    @Override
    public Boolean isWishlisted() {
        return this.wishlisted;
    }

    @Override
    public void setWishlisted(Boolean wishlisted) {
        this.wishlisted = wishlisted;
    }
}
