package com.aurolink.sae.application.solver

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import com.aurolink.sae.domain.entities.Operator
import com.aurolink.sae.domain.entities.ServiceAssignment
import com.aurolink.sae.domain.entities.SaeSolution
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SaeConstraintProviderTest {

    private val constraintVerifier = ConstraintVerifier.build(
        SaeConstraintProvider(),
        SaeSolution::class.java,
        ServiceAssignment::class.java
    )

    @Test
    fun `operatorConflict penalizes when assignments overlap`() {
        val operator = Operator(
            operatorId = "OP1",
            employeeNumber = "123",
            homeModuleId = "MOD1",
            status = "ACTIVE"
        )

        // Assignment 1: 10:00 to 12:00
        val assignment1 = ServiceAssignment(
            assignmentId = "A1",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 10, 0),
            endDateTime = LocalDateTime.of(2026, 3, 31, 12, 0),
            operator = operator
        )

        // Assignment 2: 11:00 to 13:00 (Overlaps with A1)
        val assignment2 = ServiceAssignment(
            assignmentId = "A2",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 11, 0),
            endDateTime = LocalDateTime.of(2026, 3, 31, 13, 0),
            operator = operator
        )

        // Assignment 3: 13:01 to 14:00 (Does not overlap with A2)
        val assignment3 = ServiceAssignment(
            assignmentId = "A3",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 13, 1),
            endDateTime = LocalDateTime.of(2026, 3, 31, 14, 0),
            operator = operator
        )

        constraintVerifier.verifyThat(SaeConstraintProvider::operatorConflict)
            .given(assignment1, assignment2, assignment3)
            .penalizesBy(1) // Only A1 and A2 overlap
    }

    @Test
    fun `matchingModuleId penalizes when operator homeModuleId does not match service moduleId`() {
        val operator1 = Operator(
            operatorId = "OP1",
            employeeNumber = "123",
            homeModuleId = "MOD1",
            status = "ACTIVE"
        )
        val operator2 = Operator(
            operatorId = "OP2",
            employeeNumber = "124",
            homeModuleId = "MOD2", // Different module
            status = "ACTIVE"
        )

        // Matches
        val assignment1 = ServiceAssignment(
            assignmentId = "A1",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 10, 0),
            endDateTime = LocalDateTime.of(2026, 3, 31, 12, 0),
            operator = operator1
        )

        // Does not match (MOD1 != MOD2)
        val assignment2 = ServiceAssignment(
            assignmentId = "A2",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 13, 0),
            endDateTime = LocalDateTime.of(2026, 3, 31, 15, 0),
            operator = operator2
        )

        constraintVerifier.verifyThat(SaeConstraintProvider::matchingModuleId)
            .given(assignment1, assignment2)
            .penalizesBy(1) // Only A2 breaks the constraint
    }

    @Test
    fun `minimizeWaitTime penalizes gap in minutes`() {
        val operator = Operator(
            operatorId = "OP1",
            employeeNumber = "123",
            homeModuleId = "MOD1",
            status = "ACTIVE"
        )

        // Assignment 1 ends at 12:00
        val assignment1 = ServiceAssignment(
            assignmentId = "A1",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 10, 0),
            endDateTime = LocalDateTime.of(2026, 3, 31, 12, 0),
            operator = operator
        )

        // Assignment 2 starts at 12:30. The gap is 30 minutes.
        val assignment2 = ServiceAssignment(
            assignmentId = "A2",
            moduleId = "MOD1",
            startDateTime = LocalDateTime.of(2026, 3, 31, 12, 30),
            endDateTime = LocalDateTime.of(2026, 3, 31, 14, 0),
            operator = operator
        )

        constraintVerifier.verifyThat(SaeConstraintProvider::minimizeWaitTime)
            .given(assignment1, assignment2)
            .penalizesBy(30)
    }
}
