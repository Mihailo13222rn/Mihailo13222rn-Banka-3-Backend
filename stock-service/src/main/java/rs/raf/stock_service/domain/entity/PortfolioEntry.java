package rs.raf.stock_service.domain.entity;
import lombok.*;
import rs.raf.stock_service.domain.enums.ListingType;
import rs.raf.stock_service.domain.enums.OptionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(optional = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    private Integer amount;

    private BigDecimal averagePrice;

    private Boolean isPublic = false;      // Za akcije i futures (OTC)

    private Boolean inTheMoney = false;    // Za opcije

    private Boolean used = false;          // Za opcije

    private LocalDateTime lastModified;

    // moze se npr i dodati i currentProfit, ali bolje ga izračunavati na getPortfolio pozivu
}
