package com.aurolink.sae.domain.entities

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore

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