package com.example.complaints.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "complainant"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private String complainant;

    @Column(nullable = false)
    private String country;

    @Column(name = "complaint_counter", nullable = false)
    private int counter;
}
