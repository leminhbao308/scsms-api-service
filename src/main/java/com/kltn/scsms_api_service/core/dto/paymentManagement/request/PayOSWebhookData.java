package com.kltn.scsms_api_service.core.dto.paymentManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOSWebhookData {
    
    @JsonProperty("orderCode")
    private Long orderCode;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("accountNumber")
    private String accountNumber;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("transactionDateTime")
    private String transactionDateTime;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("paymentLinkId")
    private String paymentLinkId;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("counterAccountBankId")
    private String counterAccountBankId;
    
    @JsonProperty("counterAccountBankName")
    private String counterAccountBankName;
    
    @JsonProperty("counterAccountName")
    private String counterAccountName;
    
    @JsonProperty("counterAccountNumber")
    private String counterAccountNumber;
    
    @JsonProperty("virtualAccountName")
    private String virtualAccountName;
    
    @JsonProperty("virtualAccountNumber")
    private String virtualAccountNumber;
}
