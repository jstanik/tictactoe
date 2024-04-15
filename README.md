# Tic Tac Toe

## Introduction

A simple implementation of the tic-tac-toe (a.k.a. noughts and crosses) game that can be played over
the network.
This little projects servers as a study and exercise material for my young students whom I try to
teach programming basics in the Java language.

## Project structure

The project is divided into multiple modules each covering a specific problem area:

- `tictactoe-game` - this module defines the game domain model defining the basic terms such
  as `Player`, `Game`, `Board`, ... and game rules.
- `tictactoe-net` - this module defines the network protocol between a game server and a player's
  client application. This module contains definition of the messages exchanged over the network and
  the way how these messages are serialized and deserialized to/from sequence of bytes.
- `tictactoe-server` - this module contains the implementation of the game server.
- `tictactoe-client` - this module provides supporting classes for player's client application
  implementation. It handles the network communication, so the client implementation can focus on
  the player's logic.
- `tictactoe-console` - this module contains an implementation of a player as a console application.
  The application uses a console to display game status information as well as asks application
  users for next moves.
- `tictactoe-gui` - this module contains an implementation of a player as GUI application based on
  Java Swing. It uses graphical representation of the game board to display game status information
  as well as it collects user's next desired moves.