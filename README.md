# NativePHP Play Games Services

Add **Google Play Games Services** — sign-in, leaderboards, and achievements — to your [NativePHP Mobile](https://nativephp.com) Android app in minutes. No Kotlin, no Gradle edits.

Works with **any frontend** — Livewire, React, Vue, Alpine.js, or plain JavaScript.

> **Android only.** Play Games Services is a Google Play platform feature — it doesn't exist on iOS. For an equivalent iOS experience, see [nativephp-game-center](https://github.com/bhargavdetroja/nativephp-game-center).

> **v1.0.0 testing status:** sign-in has been verified to correctly reach Google's backend
> end to end, but hasn't yet been confirmed to *succeed* with an authorized account — only
> the "account not authorized" path has been observed so far. Leaderboard and achievement
> calls are implemented and compile-verified but haven't been run against real Play Console
> data yet. See [CHANGELOG.md](CHANGELOG.md) for details. Please verify against your own
> Play Console setup before relying on this in production, and file an issue if you hit
> anything unexpected.

---

## Requirements

- PHP 8.2+
- Laravel 12+
- NativePHP Mobile 3.x or 4.x
- A [Play Console](https://play.google.com/console) app with Play Games Services configured (App ID, leaderboards, achievements)

---

## Installation

### 1. Install the package

```bash
composer require bhargavdetroja/nativephp-play-games-services
php artisan native:plugin:register bhargavdetroja/nativephp-play-games-services
```

### 2. Publish the config

```bash
php artisan vendor:publish --tag=play-games-services-config
```

### 3. Add your Play Games App ID to `.env`

```env
# Play Console → Grow → Play Games Services → Setup → Configuration
PLAY_GAMES_APP_ID=1234567890
```

### 4. Configure leaderboards and achievements

```php
// config/play-games-services.php
'leaderboards' => [
    'high_score' => env('PLAY_GAMES_LEADERBOARD_HIGH_SCORE'),
],
'achievements' => [
    'first_win' => env('PLAY_GAMES_ACHIEVEMENT_FIRST_WIN'),
],
```

### 5. Run native install

```bash
php artisan native:install --force
```

---

## Configuration

`config/play-games-services.php` gives you full control over sign-in behaviour and named slots.

### Kill-switch

```env
PLAY_GAMES_ENABLED=false
```

### Auto sign-in

`PlayGamesServices::initialize()` silently signs the player back in if they've already granted access — no UI shown. Disable with:

```env
PLAY_GAMES_AUTO_SIGN_IN=false
```

### Named slots

Reference leaderboards and achievements by name instead of hardcoding raw IDs. Unknown slot names are passed straight through, so raw IDs still work without any config changes.

---

## Usage

### PHP Facade

```php
use NativePHP\PlayGamesServices\Facades\PlayGamesServices;

// Call once on app boot — silently signs the player in if already authorized
PlayGamesServices::initialize();

// Explicit sign-in (shows the Play Games account picker if needed)
PlayGamesServices::signIn();

// Leaderboards
PlayGamesServices::submitScore('high_score', 1500);
PlayGamesServices::showLeaderboard('high_score');
PlayGamesServices::showAllLeaderboards();

// Achievements
PlayGamesServices::unlockAchievement('first_win');
PlayGamesServices::incrementAchievement('collect_100_coins', steps: 10);
PlayGamesServices::showAchievements();
```

### JavaScript (React, Vue, Alpine, plain JS)

The JS bridge accepts raw leaderboard/achievement IDs. Slot resolution happens server-side.

```javascript
import {
    initialize, signIn, getCurrentPlayer,
    submitScore, showLeaderboard, showAllLeaderboards,
    unlockAchievement, incrementAchievement, showAchievements,
    onSignedIn, onSignInFailed, onAchievementUnlocked, onPlayGamesEvent,
} from 'vendor/bhargavdetroja/nativephp-play-games-services/resources/js/playGamesServices.js';

await initialize();

onSignedIn(({ playerId, displayName, avatarUrl }) => {
    console.log(`Signed in as ${displayName}`);
});

await submitScore('CgkI...', 1500); // pass the resolved ID from Blade/PHP
```

Pass resolved IDs from Blade so slot names work from JS too:

```blade
<script>
    const HIGH_SCORE_ID = '{{ app("play-games-services")->resolveLeaderboardId("high_score") }}';
</script>
```

---

## Listening to Events

### In JavaScript

```javascript
import { onSignedIn, onAchievementUnlocked, onPlayGamesEvent } from '...';

const stop = onSignedIn(({ displayName }) => updateProfileUI(displayName));

onAchievementUnlocked(({ achievementId }) => showToast(`Achievement unlocked: ${achievementId}`));

onPlayGamesEvent('NativePHP\\PlayGamesServices\\Events\\ScoreSubmissionFailed', ({ leaderboardId, errorMessage }) => {
    console.warn(`Score submission to ${leaderboardId} failed: ${errorMessage}`);
});

stop(); // unsubscribe (React/Vue component unmount)
```

### In PHP — standard Laravel events

```php
use NativePHP\PlayGamesServices\Events\SignedIn;
use NativePHP\PlayGamesServices\Events\AchievementUnlocked;

public function handle(SignedIn $event): void
{
    auth()->user()->update(['play_games_id' => $event->playerId]);
}
```

**Livewire (v3/v4):**

```php
use Native\Mobile\Attributes\OnNative;
use NativePHP\PlayGamesServices\Events\SignedIn;

#[OnNative(SignedIn::class)]
public function handleSignedIn($playerId, $displayName, $avatarUrl = null)
{
    $this->displayName = $displayName;
}
```

---

## All Events

| Event | Properties | When it fires |
|---|---|---|
| `SignedIn` | `$playerId`, `$displayName`, `$avatarUrl` | Player is authenticated (silent or interactive) |
| `SignInFailed` | `$errorMessage` | Sign-in was cancelled or failed |
| `ScoreSubmitted` | `$leaderboardId`, `$score` | Score was accepted by the leaderboard |
| `ScoreSubmissionFailed` | `$leaderboardId`, `$errorMessage` | Score submission or leaderboard UI failed |
| `AchievementUnlocked` | `$achievementId` | A standard achievement was unlocked, or an incremental one reached 100% |
| `AchievementUnlockFailed` | `$achievementId`, `$errorMessage` | Unlock call failed |
| `AchievementIncremented` | `$achievementId`, `$steps` | An incremental achievement advanced without completing |
| `AchievementIncrementFailed` | `$achievementId`, `$errorMessage` | Increment call failed |

---

## Notes on the Play Games Services v2 SDK

- **No programmatic sign-out.** Play Games Services v2 ties sign-in to the player's Google Play Games account, not a per-app session — there is no `signOut()` in the SDK. Users manage this from the Play Games app itself.
- **`incrementAchievement`** applies a relative delta (steps), matching `AchievementsClient.increment()`. If the increment causes the achievement to reach 100%, `AchievementUnlocked` fires instead of `AchievementIncremented`.
- **Emulators** need a system image with Google Play Services and a Google account signed in in the Play Games app.

---

## Going to Production

1. Set your real `PLAY_GAMES_APP_ID` and leaderboard/achievement IDs in `.env`.
2. Publish your app to at least the Internal Testing track — Play Games Services sign-in only works for testers/reviewers on a published (even if internal) build.
3. Run `php artisan native:install --force`.
4. Build your release: `php artisan native:run android`.

---

## Troubleshooting

**Sign-in silently does nothing**
Your build isn't signed with the same certificate/App ID registered in Play Console, or the app hasn't been uploaded to any test track yet. Play Games Services requires this even for internal testing.

**`unknown slot` exception**
You passed a slot name that isn't defined in `config/play-games-services.php`. Either add it to `leaderboards`/`achievements`, or pass a raw ID directly.

**Validate plugin setup**
```bash
php artisan native:plugin:validate
```

---

## License

MIT
