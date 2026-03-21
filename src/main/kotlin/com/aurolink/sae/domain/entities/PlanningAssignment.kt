package com.aurolink.sae.domain.entities

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity
class PlanningAssignment {
    lateinit var id: String

    var bus: Bus? = null
    var routeId: String? = null
    var moduleId: String? = null

    @PlanningVariable(valueRangeProviderRefs = ["operatorRange"])
    val operator: Operator? = null

    var isPinned: Boolean = false

    var startTimestamp: Long = 0
    var endTimestamp: Long = 0
}