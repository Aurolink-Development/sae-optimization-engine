package com.aurolink.sae.domain.entities

import org.optaplanner.core.api.domain.lookup.PlanningId

data class Bus (
    @PlanningId
    val busId: String,
    val internalNumber: String,
    val plate: String?,
    val status: String,
    val capacitySeated: Int,
    val fuelLevelPct: Double? = null,
    val lat: Double? = null,
    val lon: Double? = null
)