package com.annas.aifloating;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiClient {

    public interface AnswerCallback {
        void onResult(String answer);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void solveTaskAsync(final String apiKey, final String mode, final String question, final AnswerCallback callback) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String result = solveTaskSync(apiKey, mode, question);
                if (callback != null) {
                    callback.onResult(result);
                }
            }
        });
    }

    public static String solveTaskSync(String apiKey, String mode, String question) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Error: API Key Google Gemini belum diisi di aplikasi utama.";
        }

        HttpURLConnection conn = null;
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey.trim();
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(30000);

            String instruction;
            if (mode != null && mode.contains("Pilihan Ganda")) {
                instruction = "Kamu adalah asisten soal pilihan ganda. Tulis huruf jawaban paling tepat di baris pertama (contoh: Jawaban: C), lalu jelaskan alasannya secara singkat dalam Bahasa Indonesia.";
            } else if (mode != null && mode.contains("Matematika")) {
                instruction = "Kamu adalah tutor matematika. Jelaskan langkah demi langkah secara akurat dan tulis hasil akhirnya dengan jelas dalam Bahasa Indonesia.";
            } else if (mode != null && mode.contains("Coding")) {
                instruction = "Kamu adalah asisten pemrograman. Berikan solusi kode yang bersih dan penjelasan singkat.";
            } else {
                instruction = "Kamu adalah asisten tugas pintar. Analisis konten yang ditandai lalu berikan jawaban akurat, terstruktur, dan jelas dalam Bahasa Indonesia.";
            }

            String fullPrompt = instruction + "\n\nInput:\n" + question;

            JSONObject partObj = new JSONObject();
            partObj.put("text", fullPrompt);

            JSONArray partsArr = new JSONArray();
            partsArr.put(partObj);

            JSONObject contentObj = new JSONObject();
            contentObj.put("parts", partsArr);

            JSONArray contentsArr = new JSONArray();
            contentsArr.put(contentObj);

            JSONObject payload = new JSONObject();
            payload.put("contents", contentsArr);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(payload.toString());
            writer.flush();
            writer.close();

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String rawResponse = sb.toString().trim();
            if (code < 200 || code >= 300) {
                return "Gagal terhubung ke Google AI (" + code + "): " + rawResponse;
            }

            JSONObject json = new JSONObject(rawResponse);
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject first = candidates.getJSONObject(0);
                JSONObject content = first.optJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.optJSONArray("parts");
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "Tidak ada jawaban.");
                    }
                }
            }
            return "Respons Google AI kosong.";
        } catch (Exception e) {
            return "Terjadi kesalahan: " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
