package za.co.capitec.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant", nullable = false, length = 255)
    private String merchant;

    @Column(name = "country", nullable = false, length = 20)
    private String country;

    @Column(name = "account_home_country", nullable = false, length = 20)
    private String accountHomeCountry;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

}
