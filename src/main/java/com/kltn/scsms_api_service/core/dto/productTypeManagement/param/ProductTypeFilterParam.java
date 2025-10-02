package com.kltn.scsms_api_service.core.dto.productTypeManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductTypeFilterParam extends BaseFilterParam<ProductTypeFilterParam> {
    
    private String productTypeName;
    private String productTypeCode;
    private String categoryName;
    private Boolean isActive;
    
    @Override
    protected String getDefaultSortField() {
        return "productTypeName";
    }
    
    @Override
    protected void standardizeSpecificFields(ProductTypeFilterParam request) {
        request.setProductTypeName(trimAndNullify(request.getProductTypeName()));
        request.setProductTypeCode(trimAndNullify(request.getProductTypeCode()));
        request.setCategoryName(trimAndNullify(request.getCategoryName()));
    }
    
    public static ProductTypeFilterParam standardize(ProductTypeFilterParam filterParam) {
        if (filterParam == null) {
            filterParam = new ProductTypeFilterParam();
        }
        return filterParam.standardizeFilterRequest(filterParam);
    }
}
