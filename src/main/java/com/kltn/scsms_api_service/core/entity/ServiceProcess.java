package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_process", schema = "dev")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ServiceProcess extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    
    @Column(name = "name", nullable = false, length = 150)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // Tổng thời gian dự kiến (phút)
    
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Quan hệ với ServiceProcessStep
    @OneToMany(mappedBy = "serviceProcess", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @OrderBy("stepOrder ASC")
    private List<ServiceProcessStep> processSteps = new ArrayList<>();
    
    // Quan hệ với Service (nhiều service có thể sử dụng cùng 1 process)
    @OneToMany(mappedBy = "serviceProcess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Service> services = new ArrayList<>();
    
    
    /**
     * Tính tổng thời gian dự kiến từ các bước
     */
    public Integer calculateEstimatedDuration() {
        if (processSteps == null || processSteps.isEmpty()) {
            return estimatedDuration;
        }
        
        return processSteps.stream()
                .filter(step -> step.getEstimatedTime() != null)
                .mapToInt(ServiceProcessStep::getEstimatedTime)
                .sum();
    }
    
    /**
     * Cập nhật thời gian dự kiến
     */
    public void updateEstimatedDuration() {
        this.estimatedDuration = calculateEstimatedDuration();
    }
    
    /**
     * Lấy số lượng bước trong quy trình
     */
    public int getStepCount() {
        return processSteps != null ? processSteps.size() : 0;
    }
    
    /**
     * Kiểm tra xem quy trình có bước nào không
     */
    public boolean hasSteps() {
        return processSteps != null && !processSteps.isEmpty();
    }
    
    /**
     * Lấy bước đầu tiên
     */
    public ServiceProcessStep getFirstStep() {
        if (processSteps == null || processSteps.isEmpty()) {
            return null;
        }
        return processSteps.get(0);
    }
    
    /**
     * Lấy bước cuối cùng
     */
    public ServiceProcessStep getLastStep() {
        if (processSteps == null || processSteps.isEmpty()) {
            return null;
        }
        return processSteps.get(processSteps.size() - 1);
    }
}
