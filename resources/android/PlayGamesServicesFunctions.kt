package com.nativephp.plugins.playgamesservices

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.Player
import com.nativephp.mobile.bridge.BridgeFunction
import com.nativephp.mobile.bridge.BridgeResponse
import com.nativephp.mobile.utils.NativeActionCoordinator
import org.json.JSONObject

private const val TAG = "PlayGamesServices"
private val mainHandler = Handler(Looper.getMainLooper())

private const val EVENT_SIGNED_IN = "NativePHP\\PlayGamesServices\\Events\\SignedIn"
private const val EVENT_SIGN_IN_FAILED = "NativePHP\\PlayGamesServices\\Events\\SignInFailed"
private const val EVENT_SCORE_SUBMITTED = "NativePHP\\PlayGamesServices\\Events\\ScoreSubmitted"
private const val EVENT_SCORE_SUBMISSION_FAILED = "NativePHP\\PlayGamesServices\\Events\\ScoreSubmissionFailed"
private const val EVENT_ACHIEVEMENT_UNLOCKED = "NativePHP\\PlayGamesServices\\Events\\AchievementUnlocked"
private const val EVENT_ACHIEVEMENT_UNLOCK_FAILED = "NativePHP\\PlayGamesServices\\Events\\AchievementUnlockFailed"
private const val EVENT_ACHIEVEMENT_INCREMENTED = "NativePHP\\PlayGamesServices\\Events\\AchievementIncremented"
private const val EVENT_ACHIEVEMENT_INCREMENT_FAILED = "NativePHP\\PlayGamesServices\\Events\\AchievementIncrementFailed"

// ── Event dispatch helpers ──────────────────────────────────────────────────

private fun dispatch(activity: FragmentActivity, event: String, payload: Map<String, Any?>) {
    NativeActionCoordinator.dispatchEvent(activity, event, JSONObject(payload).toString())
}

private fun dispatchCurrentPlayer(activity: FragmentActivity) {
    PlayGames.getPlayersClient(activity).currentPlayer
        .addOnSuccessListener { player: Player ->
            dispatch(
                activity, EVENT_SIGNED_IN, mapOf(
                    "playerId" to player.playerId,
                    "displayName" to player.displayName,
                    "avatarUrl" to player.iconImageUri?.toString()
                )
            )
        }
        .addOnFailureListener { e ->
            dispatch(activity, EVENT_SIGN_IN_FAILED, mapOf("errorMessage" to (e.message ?: "Unable to load player profile")))
        }
}

// ── Bridge Functions ─────────────────────────────────────────────────────────

object PlayGamesServicesFunctions {

    // ── Sign-in ──────────────────────────────────────────────────────────────

    class Initialize(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val autoSignIn = parameters["auto_sign_in"] as? Boolean ?: true

            mainHandler.post {
                PlayGamesSdk.initialize(activity)
                Log.i(TAG, "PlayGamesSdk initialized (autoSignIn=$autoSignIn)")

                if (autoSignIn) {
                    PlayGames.getGamesSignInClient(activity).isAuthenticated
                        .addOnCompleteListener { task ->
                            val authenticated = task.isSuccessful && task.result.isAuthenticated
                            Log.i(TAG, "Silent auth check: isSuccessful=${task.isSuccessful}, authenticated=$authenticated, exception=${task.exception}")
                            if (authenticated) {
                                dispatchCurrentPlayer(activity)
                            }
                            // Not authenticated on a silent check is expected — stay quiet, no SignInFailed noise.
                        }
                }
            }

            return BridgeResponse.success(mapOf("status" to "initializing"))
        }
    }

    class SignIn(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            mainHandler.post {
                Log.i(TAG, "SignIn requested for package ${activity.packageName}")
                PlayGames.getGamesSignInClient(activity).signIn()
                    .addOnCompleteListener { task ->
                        val authenticated = task.isSuccessful && task.result.isAuthenticated
                        Log.i(
                            TAG,
                            "SignIn result: isSuccessful=${task.isSuccessful}, authenticated=$authenticated, " +
                                "exception=${task.exception}, cause=${task.exception?.cause}"
                        )
                        if (authenticated) {
                            dispatchCurrentPlayer(activity)
                        } else {
                            dispatch(
                                activity, EVENT_SIGN_IN_FAILED, mapOf(
                                    "errorMessage" to (task.exception?.message
                                        ?: "Sign-in was cancelled, or this account/app isn't authorized for Play Games Services yet (check Play Console credentials & testers)")
                                )
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "SignIn task failed outright", e)
                    }
            }

            return BridgeResponse.success(mapOf("status" to "signing_in"))
        }
    }

    class GetCurrentPlayer(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            mainHandler.post { dispatchCurrentPlayer(activity) }
            return BridgeResponse.success(mapOf("status" to "fetching"))
        }
    }

    // ── Leaderboards ─────────────────────────────────────────────────────────

    class SubmitScore(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val leaderboardId = parameters["leaderboard_id"] as? String
                ?: return BridgeResponse.error("MISSING_PARAM", "leaderboard_id is required")
            val score = (parameters["score"] as? Number)?.toLong()
                ?: return BridgeResponse.error("MISSING_PARAM", "score is required")

            mainHandler.post {
                PlayGames.getLeaderboardsClient(activity).submitScoreImmediate(leaderboardId, score)
                    .addOnSuccessListener {
                        dispatch(activity, EVENT_SCORE_SUBMITTED, mapOf("leaderboardId" to leaderboardId, "score" to score))
                    }
                    .addOnFailureListener { e ->
                        dispatch(
                            activity, EVENT_SCORE_SUBMISSION_FAILED, mapOf(
                                "leaderboardId" to leaderboardId,
                                "errorMessage" to (e.message ?: "Unable to submit score")
                            )
                        )
                    }
            }

            return BridgeResponse.success(mapOf("status" to "submitting"))
        }
    }

    class ShowLeaderboard(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val leaderboardId = parameters["leaderboard_id"] as? String
                ?: return BridgeResponse.error("MISSING_PARAM", "leaderboard_id is required")

            mainHandler.post {
                PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener { intent -> activity.startActivity(intent) }
                    .addOnFailureListener { e ->
                        dispatch(
                            activity, EVENT_SCORE_SUBMISSION_FAILED, mapOf(
                                "leaderboardId" to leaderboardId,
                                "errorMessage" to (e.message ?: "Unable to open leaderboard")
                            )
                        )
                    }
            }

            return BridgeResponse.success(mapOf("status" to "showing"))
        }
    }

    class ShowAllLeaderboards(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            mainHandler.post {
                PlayGames.getLeaderboardsClient(activity).allLeaderboardsIntent
                    .addOnSuccessListener { intent -> activity.startActivity(intent) }
                    .addOnFailureListener { e -> Log.w(TAG, "Unable to open leaderboards: ${e.message}") }
            }

            return BridgeResponse.success(mapOf("status" to "showing"))
        }
    }

    // ── Achievements ─────────────────────────────────────────────────────────

    class UnlockAchievement(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val achievementId = parameters["achievement_id"] as? String
                ?: return BridgeResponse.error("MISSING_PARAM", "achievement_id is required")

            mainHandler.post {
                PlayGames.getAchievementsClient(activity).unlockImmediate(achievementId)
                    .addOnSuccessListener {
                        dispatch(activity, EVENT_ACHIEVEMENT_UNLOCKED, mapOf("achievementId" to achievementId))
                    }
                    .addOnFailureListener { e ->
                        dispatch(
                            activity, EVENT_ACHIEVEMENT_UNLOCK_FAILED, mapOf(
                                "achievementId" to achievementId,
                                "errorMessage" to (e.message ?: "Unable to unlock achievement")
                            )
                        )
                    }
            }

            return BridgeResponse.success(mapOf("status" to "unlocking"))
        }
    }

    class IncrementAchievement(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val achievementId = parameters["achievement_id"] as? String
                ?: return BridgeResponse.error("MISSING_PARAM", "achievement_id is required")
            val steps = (parameters["steps"] as? Number)?.toInt() ?: 1

            mainHandler.post {
                PlayGames.getAchievementsClient(activity).incrementImmediate(achievementId, steps)
                    .addOnSuccessListener { justUnlocked ->
                        if (justUnlocked) {
                            dispatch(activity, EVENT_ACHIEVEMENT_UNLOCKED, mapOf("achievementId" to achievementId))
                        } else {
                            dispatch(activity, EVENT_ACHIEVEMENT_INCREMENTED, mapOf("achievementId" to achievementId, "steps" to steps))
                        }
                    }
                    .addOnFailureListener { e ->
                        dispatch(
                            activity, EVENT_ACHIEVEMENT_INCREMENT_FAILED, mapOf(
                                "achievementId" to achievementId,
                                "errorMessage" to (e.message ?: "Unable to increment achievement")
                            )
                        )
                    }
            }

            return BridgeResponse.success(mapOf("status" to "incrementing"))
        }
    }

    class ShowAchievements(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            mainHandler.post {
                PlayGames.getAchievementsClient(activity).achievementsIntent
                    .addOnSuccessListener { intent -> activity.startActivity(intent) }
                    .addOnFailureListener { e -> Log.w(TAG, "Unable to open achievements: ${e.message}") }
            }

            return BridgeResponse.success(mapOf("status" to "showing"))
        }
    }
}
