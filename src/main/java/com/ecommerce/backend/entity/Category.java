package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "market_id", nullable = false)
    @JsonIgnoreProperties({"categories"})
    private Market market;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"category", "market"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
