# Changelog

## 1.0.0 — Initial release

- Sign-in: `initialize()` (silent auto sign-in), `signIn()` (interactive), `getCurrentPlayer()`.
- Leaderboards: `submitScore()`, `showLeaderboard()`, `showAllLeaderboards()`.
- Achievements: `unlockAchievement()`, `incrementAchievement()`, `showAchievements()`.
- 8 Laravel events covering sign-in and success/failure pairs for scores and achievements.
- Named slot config for leaderboard/achievement IDs, PHP facade, JS bridge.

### Known limitations at release

- **Sign-in** has been verified to correctly reach Google's Play Games Services backend end
  to end (account picker opens, request/response round-trip confirmed via logs), but has not
  yet been confirmed to complete successfully with a real authorized test account — only
  the "account not authorized" rejection path has been observed so far. Confirm sign-in
  fully succeeds with your own Play Console tester setup before relying on it in production.
- **Leaderboards and achievements have not been exercised against real Play Console data.**
  The bridge calls, PHP facade, and event wiring are implemented and validated for
  compile-time correctness, but `submitScore`, `showLeaderboard`, `unlockAchievement`,
  `incrementAchievement`, and `showAchievements` have not yet been run against a real
  leaderboard/achievement ID. Test these against your own Play Console setup before
  shipping a release build.
