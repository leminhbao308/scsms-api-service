package com.kltn.scsms_api_service.core.dto.pricingManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceBookInfoDto extends AuditDto {
    
    private String id;
    
    private String code;
    
    private String name;
    
    private String currency;
    
    @JsonProperty("valid_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime validFrom;
    
    @JsonProperty("valid_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime validTo;
    
    private List<PriceBookItemInfoDto> items;
}
