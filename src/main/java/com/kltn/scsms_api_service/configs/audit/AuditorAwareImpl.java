package com.kltn.scsms_api_service.configs.audit;

import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        try {
            LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getFullName() != null)
                    return Optional.of(currentUser.getFullName());
                else
                    return Optional.of(currentUser.getSub());
            }
        } catch (Exception e) {
            log.error("Error retrieving current auditor: {}", e.getMessage());
            return Optional.of("SYSTEM");
        }
        return Optional.of("SYSTEM");
    }
}
