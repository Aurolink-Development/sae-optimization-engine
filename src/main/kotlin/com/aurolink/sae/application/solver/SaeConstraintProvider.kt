package com.aurolink.sae.application.solver

import com.aurolink.sae.domain.entities.PlanningAssignment
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.api.score.stream.Constraint
import org.optaplanner.core.api.score.stream.ConstraintFactory
import org.optaplanner.core.api.score.stream.ConstraintProvider
import org.optaplanner.core.api.score.stream.Joiners

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
    private fun operatorConflict(factory: ConstraintFactory): Constraint {
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
    private fun busConflict(factory: ConstraintFactory): Constraint {
        return factory.forEachUniquePair(
            PlanningAssignment::class.java,
            // Filter only cases where bus is not null
            Joiners.filtering { a1, a2 -> a1.bus != null && a2.bus != null && a1.bus?.id == a2.bus?.id },
            Joiners.overlapping(PlanningAssignment::startTimestamp, PlanningAssignment::endTimestamp)
        )
            .penalize("Bus time conflict", HardSoftScore.ONE_HARD)
    }

    /**
     * Soft constraint: Minimizar los viajes que se quedan sin operador asignado.
     * De esta forma, el motor prefiere soluciones donde más viajes logran emparejarse.
     */
    private fun penalizeUnassignedOperator(factory: ConstraintFactory): Constraint {
        return factory.forEach(PlanningAssignment::class.java)
            .filter { it.operator == null }
            .penalize("Unassigned operator", HardSoftScore.ONE_SOFT)
    }
}