package org.mymediadb.api.mmdb.internal.model;

import org.mymediadb.api.mmdb.model.Image;

public class ImageImpl implements Image {
    Long id;
    int width;
    int height;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
