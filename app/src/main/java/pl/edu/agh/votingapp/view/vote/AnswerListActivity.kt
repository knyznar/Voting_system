package pl.edu.agh.votingapp.view.vote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.edu.agh.votingapp.R
import pl.edu.agh.votingapp.VotingType
import pl.edu.agh.votingapp.comunication.client.VotingController
import pl.edu.agh.votingapp.comunication.model.VotingDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnswerListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var myDataset: List<AnswerListElement>
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_list)

        val userName = intent.getStringExtra("userName")
        val userCode = intent.getStringExtra("userCode")
        confirmBtn = findViewById(R.id.confirm_btn)

        val votingConnector = VotingController().createConnector()

        votingConnector.loadVoting().enqueue(object : Callback<VotingDto> {
            override fun onResponse(call: Call<VotingDto>, response: Response<VotingDto>) {
                Log.d("BallotBull:", "Get voting response: " + response.body().toString())
                setData(response.body()!!)
            }

            override fun onFailure(call: Call<VotingDto>, t: Throwable) {
                Log.e("BallotBull", t.message!!)
            }
        })
    }

    private fun setData(votingDto: VotingDto) {
        myDataset = votingDto.answers.map {
            AnswerListElement(
                it.answerContent,
                0,
                it.questionId,
                it.answerId
            )
        }
        recyclerView = findViewById<RecyclerView>(R.id.candidate_list_rec_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AnswerListActivity)

            title = votingDto.votingContent

            when(votingDto.type) {

                VotingType.BORDA_COUNT -> adapter = BordaCountAdapter(myDataset)
                else -> {
                    adapter = CandidateListAdapter(myDataset)
                    confirmBtn.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showProgram() {
        val intent = Intent(this, CandidateProgramActivity::class.java)
        startActivity(intent)
    }
}