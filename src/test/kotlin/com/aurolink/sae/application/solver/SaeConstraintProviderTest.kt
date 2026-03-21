package com.aurolink.sae.application.solver

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import com.aurolink.sae.domain.entities.Bus
import com.aurolink.sae.domain.entities.Operator
import com.aurolink.sae.domain.entities.PlanningAssignment
import com.aurolink.sae.domain.entities.SaeSolution
import org.junit.jupiter.api.Test

class SaeConstraintProviderTest {

    private val constraintVerifier = ConstraintVerifier.build(
        SaeConstraintProvider(),
        SaeSolution::class.java,
        PlanningAssignment::class.java
    )

    @Test
    fun `operatorConflict penalizes when assignments overlap`() {
        val operator = Operator(
            operatorId = "OP1",
            employeeNumber = "123",
            firstName = "John",
            lastName = "Doe",
            status = "ACTIVE"
        )

        // Assignment 1: 1000 to 2000
        val assignment1 = PlanningAssignment().apply {
            id = "A1"
            this.operator = operator
            startTimestamp = 1000L
            endTimestamp = 2000L
        }

        // Assignment 2: 1500 to 2500 (Overlaps with A1)
        val assignment2 = PlanningAssignment().apply {
            id = "A2"
            this.operator = operator
            startTimestamp = 1500L
            endTimestamp = 2500L
        }

        // Assignment 3: 2500 to 3500 (Does not overlap with A2)
        val assignment3 = PlanningAssignment().apply {
            id = "A3"
            this.operator = operator
            startTimestamp = 2500L
            endTimestamp = 3500L
        }

        constraintVerifier.verifyThat(SaeConstraintProvider::operatorConflict)
            .given(assignment1, assignment2, assignment3)
            .penalizesBy(1) // Only A1 and A2 overlap, forming 1 pair
    }

    @Test
    fun `busConflict penalizes when assignments for same bus overlap`() {
        val bus1 = Bus(
            busId = "B1",
            internalNumber = "100",
            plate = "ABC",
            status = "ACTIVE",
            capacitySeated = 40
        )

        val dummyOperator = Operator("OP1", "123", "J", "D", "ACTIVE")
        
        val assignment1 = PlanningAssignment().apply {
            id = "A1"
            this.bus = bus1
            this.operator = dummyOperator
            startTimestamp = 500L
            endTimestamp = 1000L
        }

        val assignment2 = PlanningAssignment().apply {
            id = "A2"
            this.bus = bus1
            this.operator = dummyOperator
            startTimestamp = 800L
            endTimestamp = 1500L
        }

        val bus2 = Bus(
            busId = "B2",
            internalNumber = "101",
            plate = "XYZ",
            status = "ACTIVE",
            capacitySeated = 40
        )

        val assignment3 = PlanningAssignment().apply {
            id = "A3"
            this.bus = bus2
            this.operator = dummyOperator
            startTimestamp = 800L  // Overlaps with A2, but different bus!
            endTimestamp = 1500L
        }

        constraintVerifier.verifyThat(SaeConstraintProvider::busConflict)
            .given(assignment1, assignment2, assignment3)
            .penalizesBy(1) // Only A1 and A2 overlap on same bus
    }

    @Test
    fun `penalizeUnassignedOperator penalizes soft score per unassigned operator`() {
        // Operator is assigned
        val assignment1 = PlanningAssignment().apply {
            id = "A1"
            this.operator = Operator(
                operatorId = "OP1",
                employeeNumber = "123",
                firstName = "John",
                lastName = "Doe",
                status = "ACTIVE"
            )
        }

        // No operator assigned
        val assignment2 = PlanningAssignment().apply {
            id = "A2"
            this.operator = null
        }
        
        // No operator assigned
        val assignment3 = PlanningAssignment().apply {
            id = "A3"
            this.operator = null
        }

        constraintVerifier.verifyThat(SaeConstraintProvider::penalizeUnassignedOperator)
            .given(assignment1, assignment2, assignment3)
            .penalizesBy(2) // A2 and A3 have null operator
    }
}
