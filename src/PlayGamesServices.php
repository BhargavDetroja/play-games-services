<?php

namespace NativePHP\PlayGamesServices;

use InvalidArgumentException;

class PlayGamesServices
{
    /**
     * Initialize the Play Games SDK. Attempts a silent sign-in when
     * config('play-games-services.auto_sign_in') is true — dispatches
     * SignedIn if the player is already authenticated, otherwise does
     * nothing (no UI, no SignInFailed noise on a cold start).
     */
    public function initialize(): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.Initialize', [
            'auto_sign_in' => (bool) config('play-games-services.auto_sign_in', true),
        ]);

        return $this;
    }

    /**
     * Trigger the interactive Play Games sign-in flow. Dispatches
     * SignedIn on success or SignInFailed on failure/cancellation.
     */
    public function signIn(): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.SignIn', []);

        return $this;
    }

    /**
     * Fetch the signed-in player's profile. Dispatches SignedIn with the
     * player's ID, display name, and avatar URL, or SignInFailed if no
     * player is currently signed in.
     */
    public function getCurrentPlayer(): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.GetCurrentPlayer', []);

        return $this;
    }

    public function submitScore(string $leaderboard, int $score): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.SubmitScore', [
            'leaderboard_id' => $this->resolveLeaderboardId($leaderboard),
            'score' => $score,
        ]);

        return $this;
    }

    public function showLeaderboard(string $leaderboard): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.ShowLeaderboard', [
            'leaderboard_id' => $this->resolveLeaderboardId($leaderboard),
        ]);

        return $this;
    }

    public function showAllLeaderboards(): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.ShowAllLeaderboards', []);

        return $this;
    }

    public function unlockAchievement(string $achievement): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.UnlockAchievement', [
            'achievement_id' => $this->resolveAchievementId($achievement),
        ]);

        return $this;
    }

    /**
     * Increment an incremental achievement by $steps. Play Games tracks the
     * running total server-side — this is a relative delta, not an absolute value.
     */
    public function incrementAchievement(string $achievement, int $steps = 1): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.IncrementAchievement', [
            'achievement_id' => $this->resolveAchievementId($achievement),
            'steps' => $steps,
        ]);

        return $this;
    }

    public function showAchievements(): static
    {
        if (! $this->enabled()) {
            return $this;
        }

        $this->bridgeCall('PlayGamesServices.ShowAchievements', []);

        return $this;
    }

    /**
     * Resolve a slot name defined in config('play-games-services.leaderboards')
     * to its raw leaderboard ID. Unknown slot names are passed through as-is
     * so raw IDs work without any config changes.
     */
    public function resolveLeaderboardId(string $slot): string
    {
        return $this->resolveSlot($slot, 'leaderboards');
    }

    /**
     * Resolve a slot name defined in config('play-games-services.achievements')
     * to its raw achievement ID. Unknown slot names are passed through as-is
     * so raw IDs work without any config changes.
     */
    public function resolveAchievementId(string $slot): string
    {
        return $this->resolveSlot($slot, 'achievements');
    }

    protected function resolveSlot(string $slot, string $group): string
    {
        $slots = config("play-games-services.{$group}", []);

        if (! array_key_exists($slot, $slots)) {
            return $slot;
        }

        if (blank($slots[$slot])) {
            throw new InvalidArgumentException("Play Games Services: slot \"{$slot}\" has no ID configured in config/play-games-services.php.");
        }

        return (string) $slots[$slot];
    }

    protected function enabled(): bool
    {
        return (bool) config('play-games-services.enabled', true);
    }

    /**
     * @param  array<string, mixed>  $params
     * @return array<string, mixed>|null
     */
    protected function bridgeCall(string $method, array $params): ?array
    {
        if (! function_exists('nativephp_call')) {
            return null;
        }

        $result = nativephp_call($method, json_encode($params));

        return $result ? json_decode($result, true) : null;
    }
}
