👾 JavaMon: Pokémon-Inspired Java RPG

<p align="center">
<img src="https://www.google.com/search?q=https://placehold.co/600x150/007ACC/white%3Ftext%3DJavaMon%2BLogo%2B%2526%2BScreenshot%2BPlaceholder" alt="JavaMon Project Placeholder Image">
</p>

📝 Project Overview

JavaMon is a Pokémon-inspired game developed using Java with a Graphical User Interface (GUI).

The primary goal of this project is to provide an interactive turn-based battle system where players act as trainers and control monsters with unique abilities and stats. The game allows users to select trainers, choose monsters, engage in battles, and view outcomes such as win or game-over screens.

This project was developed to demonstrate core Object-Oriented Programming (OOP) concepts in a fun and practical way by simulating real-world relationships between trainers, monsters, abilities, and game mechanics.

🔑 Object-Oriented Programming (OOP) Concepts Applied

The project is structured around the four pillars of OOP:

Concept

Application in JavaMon

Encapsulation

Grouping data and behavior into classes such as Trainer, Monster, Ability, and Database Manager. Attributes (e.g., health, abilities) are accessed via controlled methods, ensuring data integrity.

Abstraction

Hiding complex logic, such as battle calculations, ability effects, and status effects, from the user. High-level methods are exposed to the GUI while internal processing is handled by classes like BattleMechanics and AbilityLogic.

Inheritance

Implementing reuse of shared behaviors and properties. Monsters and abilities can inherit common attributes, allowing specialized behavior without duplicating base code.

Polymorphism

Applied through abilities and effects where different abilities execute unique logic while conforming to the same method structure. This design allows flexible expansion of new abilities without altering existing code.

💻 Program Structure

The core functionality of the game is handled by the following main classes:

MainMenu: The entry point of the program, displaying the main menu GUI.

TrainerSelection: Allows the user to select their trainer.

Monster: Represents the in-game monsters, including their stats, health, and abilities.

Ability / Ability Logic: Defines and executes the unique abilities monsters can use in battle.

BattleMechanics: Controls the core turn-based combat logic, calculating damage and determining turns.

GameWindow: The main interface for gameplay and battles.

GameOver: Displays the final score (rounds completed) when the player loses.

Database Manager: Handles database operations using javmon.db for data persistence.

Status Effect: Manages temporary effects such as buffs or debuffs applied during combat.

▶️ How to Run the Program

To run the JavaMon game on your local machine, follow these steps:

Prerequisites: Ensure Java JDK 21 is installed on your computer.

IDE Setup: Install Git and an Integrated Development Environment (IDE) such as Eclipse or VS Code.

Clone the Repository: Open your terminal or command prompt and clone the project:

git clone [https://github.com/bitancutiepie/JavaMon.git](https://github.com/bitancutiepie/JavaMon.git)


Open in IDE: Open the cloned JavaMon folder in your IDE.

Add Library: You need to manually add the SQLite JDBC driver:

Select the Javamon Project and go to Properties.

Go to Libraries, select add external JARS.

Follow the file path git/Javamon/Javamon/lib and add the sqlite-jdbc-3.51.0.0.jar.

Run: Locate the MainMenu.java file inside the src/javmon package.

Launch: Run the file as a Java Application. The game window will launch, and you can begin playing.

🎮 Sample Gameplay Features

Initial Setup: The game begins at the Main Menu, followed by the Trainer Selection screen, where users choose from five unique trainers.

Draft Selection: Users select three JavaMons to form their team, with confirmation prompts displaying the monster's vital statistics.

Combat: In the Game Window, JavaMons have four abilities and one Ultimate Ability, accessible via the FIGHT button.

In-Battle UI: Users can hover over Ally and Enemy JavaMons to view stats.

Tactics: The JAVAMON button allows players to swap monsters, and the HELP button displays the Type Chart, Game Mechanics, and Tips.

Progression: Opponent teams consist of three JavaMons per round. Defeating an enemy grants 30 XP to the finishing monster, leveling them up and increasing their stats for subsequent, more difficult opponents.

Defeat: If the player is defeated, the Game Over screen appears, displaying the number of rounds completed.

🚀 Future Enhancements

Due to time constraints during initial development, the following improvements are recommended for future versions:

JavaMon Balancing: Proper balancing of abilities and statistics to ensure fair, competitive, and strategic gameplay, preventing the reliance on "broken" characters.

Reward System per Floor: Implementing rewards (stat boosts, items, or ability upgrades) after each completed floor/round to enhance player progression and engagement.

Move Set Limit: Introducing a restricted number of usable abilities to promote strategic decision-making in team loadouts.

Expanded JavaMon Selection: Adding more diverse JavaMons with different classes and abilities to increase variety and replayability.

Move Set Learning: Allowing JavaMons to learn new abilities as they level up, enhancing the sense of growth and making long-term gameplay more dynamic.

🧑‍💻 Authors and Acknowledgement

Authors

Bitancor, Joshua Vincent - Group Leader, Lead Programmer for game design/logic, System Analyst.

Balbuena, Adrian G. - Lead Creative Designer for the overall GUI and elements.

Catapang, Leann Kirsten T. - Assigned Programmer for GUI Design and elements, and game design/logic.

Plata, Cris Ed John C. - Lead Developer for program requirements/system architecture, and Assigned Programmer for GUI Design and elements.

References

Pokemon

Pokerogue

Rogue like games

Roguelike floor games
