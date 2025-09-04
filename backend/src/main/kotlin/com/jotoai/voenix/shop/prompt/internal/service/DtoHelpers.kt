package com.jotoai.voenix.shop.prompt.internal.service

/** Small helper to reduce repeated requireNotNull boilerplate in assemblers. */
fun <T : Any> idOrThrow(
    id: T?,
    label: String,
): T = requireNotNull(id) { "$label ID cannot be null when converting to DTO" }
