package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.lookup.PlanningId

data class Bus(
    @PlanningId
    val busId: String,
    val internalNumber: String,
    val status: String
) {
    override fun toString(): String {
        return "Bus(id=$busId, internalNumber=$internalNumber)"
    }
}