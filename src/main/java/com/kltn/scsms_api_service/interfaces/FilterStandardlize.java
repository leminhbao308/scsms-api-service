package com.kltn.scsms_api_service.interfaces;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;

public interface FilterStandardlize<T> {
    T standardizeFilterRequest(T request);
}
