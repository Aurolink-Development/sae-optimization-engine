package com.aurolink.sae.domain.entities

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore

@PlanningSolution
class SaeSolution {
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "operatorRange")
    lateinit var operator: List<Operator>

    @ProblemFactCollectionProperty
    lateinit var buses: List<Bus>

    @PlanningEntityCollectionProperty
    lateinit var assignments: List<PlanningAssignment>

    @PlanningScore
    var score: HardSoftScore? = null
}