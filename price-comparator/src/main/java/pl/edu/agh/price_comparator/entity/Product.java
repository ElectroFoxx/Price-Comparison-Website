package pl.edu.agh.price_comparator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(name = "products")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "manufacturer_code", nullable = false, unique = true)
    @JsonProperty(value = "manufacturer_code")
    private String manufacturerCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonProperty("created_at")
    private Date createdAt;

    @Column(name = "emailed_at", nullable = false)
    @JsonIgnore
    private Date emailedAt;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<Price> prices;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference(value = "product-subscription")
    private List<Subscription> subscriptions;
}
