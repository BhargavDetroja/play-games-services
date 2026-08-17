<?php

namespace NativePHP\PlayGamesServices\Commands;

use Native\Mobile\Plugins\Commands\NativePluginHookCommand;

/**
 * Substitutes the ${PLAY_GAMES_APP_ID} placeholder AndroidPluginCompiler writes
 * into AndroidManifest.xml with the real value from .env. The generic plugin
 * meta_data pipeline in nativephp/mobile does not resolve ${...} placeholders
 * itself — Android's manifest merger will hard-fail on the raw placeholder
 * syntax if nothing substitutes it before Gradle runs, so this hook does it.
 */
class PostCompileCommand extends NativePluginHookCommand
{
    protected $signature = 'nativephp:play-games-services:post-compile';

    protected $description = 'Inject PLAY_GAMES_APP_ID into AndroidManifest.xml';

    public function handle(): int
    {
        if (! $this->isAndroid()) {
            return self::SUCCESS;
        }

        $manifestPath = $this->buildPath().'/app/src/main/AndroidManifest.xml';

        if (! file_exists($manifestPath)) {
            $this->warn("AndroidManifest.xml not found at {$manifestPath}");

            return self::SUCCESS;
        }

        $manifest = file_get_contents($manifestPath);
        $manifest = str_replace('${PLAY_GAMES_APP_ID}', (string) env('PLAY_GAMES_APP_ID', ''), $manifest);
        file_put_contents($manifestPath, $manifest);

        $this->info('Play Games App ID injected into AndroidManifest.xml');

        return self::SUCCESS;
    }
}
