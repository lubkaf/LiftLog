package com.example.liftlog.data.remote;

import java.util.List;

/**
 * Stub interfejsu do ExerciseDB (RapidAPI).
 *
 * TODO(bogdans):
 *  1. Dodaj zależności do app/build.gradle.kts:
 *       implementation("com.squareup.retrofit2:retrofit:2.11.0")
 *       implementation("com.squareup.retrofit2:converter-gson:2.11.0")
 *  2. Załaduj RAPIDAPI_KEY z local.properties do BuildConfig
 *     (manifestPlaceholder lub buildConfigField w android { defaultConfig {...} }).
 *  3. Adnotuj metodę poniżej Retrofit-em:
 *       @GET("exercises")
 *       Call<List<ExerciseDto>> getAllExercises(@Header("X-RapidAPI-Key") String apiKey,
 *                                               @Header("X-RapidAPI-Host") String host);
 *  4. Wywołaj z {@link com.example.liftlog.ui.library.LibraryViewModel#fetchFromExerciseDb()}.
 *  5. Po pobraniu zmapuj ExerciseDto → {@link com.example.liftlog.data.model.Exercise}
 *     z isCustom=0 i insertuj przez ExerciseRepository (deduplikacja po name).
 */
public interface ExerciseApiService {

    /** Pobierz wszystkie ćwiczenia z ExerciseDB. */
    List<ExerciseDto> getAllExercises(String apiKey);

    /** DTO zwracane przez ExerciseDB — pola dopasować po sprawdzeniu odpowiedzi. */
    class ExerciseDto {
        public String name;
        public String target;      // grupa mięśniowa
        public String equipment;
        public String instructions;
    }
}
