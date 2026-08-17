/**
 * Play Games Services Plugin for NativePHP Mobile
 *
 * Slot-name resolution (config('play-games-services.leaderboards'/'achievements'))
 * happens server-side via the PHP Facade or Blade. From JavaScript, pass raw
 * leaderboard/achievement IDs directly.
 *
 * @example
 * import { initialize, signIn, submitScore, onSignedIn } from '.../resources/js/playGamesServices.js';
 *
 * await initialize();
 * onSignedIn(({ playerId, displayName }) => console.log(`Signed in as ${displayName}`));
 * await submitScore('CgkI...', 1500);
 */

async function bridgeCall(method, params = {}) {
    const response = await fetch('/_native/api/call', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]')?.content || '',
        },
        body: JSON.stringify({ method, params }),
    });
    return response.json();
}

// ── Sign-in ───────────────────────────────────────────────────────────────────

export async function initialize(autoSignIn = true) {
    return bridgeCall('PlayGamesServices.Initialize', { auto_sign_in: autoSignIn });
}

export async function signIn() {
    return bridgeCall('PlayGamesServices.SignIn', {});
}

export async function getCurrentPlayer() {
    return bridgeCall('PlayGamesServices.GetCurrentPlayer', {});
}

// ── Leaderboards ──────────────────────────────────────────────────────────────

/**
 * @param {string} leaderboardId  Raw leaderboard ID (resolved server-side for slot names)
 * @param {number} score
 */
export async function submitScore(leaderboardId, score) {
    return bridgeCall('PlayGamesServices.SubmitScore', { leaderboard_id: leaderboardId, score });
}

export async function showLeaderboard(leaderboardId) {
    return bridgeCall('PlayGamesServices.ShowLeaderboard', { leaderboard_id: leaderboardId });
}

export async function showAllLeaderboards() {
    return bridgeCall('PlayGamesServices.ShowAllLeaderboards', {});
}

// ── Achievements ──────────────────────────────────────────────────────────────

export async function unlockAchievement(achievementId) {
    return bridgeCall('PlayGamesServices.UnlockAchievement', { achievement_id: achievementId });
}

export async function incrementAchievement(achievementId, steps = 1) {
    return bridgeCall('PlayGamesServices.IncrementAchievement', { achievement_id: achievementId, steps });
}

export async function showAchievements() {
    return bridgeCall('PlayGamesServices.ShowAchievements', {});
}

// ── Event listeners ───────────────────────────────────────────────────────────

/**
 * Listen for any Play Games Services event dispatched from native code.
 * Returns an unsubscribe function.
 *
 * Event names:
 *   NativePHP\PlayGamesServices\Events\SignedIn
 *   NativePHP\PlayGamesServices\Events\SignInFailed
 *   NativePHP\PlayGamesServices\Events\ScoreSubmitted
 *   NativePHP\PlayGamesServices\Events\ScoreSubmissionFailed
 *   NativePHP\PlayGamesServices\Events\AchievementUnlocked
 *   NativePHP\PlayGamesServices\Events\AchievementUnlockFailed
 *   NativePHP\PlayGamesServices\Events\AchievementIncremented
 *   NativePHP\PlayGamesServices\Events\AchievementIncrementFailed
 *
 * @param {string} eventName - Fully-qualified PHP event class name
 * @param {(payload: object) => void} callback
 * @returns {() => void} unsubscribe function
 */
export function onPlayGamesEvent(eventName, callback) {
    const handler = (e) => {
        if (e.detail?.event === eventName) {
            callback(e.detail.payload ?? {});
        }
    };
    document.addEventListener('native-event', handler);
    return () => document.removeEventListener('native-event', handler);
}

/** @param {(payload: { playerId: string, displayName: string, avatarUrl: string|null }) => void} callback */
export function onSignedIn(callback) {
    return onPlayGamesEvent('NativePHP\\PlayGamesServices\\Events\\SignedIn', callback);
}

/** @param {(payload: { errorMessage: string }) => void} callback */
export function onSignInFailed(callback) {
    return onPlayGamesEvent('NativePHP\\PlayGamesServices\\Events\\SignInFailed', callback);
}

/** @param {(payload: { achievementId: string }) => void} callback */
export function onAchievementUnlocked(callback) {
    return onPlayGamesEvent('NativePHP\\PlayGamesServices\\Events\\AchievementUnlocked', callback);
}
