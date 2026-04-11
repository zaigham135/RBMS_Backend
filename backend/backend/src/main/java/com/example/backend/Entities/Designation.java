package com.example.backend.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "designations")
@Getter
@Setter
@NoArgsConstructor
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** MANAGER, EMPLOYEE, or null = applicable to both */
    @Column(name = "applicable_role")
    private String applicableRole;

    public Designation(String name, String applicableRole) {
        this.name = name;
        this.applicableRole = applicableRole;
    }
}
