package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonClassDescription("Lấy danh sách tất cả các chi nhánh đang hoạt động. KHÔNG CẦN truyền bất kỳ tham số nào - tất cả fields đều optional và có thể bỏ qua.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBranchesRequest {

    @JsonProperty("_placeholder")
    private String placeholder;
}

