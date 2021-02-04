# Introduction
This is an interview challenge for takeaway.com created by Chen (morning142857@hotmail.com).
A http based end-2-end game for players to exchange numbers until one of the player get 1.
{See number exchanging rule in gameofthree.game.Game#playCore}

# Feature list
1. Without manual interaction, game will be automatically created in every 10 sec. (Configurable with game.rest.sec)
2. Game will be automatically played by 2 processes.
3. Only 1 game will be running at the same time (otherwise very hard to see what's going on in logs)
4. When a game comes finishes, the next game will be scheduled.

// items below not finished yet.
5. It is Possible to manually schedule a game by posting endpoint `/admin/games/create` with start number.  
The instance get post will be the game starter (the player sending out the 1st number)
The manually scheduled game will be run as the next game after finishing the ongoing one.
If multiple games created manually they will be queued.
If both players have manually created games, they will roll for the next starter the same as for auto-created games do.
6. All the game get played (history or onging) can be queried with endpoint '/admin/games/list' 
7. Game play can be viewed with endpoint '/admin/games/{id}/log', both player have all game logs. 
(however the content for the same game can be mirrored, e.g. for player1 `send 50` is `receive 50` for player2, `Win` for player1 is `Loss` for player2,
Can be improved if we put player name somewhere and display `{playerName} plays 50` or `{playerName} wins`)
8. Highly modular therefore any part of the logic is easy to replace.
9. When a one of the players disconnects, the current game ends exceptionally (display also in game log page), a new game will start when he comes back.


# Known Issue List
1. When it happens that `player1` find `player2` disconnected (network issues), however in the view of `play2` both players are online and healthy. The game lifecycle state will be broken. -- can be solved by involving a game timeout without action.


#How to run
Build with JAVA11.
1. Start player1: `bash run.sh player1 8080 http://player2:9090`
1. Start player2: `bash run.sh player2 9090 http://player1:8080`

If the test env trying to run it doesn't have java11, replace the command with:
1. `bash run.sh player1 8080 http://player2:9090 rebuild`
1. `bash run.sh player1 8080 http://player2:9090 rebuild`

It will build the `.jar` file inside the docker container. (can be very slow)

TODO - A stand along image can be created, if necessary. See if have a little more time :P
