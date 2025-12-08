package com.kltn.scsms_api_service.core.dto.mediaManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MediaFilterParam extends BaseFilterParam<MediaFilterParam> {
    
    private String entityType;
    private UUID entityId;
    private String mediaType;
    private Boolean isMain;
    private Boolean isDeleted;
    
    @Override
    protected String getDefaultSortField() {
        return "sortOrder";
    }
    
    @Override
    protected void standardizeSpecificFields(MediaFilterParam request) {
        request.setEntityType(trimAndNullify(request.getEntityType()));
        request.setMediaType(trimAndNullify(request.getMediaType()));
    }
    
    public static MediaFilterParam standardize(MediaFilterParam filterParam) {
        if (filterParam == null) {
            filterParam = new MediaFilterParam();
        }
        return filterParam.standardizeFilterRequest(filterParam);
    }
}
