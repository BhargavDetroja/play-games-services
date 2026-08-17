<?php

namespace NativePHP\PlayGamesServices\Events;

use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class SignedIn
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public function __construct(
        public readonly string $playerId,
        public readonly string $displayName,
        public readonly ?string $avatarUrl = null,
    ) {}
}
