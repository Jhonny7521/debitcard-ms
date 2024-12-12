package com.bm_nttdata.debitcard_ms.mapper;

import com.bm_nttdata.debitcard_ms.entity.DebitCard;
import com.bm_nttdata.debitcard_ms.model.DebitCardRequestDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardResponseDto;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;

/**
 * Interfaz de mapeo para la conversión entre objetos relacionados con Customer.
 * Utiliza MapStruct para la implementación automática de las conversiones.
 */
@Mapper(componentModel = "spring")
public interface DebitCardMapper {

    /**
     * Convierte el DebitCardRequestDto a una entidad DebitCard.
     *
     * @param debitCardRequest DTO con los datos de solicitud de una tarjeta de débito
     * @return Entidad de tarjeta de débito
     */
    DebitCard debitCardRequestToDebitCardEntity(DebitCardRequestDto debitCardRequest);

    /**
     * Convierte una entidad DebitCard a DebitCardResponseDto.
     *
     * @param debitCard Entidad de tarjeta de débito
     * @return DTO de respuesta con los datos de la tarjeta de débito
     */
    DebitCardResponseDto debitCardEntityToDebitCardDto(DebitCard debitCard);

    /**
     * Convierte un LocalDateTime a OffsetDateTime en UTC.
     *
     * @param localDateTime Fecha y hora local
     * @return Fecha y hora con zona horaria UTC
     */
    default OffsetDateTime map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    /**
     * Convierte un OffsetDateTime a LocalDateTime.
     *
     * @param offsetDateTime Fecha y hora con zona horaria
     * @return Fecha y hora local
     */
    default LocalDateTime map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDateTime();
    }
}
