package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.CenterSocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CenterSocialMediaRepository extends JpaRepository<CenterSocialMedia, UUID> {
    
    List<CenterSocialMedia> findByCenterCenterId(UUID centerId);
    
    List<CenterSocialMedia> findByCenterCenterIdAndIsActiveTrue(UUID centerId);
    
    @Query("SELECT csm FROM CenterSocialMedia csm WHERE csm.center.centerId = :centerId AND csm.platform = :platform")
    Optional<CenterSocialMedia> findByCenterAndPlatform(@Param("centerId") UUID centerId, @Param("platform") String platform);
    
    @Query("SELECT csm FROM CenterSocialMedia csm WHERE csm.center.centerId = :centerId AND csm.platform = :platform AND csm.isActive = true")
    Optional<CenterSocialMedia> findActiveByCenterAndPlatform(@Param("centerId") UUID centerId, @Param("platform") String platform);
    
    boolean existsByCenterCenterIdAndPlatformAndIsActiveTrue(UUID centerId, String platform);
}
