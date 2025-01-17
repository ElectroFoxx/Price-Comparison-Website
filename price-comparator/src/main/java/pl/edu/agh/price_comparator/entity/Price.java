package pl.edu.agh.price_comparator.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "prices")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at", nullable = false)
    @JsonProperty(value = "created_at")
    private Date createdAt;
   
    @Column(name = "price_xkom")
    @JsonProperty(value = "price_xkom")
    private int priceXkom;

    @Column(name = "price_morele")
    @JsonProperty(value = "price_morele")
    private int priceMorele;

    @Column(name = "price_media_expert")
    @JsonProperty(value = "price_media_expert")
    private int priceMediaExpert;
}
