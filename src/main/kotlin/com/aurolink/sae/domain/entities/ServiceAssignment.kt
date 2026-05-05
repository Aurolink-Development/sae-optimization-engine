package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import java.time.LocalDateTime

@PlanningEntity
class ServiceAssignment {

    @PlanningId
    lateinit var assignmentId: String

    lateinit var moduleId: String
    
    // Asumiendo rango de tiempo del servicio / turno
    lateinit var startDateTime: LocalDateTime
    lateinit var endDateTime: LocalDateTime
    
    // Este campo se asigna asíncronamente en vivo cuando el operador entra al bus
    // Por lo tanto, no es variable de decisión (sino null o estático en el Solver)
    var bus: Bus? = null

    // Variable de decisión: a qué operador se le asigna este servicio
    @PlanningVariable(valueRangeProviderRefs = ["operatorRange"])
    var operator: Operator? = null

    // Constructor vacío necesario para Timefold
    constructor()

    constructor(
        assignmentId: String,
        moduleId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        bus: Bus? = null,
        operator: Operator? = null
    ) {
        this.assignmentId = assignmentId
        this.moduleId = moduleId
        this.startDateTime = startDateTime
        this.endDateTime = endDateTime
        this.bus = bus
        this.operator = operator
    }

    override fun toString(): String {
        return "ServiceAssignment(id=$assignmentId, module=$moduleId, start=$startDateTime, end=$endDateTime, bus=$bus, operator=$operator)"
    }
}
