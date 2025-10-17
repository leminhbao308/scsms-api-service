package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_process_step", schema = "dev")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ServiceProcessStep extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ServiceProcess serviceProcess;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Column(name = "name", nullable = false, length = 150)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Loại bỏ estimated_time - thời gian được quản lý ở Service level
    
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;
    
    // Loại bỏ mối quan hệ với ServiceProcessStepProduct
    // ServiceProcessStep giờ chỉ chứa thông tin cơ bản về bước
    
    // Các method liên quan đến product đã được loại bỏ
    // ServiceProcessStep giờ chỉ quản lý thông tin cơ bản về bước
    
    /**
     * Kiểm tra xem bước này có thể bỏ qua không
     */
    public boolean canSkip() {
        return !isRequired;
    }
    
    /**
     * Lấy bước tiếp theo trong quy trình
     */
    public ServiceProcessStep getNextStep() {
        if (serviceProcess == null || serviceProcess.getProcessSteps() == null) {
            return null;
        }
        
        List<ServiceProcessStep> steps = serviceProcess.getProcessSteps();
        int currentIndex = steps.indexOf(this);
        
        if (currentIndex == -1 || currentIndex >= steps.size() - 1) {
            return null;
        }
        
        return steps.get(currentIndex + 1);
    }
    
    /**
     * Lấy bước trước đó trong quy trình
     */
    public ServiceProcessStep getPreviousStep() {
        if (serviceProcess == null || serviceProcess.getProcessSteps() == null) {
            return null;
        }
        
        List<ServiceProcessStep> steps = serviceProcess.getProcessSteps();
        int currentIndex = steps.indexOf(this);
        
        if (currentIndex <= 0) {
            return null;
        }
        
        return steps.get(currentIndex - 1);
    }
    
    /**
     * Kiểm tra xem đây có phải bước đầu tiên không
     */
    public boolean isFirstStep() {
        return stepOrder != null && stepOrder == 1;
    }
    
    /**
     * Kiểm tra xem đây có phải bước cuối cùng không
     */
    public boolean isLastStep() {
        if (serviceProcess == null || serviceProcess.getProcessSteps() == null) {
            return false;
        }
        
        return stepOrder != null && stepOrder.equals(serviceProcess.getProcessSteps().size());
    }
}