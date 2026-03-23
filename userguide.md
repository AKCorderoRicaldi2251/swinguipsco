# User Guide: Dice Shooter System

This guide explains how to operate the Dice Shooter application, manage your player profile, and understand the underlying data systems.

---

## 1. Profile Management & Authentication

The system uses a persistent save architecture to track your progress across different sessions.

### **Starting a New Session**
* **New Game**: Select this option to register a new **Hacker ID** (username).
* **Registration**: You will be prompted to enter a name, which initializes a fresh profile with zeroed stats across all four game modules.

### **Loading an Existing Profile**
* **Load Game**: This option allows you to resume progress from a previously created ID.
* **Profile Selection**: The system scans for all available data files and presents a list of registered usernames for you to select from.
* **Data Retrieval**: Once selected, the system pulls your specific high scores and win/loss ratios into the active session.

---

## 2. Data Infrastructure (JSON)

Your progress is managed by the `GameDataManager`, which handles the technical side of saving and loading.

* **Storage Format**: All player data is serialized into **JSON** (.json) files, making the data lightweight and easy for the system to parse.
* **Save Directory**: Profiles are stored in a dedicated `saves/` folder within the project root.
* **Automatic Syncing**: The system performs a "System Sync" by overwriting your JSON file whenever you bank a score or complete a game, ensuring no progress is lost.
* **Global Rankings**: The leaderboard functions by reading every JSON file in the `saves/` directory simultaneously to calculate and display the global rankings of all operators.

---

## 3. Game Module Protocols

### **Game 1: Dice Patterns Challenge**
* **Mechanics**: Roll five dice with up to two rerolls.
* **Controls**: Shoot a die to **[LOCKED]** it (Cyan border) so it remains unchanged during a reroll.
* **Banking**: Shoot **BANK_SCORE** to commit your points to your permanent JSON profile.

### **Game 2: Dice Grid Puzzle**
* **Mechanics**: Populate a 3x3 matrix with randomly generated dice.
* **Placement**: Shoot an empty grid cell to place the "Current Die" shown in the UI.

### **Game 3: Dice Codebreaker**
* **Mechanics**: Deduce a hidden 4-digit code using logic.
* **Feedback**: Green borders indicate a correct value in the correct spot; yellow borders indicate the value exists but is in the wrong spot.

### **Game 4: System Crash (Liar's Dice)**
**Game Type**: Tactical Liar's Dice Variant.
* **Objective**: Bid on the quantity of specific dice faces.
* **Bugs (1s)**: Function as unstable wildcards that increase the count of any bid.
* **Patches (6s)**: Counter-measures that neutralize Bugs. One Patch deletes one Bug from the total wildcard pool.
* **Calculation**: Total Count = (Dice matching Bid Value) + Max(0, Bugs - Patches).
* **Persistence**: Results are logged to your JSON profile as a Win/Loss ratio.. 

---
