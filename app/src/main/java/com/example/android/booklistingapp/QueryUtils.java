/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.booklistingapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from GOOGLE BOOK.
 */
public final class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    @NonNull
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    @Nullable
    private static List<Book> extractFeatureFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding book to
        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            if(baseJsonResponse.has("items")){
                // Extract the JSONArray associated with the key called "items",
                // which represents a list of books.
                JSONArray bookArray = baseJsonResponse.getJSONArray("items");

                // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
                for (int i = 0; i < bookArray.length(); i++) {

                    // Get a single earthquake at position i within the list of earthquakes
                    JSONObject currentBook = bookArray.getJSONObject(i);

                    // For a given earthquake, extract the JSONObject associated with the
                    // key called "properties", which represents a list of all properties
                    // for that earthquake.
                    JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                    //Extract the imageLinks JSONObject
                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                    String thumbnail;
                    if (imageLinks.has("thumbnail")){
                        //Extract the url of the smallThumbnail
                        thumbnail = imageLinks.getString("thumbnail");
                    }else {thumbnail = "No Image";}

                    // Extract the value for the key called "title"
                    String title;
                    if (volumeInfo.has("title")) {
                        title = volumeInfo.getString("title");
                    } else {
                        title = "No Title";
                    }

                    JSONArray authorsArray;
                    ArrayList<String> authors = new ArrayList<String>();

                    if (volumeInfo.has("authors")) {
                        authorsArray = volumeInfo.getJSONArray("authors");
                        for (int j=0; j<authorsArray.length(); j++) {
                            authors.add(authorsArray.getString(j));
                        }
                    } else {
                        authors.add("Uknown Author");
                    }

                    // Extract the value for the key called "subtitle"
                    String subtitle;
                    if (volumeInfo.has("publisher")) {
                        subtitle = volumeInfo.getString("subtitle");
                    } else {
                        subtitle = "No Subtitle";
                    }

                    // Extract the value for the key called "publisher"
                    String publisher;
                    if (volumeInfo.has("publisher")) {
                        publisher = volumeInfo.getString("publisher");
                    } else {
                        publisher = "No Publisher";
                    }

                    // Extract the value for the key called "publisher"
                    String infoLink;
                    if (volumeInfo.has("infoLink")) {
                        infoLink = volumeInfo.getString("infoLink");
                    } else {
                        infoLink = "No info link";
                    }

                    // Create a new {@link Book} object with the info,
                    // and url from the JSON response.
                    Book book = new Book(thumbnail,title,subtitle,authors,publisher,infoLink);

                    // Add the new {@link Earthquake} to the list of earthquakes.
                    books.add(book);
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return books;
    }

    /**
     * Query the Google Books dataset and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Book> books = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Book}s
        return books;
    }
}