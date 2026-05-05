package com.aurolink.sae.api

import com.aurolink.sae.domain.entities.SaeSolution
import jakarta.inject.Inject
import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import ai.timefold.solver.core.api.solver.SolverManager
import ai.timefold.solver.core.api.solver.SolverStatus
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Path("/api/solver")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SolverController {

    @Inject
    lateinit var solverManager: SolverManager<SaeSolution, String>

    /**
     * Cache en memoria para las soluciones finales resueltas por Timefold.
     * Clave: problemId (UUID), Valor: solución optimizada.
     * Nota: En producción debería persistirse en Redis o MongoDB.
     */
    private val solutionCache = ConcurrentHashMap<String, SaeSolution>()

    /**
     * POST /api/solver/optimize
     *
     * Recibe el problema (operadores, buses, assignments) y lanza la optimización
     * de forma asíncrona usando Timefold Solver.
     *
     * Retorna un problemId (UUID) para que el cliente haga polling en /status/{problemId}.
     */
    @POST
    @Path("/optimize")
    fun optimizeAssignments(unsolvedProblem: SaeSolution): Response {
        val problemId = UUID.randomUUID().toString()

        // Lanzar optimización asíncrona con listener para capturar la solución final
        solverManager.solve(problemId, unsolvedProblem) { finalSolution ->
            solutionCache[problemId] = finalSolution
        }

        return Response.accepted(mapOf("problemId" to problemId)).build()
    }

    /**
     * GET /api/solver/status/{problemId}
     *
     * Retorna el estado de la optimización y la mejor solución encontrada hasta el momento.
     *
     * Respuestas:
     * - 200 + { status: "SOLVING", partialSolution: {...} } → todavía resolviendo
     * - 200 + { status: "DONE", solution: {...} }           → terminó, solución final
     * - 404                                                 → problemId desconocido
     */
    @GET
    @Path("/status/{problemId}")
    fun getOptimizationStatus(@PathParam("problemId") problemId: String): Response {
        val solverStatus = solverManager.getSolverStatus(problemId)

        return when (solverStatus) {
            SolverStatus.SOLVING_ACTIVE -> {
                // Timefold sigue resolviendo; devolvemos la mejor solución parcial si ya hay una
                val partial = solutionCache[problemId]
                Response.ok(
                    mapOf(
                        "status" to "SOLVING",
                        "partialSolution" to partial,
                    )
                ).build()
            }

            SolverStatus.NOT_SOLVING -> {
                // El solver terminó — buscamos la solución guardada en el cache
                val finalSolution = solutionCache[problemId]
                if (finalSolution != null) {
                    Response.ok(
                        mapOf(
                            "status" to "DONE",
                            "solution" to finalSolution,
                        )
                    ).build()
                } else {
                    // problemId nunca existió o ya fue purgado del cache
                    Response.status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Problem ID not found: $problemId"))
                        .build()
                }
            }

            else -> {
                Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Unknown problem ID: $problemId"))
                    .build()
            }
        }
    }
}