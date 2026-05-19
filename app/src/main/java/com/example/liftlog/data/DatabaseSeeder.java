package com.example.liftlog.data;

import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.model.Exercise;

class DatabaseSeeder {

    static void seed(ExerciseDao dao) {
        String[][] exercises = {
            {"Wyciskanie sztangi na ławce poziomej", "Klatka piersiowa"},
            {"Wyciskanie sztangi na ławce skośnej", "Klatka piersiowa"},
            {"Rozpiętki z hantlami", "Klatka piersiowa"},
            {"Pompki", "Klatka piersiowa"},
            {"Przysiad ze sztangą", "Nogi"},
            {"Leg press", "Nogi"},
            {"Wykroki z hantlami", "Nogi"},
            {"Uginanie nóg w leżeniu", "Nogi"},
            {"Wyciskanie nóg pionowo", "Nogi"},
            {"Martwy ciąg", "Plecy"},
            {"Wiosłowanie sztangą", "Plecy"},
            {"Podciąganie na drążku", "Plecy"},
            {"Ściąganie drążka wyciągu", "Plecy"},
            {"Wyciskanie żołnierskie", "Barki"},
            {"Unoszenie hantli bokiem", "Barki"},
            {"Arnold press", "Barki"},
            {"Uginanie ramion ze sztangą", "Biceps"},
            {"Uginanie ramion z hantlami", "Biceps"},
            {"Wyciskanie francuskie", "Triceps"},
            {"Prostowanie ramion na wyciągu", "Triceps"},
            {"Plank", "Brzuch"},
            {"Spięcia brzucha", "Brzuch"},
            {"Wspięcia na palce", "Łydki"},
        };

        for (String[] data : exercises) {
            Exercise e = new Exercise();
            e.name = data[0];
            e.muscleGroup = data[1];
            e.isCustom = 0;
            dao.insert(e);
        }
    }
}
