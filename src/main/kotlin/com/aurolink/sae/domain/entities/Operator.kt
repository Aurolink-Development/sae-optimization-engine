package com.aurolink.sae.domain.entities

import org.optaplanner.core.api.domain.lookup.PlanningId
import kotlin.time.Instant

data class Operator (
    @PlanningId
    val operatorId: String,
    val employeeNumber: String,
    val firstName: String,
    val lastName: String,
    val status: String,
    val licenseExpiresAt: Instant? = null
)