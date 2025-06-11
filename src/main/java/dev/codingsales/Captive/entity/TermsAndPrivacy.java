package dev.codingsales.Captive.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="TermsAndPrivacy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsAndPrivacy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String type;

    @Lob
    @Column(nullable = false, columnDefinition = "Text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public TermsAndPrivacy(String type, String content){
        this.type = type;
        this.content = content;
        this.lastUpdated = LocalDateTime.now();
    }
}
