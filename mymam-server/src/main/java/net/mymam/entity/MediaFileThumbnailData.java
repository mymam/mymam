/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.entity;

import javax.persistence.Embeddable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author fstab
 */
@Embeddable
public class MediaFileThumbnailData {

    @NotNull
    private String smallImg;

    @NotNull
    private String mediumImg;

    @NotNull
    private String largeImg;

    @NotNull
    @Min(value = 0)
    private Long thumbnailOffsetMs;

    public Long getThumbnailOffsetMs() {
        return thumbnailOffsetMs;
    }

    public void setThumbnailOffsetMs(Long thumbnailOffsetMs) {
        this.thumbnailOffsetMs = thumbnailOffsetMs;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }

    public String getMediumImg() {
        return mediumImg;
    }

    public void setMediumImg(String mediumImg) {
        this.mediumImg = mediumImg;
    }

    public String getLargeImg() {
        return largeImg;
    }

    public void setLargeImg(String largeImg) {
        this.largeImg = largeImg;
    }
}
