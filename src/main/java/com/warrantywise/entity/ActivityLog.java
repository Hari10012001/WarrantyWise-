package com.warrantywise.entity;

import com.warrantywise.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name="idx_activity_user", columnList="user_id, created_at"),
    @Index(name="idx_activity_entity", columnList="entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private ActionType action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
