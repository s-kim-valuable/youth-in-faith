package com.sekaguchi.youthinfaith.game.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sekaguchi.youthinfaith.game.domain.GameConfig
import com.sekaguchi.youthinfaith.game.domain.GameState
import com.sekaguchi.youthinfaith.game.domain.TEAM_COLORS
import com.sekaguchi.youthinfaith.game.domain.TeamConfig
import com.sekaguchi.youthinfaith.game.domain.TeamState
import com.sekaguchi.youthinfaith.game.domain.defaultTeams
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GameRepositoryImpl(private val database: FirebaseDatabase) : GameRepository {

    override fun configFlow(): Flow<GameConfig> = callbackFlow {
        val ref = database.getReference("config")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teamCount = snapshot.child("team_count").getValue(Int::class.java)
                if (teamCount == null) {
                    trySend(GameConfig(defaultTeams()))
                    return
                }
                val namesSnapshot = snapshot.child("team_names")
                val teams = (1..teamCount.coerceIn(2, 6)).map { i ->
                    val id = "team$i"
                    val name = namesSnapshot.child(id).getValue(String::class.java) ?: "${i}팀"
                    TeamConfig(id, name, TEAM_COLORS[i - 1])
                }
                trySend(GameConfig(teams))
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun gameStateFlow(): Flow<GameState> = callbackFlow {
        val ref = database.getReference("game")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(
                    GameState(
                        correctAnswer = snapshot.child("correct_answer").getValue(String::class.java) ?: "",
                        isRevealed = snapshot.child("is_revealed").getValue(Boolean::class.java) ?: false
                    )
                )
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun teamsFlow(): Flow<Map<String, TeamState>> = callbackFlow {
        val ref = database.getReference("teams")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teams = mutableMapOf<String, TeamState>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    teams[id] = TeamState(
                        input = child.child("input").getValue(String::class.java) ?: "",
                        status = child.child("status").getValue(String::class.java) ?: "typing"
                    )
                }
                trySend(teams)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun saveConfig(config: GameConfig) {
        database.getReference("config/team_count").setValue(config.teams.size).await()
        database.getReference("config/team_names").setValue(
            config.teams.associate { it.id to it.displayName }
        ).await()
        // 旧チームのデータを破棄してから新チームを書き込む（チーム数減少時の残存対策）
        database.getReference("teams").setValue(
            config.teams.associate { it.id to mapOf("input" to "", "status" to "typing") }
        ).await()
    }

    override suspend fun updateTeamInput(team: String, input: String) {
        database.getReference("teams/$team/input").setValue(input).await()
    }

    override suspend fun submitTeam(team: String) {
        database.getReference("teams/$team/status").setValue("submitted").await()
    }

    override suspend fun setCorrectAnswer(answer: String) {
        database.getReference("game/correct_answer").setValue(answer).await()
    }

    override suspend fun revealAnswer() {
        database.getReference("game/is_revealed").setValue(true).await()
    }

    override suspend fun resetGame(teamIds: List<String>) {
        database.getReference("game/is_revealed").setValue(false).await()
        teamIds.forEach { id ->
            val ref = database.getReference("teams/$id")
            ref.child("input").setValue("").await()
            ref.child("status").setValue("typing").await()
        }
    }

    override suspend fun getHostPin(): String = runCatching {
        val snapshot = database.getReference("config/host_pin").get().await()
        snapshot.getValue(String::class.java) ?: DEFAULT_HOST_PIN
    }.getOrDefault(DEFAULT_HOST_PIN)

    override suspend fun setHostPin(pin: String) {
        database.getReference("config/host_pin").setValue(pin).await()
    }

    private companion object {
        const val DEFAULT_HOST_PIN = "1234"
    }
}
