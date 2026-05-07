package com.aurolink.sae.application.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import com.aurolink.sae.domain.entities.ServiceAssignment
import java.time.temporal.ChronoUnit

class SaeConstraintProvider : ConstraintProvider {

    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            operatorConflict(factory),
            busConflict(factory),
            matchingModuleId(factory),
            minimizeWaitTime(factory),
            unassignedBus(factory),
            unassignedOperator(factory),
            debugPrint(factory)
        )
    }

    /**
     * Hard Constraint 1: Un Operador no puede tener servicios (turnos) con tiempos superpuestos.
     */
    fun operatorConflict(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { it.operator != null }
            .join(
                factory.forEachIncludingUnassigned(ServiceAssignment::class.java).filter { it.operator != null },
                Joiners.equal(ServiceAssignment::operator),
                Joiners.overlapping(ServiceAssignment::startDateTime, ServiceAssignment::endDateTime),
                Joiners.lessThan(ServiceAssignment::assignmentId)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("operatorConflict")
    }

    /**
     * Hard Constraint 1.5: Un Autobús no puede tener servicios (turnos) con tiempos superpuestos.
     */
    fun busConflict(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { it.bus != null }
            .join(
                factory.forEachIncludingUnassigned(ServiceAssignment::class.java).filter { it.bus != null },
                Joiners.equal(ServiceAssignment::bus),
                Joiners.overlapping(ServiceAssignment::startDateTime, ServiceAssignment::endDateTime),
                Joiners.lessThan(ServiceAssignment::assignmentId)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("busConflict")
    }

    /**
     * Hard Constraint 2: El operador y el servicio deben pertenecer al mismo módulo/patio.
     */
    fun matchingModuleId(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            // Filtramos asginaciones cuyo operator ya fue definido pero su homeModuleId no coincide 
            // con el moduleId del servicio asignado.
            .filter { assignment ->
                val op = assignment.operator
                op != null && op.homeModuleId != assignment.moduleId
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("matchingModuleId")
    }

    /**
     * Soft Constraint 1: Minimizar el tiempo de inactividad o espera ("Wait Time")
     * entre servicios para un mismo conductor.
     */
    fun minimizeWaitTime(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { it.bus != null }
            .join(
                factory.forEachIncludingUnassigned(ServiceAssignment::class.java).filter { it.bus != null },
                Joiners.equal(ServiceAssignment::bus),
                Joiners.lessThan(ServiceAssignment::endDateTime, ServiceAssignment::startDateTime)
            )
            .penalize(HardSoftScore.ONE_SOFT) { a1, a2 ->
                ChronoUnit.MINUTES.between(a1.endDateTime, a2.startDateTime).toInt()
            }
            .asConstraint("minimizeWaitTime")
    }

    /**
     * Hard Constraint 3: Penalizar fuertemente si un viaje se queda sin autobús.
     * Esto fuerza al motor a intentar asignar SIEMPRE un autobús si hay disponibles.
     */
    fun unassignedBus(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { it.bus == null }
            .penalize(HardSoftScore.ofHard(10))
            .asConstraint("unassignedBus")
    }

    /**
     * Soft Constraint 2: Penalizar si un viaje se queda sin operador.
     * Si no hay operadores (como ahora), simplemente absorberá los puntos negativos sin abortar.
     */
    fun unassignedOperator(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { it.operator == null }
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("unassignedOperator")
    }

    fun debugPrint(factory: ConstraintFactory): Constraint {
        return factory.forEachIncludingUnassigned(ServiceAssignment::class.java)
            .filter { 
                println("DEBUG-TIMEFOLD: trip=${it.assignmentId}, start=${it.startDateTime}, end=${it.endDateTime}, bus=${it.bus}, op=${it.operator}")
                false 
            }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("debugPrint")
    }
}