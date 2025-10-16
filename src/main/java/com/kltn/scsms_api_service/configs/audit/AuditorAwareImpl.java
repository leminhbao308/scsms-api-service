package com.kltn.scsms_api_service.configs.audit;

import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        try {
            LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
            if (currentUser != null && currentUser.getEmail() != null) {
                return Optional.of(currentUser.getEmail());
            }
        } catch (Exception e) {
            return Optional.of("SYSTEM");
        }
        return Optional.of("SYSTEM");
    }
}
