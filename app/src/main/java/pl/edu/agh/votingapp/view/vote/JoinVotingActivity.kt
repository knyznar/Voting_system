package pl.edu.agh.votingapp.view.vote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import pl.edu.agh.votingapp.R
import pl.edu.agh.votingapp.viewmodel.join.ServerData
import java.net.InetAddress

class JoinVotingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_voting)
        val name = intent.getStringExtra("NAME")
        val host = intent.getSerializableExtra("HOST") as InetAddress
        val port = intent.getIntExtra("PORT", 8080)

        val etFirstName: EditText = findViewById(R.id.etFirstName) as EditText
        val etLastName: EditText = findViewById(R.id.etLastName) as EditText
        val etUserCode: EditText = findViewById(R.id.etUserCode) as EditText

        val firstName = etFirstName.text
        val lastName = etLastName.text
        val userCode = etUserCode.text

//        TODO("TUTAJ POTRZEBNA JEST ZMIANA WIDOKU")
//        - pole na userCode : Long? i przekazanie go przez intent dalej
//        - wyświetlenie nazwy głosowania
//        - dodanie popupu "czy na pewno chcesz dołaczyć"

        val joinVotingButton: Button = findViewById(R.id.btn_login)
        joinVotingButton.setOnClickListener {
            Log.d("BallotBull: join voting", "Join voting with name ${name!!}")
            val intent = Intent(this, AnswerListActivity::class.java)
            ServerData.host = host
            ServerData.port = port
            startActivity(intent)
        }
    }
}
