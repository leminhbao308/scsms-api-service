package com.kltn.scsms_api_service.core.dto.productAttributeManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductAttributeFilterParam extends BaseFilterParam<ProductAttributeFilterParam> {
    
    private String attributeName;
    private String attributeCode;
    private String dataType;
    private Boolean isRequired;
    private Boolean isActive;
    
    @Override
    protected String getDefaultSortField() {
        return "attributeName";
    }
    
    @Override
    protected void standardizeSpecificFields(ProductAttributeFilterParam request) {
        request.setAttributeName(trimAndNullify(request.getAttributeName()));
        request.setAttributeCode(trimAndNullify(request.getAttributeCode()));
        request.setDataType(trimAndNullify(request.getDataType()));
    }
    
    public static ProductAttributeFilterParam standardize(ProductAttributeFilterParam filterParam) {
        if (filterParam == null) {
            filterParam = new ProductAttributeFilterParam();
        }
        return filterParam.standardizeFilterRequest(filterParam);
    }
}
