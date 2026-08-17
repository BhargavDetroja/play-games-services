<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Kill Switch
    |--------------------------------------------------------------------------
    | Set PLAY_GAMES_ENABLED=false to disable Play Games Services globally —
    | useful for debugging or platforms where it doesn't apply.
    */

    'enabled' => env('PLAY_GAMES_ENABLED', true),

    /*
    |--------------------------------------------------------------------------
    | App ID
    |--------------------------------------------------------------------------
    | Your Play Games Services App ID, found in Play Console →
    | Grow → Play Games Services → Setup → Configuration.
    */

    'app_id' => env('PLAY_GAMES_APP_ID'),

    /*
    |--------------------------------------------------------------------------
    | Auto Sign-In
    |--------------------------------------------------------------------------
    | When true, PlayGamesServices::initialize() silently signs the player in
    | if they're already authenticated with the Play Games app — no UI shown.
    */

    'auto_sign_in' => env('PLAY_GAMES_AUTO_SIGN_IN', true),

    /*
    |--------------------------------------------------------------------------
    | Leaderboards
    |--------------------------------------------------------------------------
    | Name your leaderboards here and reference them by slot name in code.
    | IDs are found in Play Console → Play Games Services → Leaderboards.
    */

    'leaderboards' => [

        // 'high_score' => env('PLAY_GAMES_LEADERBOARD_HIGH_SCORE'),

    ],

    /*
    |--------------------------------------------------------------------------
    | Achievements
    |--------------------------------------------------------------------------
    | Name your achievements here and reference them by slot name in code.
    | IDs are found in Play Console → Play Games Services → Achievements.
    */

    'achievements' => [

        // 'first_win' => env('PLAY_GAMES_ACHIEVEMENT_FIRST_WIN'),

    ],

];
