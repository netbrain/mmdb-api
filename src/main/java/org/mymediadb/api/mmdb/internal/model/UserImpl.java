package org.mymediadb.api.mmdb.internal.model;

import org.mymediadb.api.mmdb.model.Image;
import org.mymediadb.api.mmdb.model.User;

public class UserImpl implements User{

    private String username;
    private String displayName;
    private Image picture25;
    private Image picture50;
    private Image picture150;

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Image getPicture25() {
        return this.picture25;
    }

    @Override
    public Image getPicture50() {
        return this.picture50;
    }

    @Override
    public Image getPicture150() {
        return this.picture150;
    }
}
