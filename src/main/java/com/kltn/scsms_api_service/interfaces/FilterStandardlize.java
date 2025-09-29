package com.kltn.scsms_api_service.interfaces;

public interface FilterStandardlize<T> {
    T standardizeFilterRequest(T request);
}
