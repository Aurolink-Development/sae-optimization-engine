package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.lookup.PlanningId

data class Operator(
    @PlanningId
    val operatorId: String,
    val employeeNumber: String,
    val homeModuleId: String?,
    val status: String
) {
    override fun toString(): String {
        return "Operator(id=$operatorId, employeeNumber=$employeeNumber, module=$homeModuleId)"
    }
}