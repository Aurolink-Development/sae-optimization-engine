package com.aurolink.sae.api

import com.aurolink.sae.domain.entities.SaeSolution
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.optaplanner.core.api.solver.SolverManager
import java.util.UUID

@Path("/api/solver")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SolverController {

    @Inject
    lateinit var solverManager: SolverManager<SaeSolution, String>

    @POST
    @Path("/optimize")
    fun optimizeAssignments(unsolvedProblem: SaeSolution): String {
        val problemId = UUID.randomUUID().toString()
        
        // Asynchronous call (Non-blocking)
        solverManager.solve(problemId, unsolvedProblem)
        
        return problemId // Client receives jobId and polls the status
    }

    @jakarta.ws.rs.GET
    @Path("/status/{problemId}")
    fun getOptimizationStatus(@jakarta.ws.rs.PathParam("problemId") problemId: String): SaeSolution? {
        // Returns the best solution found so far (or final if completed)
        return solverManager.getSolverStatus(problemId)?.let { status ->
            if (status == org.optaplanner.core.api.solver.SolverStatus.NOT_SOLVING) {
               // In a real app we'd fetch the final solution from a DB/Cache here.
               // For demo purposes solverManager might not retain it depending on config after it stops:
               null 
            } else {
               // Still solving? Could throw 202 Accepted or return partial
               null
            }
        }
    }
}