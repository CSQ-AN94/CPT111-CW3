# CPT111 Coursework 3 – Movie Recommendation & Tracker

Group: 24  
Semester 1, 2025–26

## Overview
This project implements a Movie Recommendation and Tracker System in Java.
Users can log in, manage a personal watchlist and viewing history, and receive simple
top-N movie recommendations based on their behaviour.

## Project Structure

```
Project Root
├── src/
│   ├── Movie.java
│   ├── User.java
│   ├── Watchlist.java
│   ├── History.java
│   ├── RecommendationEngine.java
│   ├── MovieFileHandler.java
│   ├── UserFileHandler.java
│   └── MovieAppGUI.java
├── data/
│   ├── movies.csv
│   └── users.csv
└── README.md
```

## Requirements
- Java JDK 21 (or later)


## Data Files
- `data/movies.csv`: movie library (ID, Title, Genre, Year, Rating)
- `data/users.csv` : user data (Username, Password, Watchlist, History)

## Advanced Features (implemented)
- [x] Create new user account (Register)
- [x] Change password
- [x] Multiple recommendation strategies, switchable at runtime (Genre/Year/Rating)
- [x] JavaFX GUI (replaces command-line menu)
- [x] Hash users’ passwords before saving to CSV

## Notes
1. If the program cannot locate the CSV files, please check that the working directory
is set to the project root and that the `data/` folder exists at the same level as `src/`.
2.  Usernames and initial user records follow the original CSV file provided
    for the coursework, while passwords in `users.csv` are stored as hashed values
    as part of the password hashing advanced feature.
