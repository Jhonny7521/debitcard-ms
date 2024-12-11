package com.bm_nttdata.debitcard_ms.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad que representa a las tarjetas de débito en el sistema bancario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "debit-card")
public class DebitCard {

    private String id;
    private String cardNumber;
    private String customerId;
    private String primaryAccountId;
    private List<String> associatedAccountIds;
    private String expirationDate;
    private String ccvCode;
    private String cardPin;
    private LocalDateTime creationDate;
    private boolean active;

}


