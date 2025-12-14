# JavaMon

## Project Overview

JavaMon is a Pokémon-inspired turn-based role-playing game developed in **Java** with a **Graphical User Interface (GUI)**. The main purpose of the project is to provide an interactive battle system where players act as trainers who control monsters with unique abilities and statistics.

Players can select trainers, draft JavaMons, engage in turn-based battles, and experience outcomes such as victory or game-over scenarios. JavaMon demonstrates **Object-Oriented Programming (OOP)** concepts in a fun and practical way by simulating real-world relationships between trainers, monsters, abilities, and game mechanics.

This project is intended for students and users who want to experience a simple RPG-style game while showcasing core OOP principles in Java.

---

## OOP Concepts Applied

### Encapsulation
Encapsulation is applied by grouping data and behavior into classes such as `Trainer`, `Monster`, `Ability`, and `DatabaseManager`. Each class manages its own attributes and provides controlled access through methods, ensuring data integrity.

### Abstraction
Abstraction is used to hide complex game logic from the user. Battle calculations, ability effects, and status effects are handled internally by classes like `BattleMechanics`, `AbilityLogic`, and `StatusEffect`.

### Inheritance
Inheritance is implemented to reuse shared behaviors and properties. Monsters and abilities inherit common attributes, allowing specialized behavior without duplicating code.

### Polymorphism
Polymorphism is applied through abilities and effects where different abilities execute different logic while sharing the same method structure.

---

## Program Structure

### Main Classes and Their Roles

- **MainMenu** – Entry point of the program
- **TrainerSelection** – Trainer selection screen
- **Monster** – Represents JavaMons with stats and abilities
- **Ability / AbilityLogic** – Ability definitions and execution
- **BattleMechanics** – Turn-based combat logic
- **GameWindow** – Main gameplay interface
- **GameOver** – Game-over screen
- **DatabaseManager** – SQLite database handler
- **StatusEffect** – Buff and debuff management

---

## How to Run the Program

### Prerequisites
- Java JDK 21
- Git
- IDE (Eclipse or VS Code recommended)

### Installation & Execution

```bash
git clone https://github.com/bitancutiepie/JavaMon.git
```

1. Open the project in your IDE  
2. Go to **Project Properties → Libraries → Add External JARs**  
3. Add `sqlite-jdbc-3.51.0.0.jar` from `JavaMon/Javamon/lib/`  
4. Locate `MainMenu.java` inside `src/javmon`  
5. Run the file as a Java Application  

---

## Gameplay Overview

- Start at the **Main Menu** and adjust volume settings
- Choose from **five unique trainers**
- Draft **three JavaMons** with unique abilities and classes
- Engage in turn-based battles using abilities and ultimate skills
- Gain XP, level up, and progress through increasingly difficult rounds
- A **Game Over** screen appears if the player is defeated

---

## Authors

- **Adrian G. Balbuena** – Lead Creative Designer (GUI)
- **Joshua Vincent Bitancor** – Group Leader, Lead Programmer, System Analyst
- **Leann Kirsten T. Catapang** – GUI and Game Logic Programmer
- **Cris Ed John P. Plata** – System Architecture Lead, GUI Programmer

---

## Acknowledgement

The team would like to thank our instructor for guidance throughout the development of this project and for managing multiple academic timelines alongside this work. We also acknowledge the Java documentation and open-source resources that contributed to the completion of this project.

---

## References

- Pokémon  
- PokéRogue  
- Roguelike games  
- Roguelike floor-based games  

---

## Future Enhancements

- JavaMon balancing for fair gameplay
- Reward system per floor
- Move set limits
- Expanded JavaMon roster
- Move learning through leveling
