package pl.edu.agh.votingapp.comunication.server

import android.util.Log
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import pl.edu.agh.votingapp.VotingType
import pl.edu.agh.votingapp.comunication.model.AnswerDto
import pl.edu.agh.votingapp.comunication.model.QuestionDto
import pl.edu.agh.votingapp.comunication.model.VoteResponseDto
import pl.edu.agh.votingapp.comunication.model.VotingDto
import java.util.concurrent.TimeUnit

import pl.edu.agh.votingapp.database.AppDatabase
import pl.edu.agh.votingapp.database.dao.AnswersDAO
import pl.edu.agh.votingapp.database.dao.QuestionDAO
import pl.edu.agh.votingapp.database.dao.VotingDAO
import pl.edu.agh.votingapp.database.entities.User
import pl.edu.agh.votingapp.votings.*

class VoteServer {

    private val db: AppDatabase = AppDatabase.getInstance()
    private val questionDAO: QuestionDAO = db.QuestionDAO()
    private val votingDao: VotingDAO = db.VotingDAO()
    private val answersDAO: AnswersDAO = db.AnswersDAO()
    private lateinit var server: ApplicationEngine

    fun startServer(createdPort: Int) {
        val voting = votingDao.getWithMaxId()
        val answers = answersDAO.loadAllAnswers(voting.votingId)
        val ongoingVoting: BaseVoting = when (voting.type) {
            VotingType.BORDA_COUNT -> BordaCount(db)
            VotingType.FIRST_PAST_THE_POST -> FirstPastThePostVoting(db)
            VotingType.SINGLE_NON_TRANSFERABLE_VOTE -> SingleNonTransferableVote(db)
            VotingType.TWO_ROUND_SYSTEM, VotingType.MAJORITY_VOTE -> MajorityVote(db)
            VotingType.NONE -> throw RuntimeException()
        }

        server = embeddedServer(Netty, createdPort) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()

                }
            }
            routing {
                get("/voting") {
                    val message = VotingDto(
                        voting.votingId,
                        voting.type,
                        answers.map {
                            AnswerDto(
                                it.answerId,
                                it.votingId,
                                it.questionId,
                                it.answerContent
                            )
                        },
                        voting.votingContent,
                        voting.winnersNb
                    )

                    Log.d("BallotBull", message.toString())
                    call.respond(message)
                }

                post("/voting") {
                    val request = call.receive<VoteResponseDto>()
                    Log.d("BallotBull", request.toString())
                    val userDto = request.userDto
                    val answersMap = request.answersIdToCount
                    ongoingVoting.addUser(
                        User(
                            votingId = voting.votingId,
                            userName = userDto.userName,
                            userCode = userDto.userCode
                        )
                    )
                    answersMap.forEach { (answerId, counter) ->
                        ongoingVoting.updateAnswerCount(
                            voting.votingId, userDto.userName, answerId, counter
                        )
                        call.respond(HttpStatusCode.OK, "Votes case")
                    }
                }
            }
        }.start(true)
    }

    fun stopServer() {
        server.stop(1, 1, TimeUnit.SECONDS)
    }

}