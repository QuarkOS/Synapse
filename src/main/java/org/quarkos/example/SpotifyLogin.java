package org.quarkos.example;

import org.quarkos.spotify.SpotifyAuthenticator;
import se.michaelthelin.spotify.SpotifyApi;

public class SpotifyLogin {
    public static void main(String[] args) {
        SpotifyAuthenticator authenticator = new SpotifyAuthenticator();

        // This call will now block and handle the entire process.
        // The program will pause here until the user has logged in
        // and the tokens have been received.
        authenticator.authenticate();

        // No need for Thread.sleep() anymore!
        System.out.println("Ready to use the API.");

        // Now you can get the authenticated API object and use it.
        SpotifyApi spotifyApi = authenticator.getSpotifyApi();

        // Example: Get the current user's profile
        try {
            System.out.println("Your display name is: " + spotifyApi.getCurrentUsersProfile().build().execute().getDisplayName());
        } catch (Exception e) {
            System.out.println("Error fetching profile: " + e.getMessage());
        }
    }
}
