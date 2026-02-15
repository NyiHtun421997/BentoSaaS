package com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response;

import com.nyihtuun.bentosystem.domain.valueobject.status.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InvoiceResponseDto {
    @EqualsAndHashCode.Include
    private final UUID invoiceId;
    private final UUID subscriptionId;
    private final UUID userId;
    private final UUID providedUserId;
    private final InvoiceStatus invoiceStatus;
    private final BigDecimal amount;
    private final List<UUID> subscribedMealIds;
    private final ZonedDateTime issuedAt;
    private final ZonedDateTime updatedAt;
    private final ZonedDateTime paidAt;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
}
