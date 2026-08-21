# Changelog

## 1.1.0

- Fixed the iOS-equivalent link in README.md to point to the correct `game-center` repo.

## 1.0.0 — Initial release

- Sign-in: `initialize()` (silent auto sign-in), `signIn()` (interactive), `getCurrentPlayer()`.
- Leaderboards: `submitScore()`, `showLeaderboard()`, `showAllLeaderboards()`.
- Achievements: `unlockAchievement()`, `incrementAchievement()`, `showAchievements()`.
- 8 Laravel events covering sign-in and success/failure pairs for scores and achievements.
- Named slot config for leaderboard/achievement IDs, PHP facade, JS bridge.
