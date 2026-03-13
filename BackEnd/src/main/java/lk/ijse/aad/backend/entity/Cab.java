package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cab")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Cab {

    // ⚠️ IMPORTANT: Field order must match the JPQL constructor query in CabRepository
    // ORDER: cabId, cabName, cabModel, cabPlate, cabType,
    //        pricePerKm, seatCount, cabDescription, available, cabImage, imageType

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cab_id")
    private Integer cabId;

    @Column(name = "cab_name", nullable = false)
    private String cabName;

    @Column(name = "cab_model", nullable = false)
    private String cabModel;

    @Column(name = "cab_plate", nullable = false, unique = true)
    private String cabPlate;

    @Column(name = "cab_type", nullable = false)
    private String cabType;

    @Column(name = "price_per_km", nullable = false)
    private double pricePerKm;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Column(name = "cab_description")
    private String cabDescription;

    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Lob
    @Column(name = "cab_image", columnDefinition = "LONGBLOB")
    private byte[] cabImage;

    @Column(name = "image_type")
    private String imageType;
}