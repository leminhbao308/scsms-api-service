package com.kltn.scsms_api_service.core.dto.promotionTypeManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PromotionTypeFilterParam extends BaseFilterParam<PromotionTypeFilterParam> {
    
    private String typeCode;
    private String typeName;
    private String description;
    private Boolean isActive;
    private String keyword;
    
    public PromotionTypeFilterParam() {
        super();
        this.setSort("typeName");
        this.setDirection("ASC");
    }
}
