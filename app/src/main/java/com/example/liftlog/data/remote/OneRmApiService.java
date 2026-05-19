package com.example.liftlog.data.remote;

/**
 * Stub interfejsu do API Ninjas — kalkulator 1RM.
 *
 * TODO(bogdans):
 *  1. Dodaj Retrofit (patrz {@link ExerciseApiService}).
 *  2. Załaduj API_NINJAS_KEY z local.properties do BuildConfig.
 *  3. Adnotuj:
 *       @GET("v1/onerepmax")
 *       Call<OneRmResponse> calculate(@Header("X-Api-Key") String apiKey,
 *                                     @Query("weight") float weight,
 *                                     @Query("reps") int reps);
 *  4. W {@link com.example.liftlog.ui.onerm.OneRmFragment} odkomentuj checkbox
 *     "Użyj API Ninjas" i wywołaj API gdy zaznaczone.
 *  5. Jeśli API zwróci błąd / offline — fallback na {@link com.example.liftlog.ui.onerm.OneRmCalculator}.
 */
public interface OneRmApiService {

    OneRmResponse calculate(String apiKey, float weightKg, int reps);

    /** API Ninjas zwraca pole one_rep_max (float, lb lub kg w zależności od parametru units). */
    class OneRmResponse {
        public float one_rep_max;
    }
}
