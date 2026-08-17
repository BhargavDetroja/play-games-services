<?php

namespace NativePHP\PlayGamesServices\Facades;

use Illuminate\Support\Facades\Facade;

/**
 * @method static static initialize()
 * @method static static signIn()
 * @method static static getCurrentPlayer()
 * @method static static submitScore(string $leaderboard, int $score)
 * @method static static showLeaderboard(string $leaderboard)
 * @method static static showAllLeaderboards()
 * @method static static unlockAchievement(string $achievement)
 * @method static static incrementAchievement(string $achievement, int $steps = 1)
 * @method static static showAchievements()
 * @method static string resolveLeaderboardId(string $slot)
 * @method static string resolveAchievementId(string $slot)
 *
 * @see \NativePHP\PlayGamesServices\PlayGamesServices
 */
class PlayGamesServices extends Facade
{
    protected static function getFacadeAccessor(): string
    {
        return 'play-games-services';
    }
}
