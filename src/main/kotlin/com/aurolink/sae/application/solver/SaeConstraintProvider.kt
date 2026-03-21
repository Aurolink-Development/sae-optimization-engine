package com.aurolink.sae.application.solver

import com.aurolink.sae.domain.entities.PlanningAssignment
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners

class SaeConstraintProvider : ConstraintProvider {
    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            operatorConflict(factory),
            busConflict(factory), // Assuming bus gets assigned too eventually, or standardizing
            penalizeUnassignedOperator(factory) // Soft Constraint
        )
    }

    /**
     * Hard constraint: Un operador no puede tener dos viajes asignados
     * al mismo tiempo (solapamiento de startTimestamp y endTimestamp).
     */
    fun operatorConflict(factory: ConstraintFactory): Constraint {
        return factory.forEachUniquePair(
            PlanningAssignment::class.java,
            Joiners.equal(PlanningAssignment::operator),
            Joiners.overlapping(PlanningAssignment::startTimestamp, PlanningAssignment::endTimestamp)
        )
            .penalize("Operator time conflict", HardSoftScore.ONE_HARD)
    }

    /**
     * Hard constraint: Un autobús no puede tener dos viajes asignados
     * al mismo tiempo (sí aplica la variable bus está definida como 'PlanningVariable').
     * Por ahora penaliza si el mismo bus está solapado (si en el futuro se optimiza también el bus).
     */
    fun busConflict(factory: ConstraintFactory): Constraint {
        return factory.forEachUniquePair(
            PlanningAssignment::class.java,
            Joiners.overlapping(PlanningAssignment::startTimestamp, PlanningAssignment::endTimestamp),
            // Filter only cases where bus is not null and bus matches
            Joiners.filtering { a1, a2 -> a1.bus != null && a2.bus != null && a1.bus?.busId == a2.bus?.busId }
        )
            .penalize("Bus time conflict", HardSoftScore.ONE_HARD)
    }

    /**
     * Soft constraint: Minimizar los viajes que se quedan sin operador asignado.
     * De esta forma, el motor prefiere soluciones donde más viajes logran emparejarse.
     */
    fun penalizeUnassignedOperator(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingNullVars(PlanningAssignment::class.java)
            .filter { it.operator == null }
            .penalize("Unassigned operator", HardSoftScore.ONE_SOFT)
    }
}