package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable

@PlanningEntity
class PlanningAssignment {
    @PlanningId
    lateinit var id: String

    var bus: Bus? = null
    var routeId: String? = null
    var moduleId: String? = null

    @PlanningVariable(valueRangeProviderRefs = ["operatorRange"])
    var operator: Operator? = null

    var isPinned: Boolean = false

    var startTimestamp: Long = 0
    var endTimestamp: Long = 0
}