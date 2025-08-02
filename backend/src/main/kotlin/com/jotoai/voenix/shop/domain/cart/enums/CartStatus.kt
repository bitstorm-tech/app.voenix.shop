package com.jotoai.voenix.shop.domain.cart.enums

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class CartStatus(
    @JsonValue val value: String,
) {
    ACTIVE("active"), // Cart is currently active and being used
    ABANDONED("abandoned"), // Cart was abandoned by the user
    CONVERTED("converted"), // Cart was converted to an order
}

@Converter(autoApply = true)
class CartStatusConverter : AttributeConverter<CartStatus, String> {
    override fun convertToDatabaseColumn(attribute: CartStatus?): String? = attribute?.value

    override fun convertToEntityAttribute(dbData: String?): CartStatus? =
        when (dbData) {
            "active" -> CartStatus.ACTIVE
            "abandoned" -> CartStatus.ABANDONED
            "converted" -> CartStatus.CONVERTED
            else -> null
        }
}
