package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore

@PlanningSolution
class SaeSolution {

    // Rango de valores (ValueRange) que las ServiceAssignments usarán como posibles Operators
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "operatorRange")
    lateinit var operators: List<Operator>

    // Hechos del problema estáticos, los Buses disponibles
    @ProblemFactCollectionProperty
    lateinit var buses: List<Bus>

    // Lista de entidades a planificar (turnos a asignar a operadores)
    @PlanningEntityCollectionProperty
    lateinit var assignments: List<ServiceAssignment>

    // Score final de la solución luego de pasar por los Constraints
    @PlanningScore
    var score: HardSoftScore? = null

    // Constructor vacío
    constructor()

    constructor(
        operators: List<Operator>,
        buses: List<Bus>,
        assignments: List<ServiceAssignment>
    ) {
        this.operators = operators
        this.buses = buses
        this.assignments = assignments
    }
}