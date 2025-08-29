package com.jotoai.voenix.shop.order.internal.assembler

import com.jotoai.voenix.shop.order.api.dto.AddressDto
import com.jotoai.voenix.shop.order.internal.entity.Address
import org.springframework.stereotype.Component

/**
 * Assembler for converting between Address entities and AddressDto objects.
 */
@Component
class AddressAssembler {
    /**
     * Converts an Address entity to AddressDto.
     */
    fun toDto(entity: Address): AddressDto =
        AddressDto(
            streetAddress1 = entity.streetAddress1,
            streetAddress2 = entity.streetAddress2,
            city = entity.city,
            state = entity.state,
            postalCode = entity.postalCode,
            country = entity.country,
        )

    /**
     * Converts an AddressDto to Address entity.
     */
    fun toEntity(dto: AddressDto): Address =
        Address(
            streetAddress1 = dto.streetAddress1,
            streetAddress2 = dto.streetAddress2,
            city = dto.city,
            state = dto.state,
            postalCode = dto.postalCode,
            country = dto.country,
        )
}
