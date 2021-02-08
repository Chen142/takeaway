# Introduction
This is an interview challenge for takeaway.com created by Chen (morning142857@hotmail.com).
A http based end-2-end game for players to exchange numbers until one of the player get 1.
{See number exchanging rule in gameofthree.game.Game#playCore}

# Feature list
1. Without manual interaction, game will be automatically created in every 10 sec. (Configurable with game.rest.sec)
2. Game will be automatically played by 2 processes, the start number is random(2~10000)
3. Only 1 game will be running at the same time (otherwise very hard to see what's going on in logs)
4. When a game comes finishes, the next game will be scheduled.
5. It is Possible to manually schedule a game by posting endpoint `/admin/games/create` with start number (must be >2) .
The instance get post will be the game starter (the player sending out the 1st number)
The manually scheduled game will be run as the next game after finishing the ongoing one.
If multiple games created manually they will be queued.
If both players have manually created games, they will roll for the next starter the same as for auto-created games do.
6. All the game get played (history or onging) can be queried with endpoint '/admin/games/list' 
7. Game play can be viewed with endpoint '/admin/games/{id}/log', both player have all game logs. 
(however the content for the same game can be mirrored, e.g. for player1 `send 50` is `receive 50` for player2, `Win` for player1 is `Loss` for player2,
Can be improved if we put player name somewhere and display `{playerName} plays 50` or `{playerName} wins`)
8. When one of the players disconnects, the current game ends exceptionally (display also in game log page), a new game will start when he comes back.
9. Players can quit and rejoin as they want.


# Known Issues List
1. Reconnecting not fully tested, there can be 1 or 2 edge cases not covered.

# System Requirements
1. Docker (version >= 1.10)

#How to run
1. Build `bash build.sh` (can be slow)
1. Start player1: `bash run.sh player1 8080 http://player2:9090`
1. Start player2: `bash run.sh player2 9090 http://player1:8080` (open a new terminal)
1. View played games @`http://localhost:8080/admin/games/list` after some time.
1. View game logs @`http://localhost:8080/admin/games/{id}/log`, by copying an id from played games page.
1. Manually schedule a game with number 200 by calling 
`curl --data "200" http://localhost:8080/admin/games/create -H  "CONTENT-TYPE: application/json"`, remember the game id returned.
1. Now we can find the game id on played games list. (maybe after several seconds)
1. The game log is available with the game id too. (maybe after several seconds)
1. Try out reconnection. - try to kill one container and reboot it.

# TODO
1. makefile
1. e2e tests
1. log config.

