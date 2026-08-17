<?php

namespace NativePHP\PlayGamesServices;

use Illuminate\Support\ServiceProvider;
use NativePHP\PlayGamesServices\Commands\PostCompileCommand;

class PlayGamesServicesServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->mergeConfigFrom(__DIR__.'/../config/play-games-services.php', 'play-games-services');

        $this->app->singleton('play-games-services', fn () => new PlayGamesServices);
    }

    public function boot(): void
    {
        $this->publishes([
            __DIR__.'/../config/play-games-services.php' => config_path('play-games-services.php'),
        ], 'play-games-services-config');

        if ($this->app->runningInConsole()) {
            $this->commands([
                PostCompileCommand::class,
            ]);
        }
    }
}
