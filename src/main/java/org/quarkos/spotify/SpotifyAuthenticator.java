package org.quarkos.spotify;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.Desktop;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import org.apache.hc.core5.http.ParseException;

/**
 * Handles the OAuth 2.0 Authorization Code Flow for the Spotify API.
 * This class opens a browser for the user to log in and then
 * runs a temporary local server to catch the redirect and exchange the
 * authorization code for an access token.
 */
public class SpotifyAuthenticator {

    private static final Dotenv dotenv = Dotenv.load();
    private static final URI redirectUri = URI.create("http://127.0.0.1:8888/callback");
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthenticator.class);

    private final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(dotenv.get("SPOTIFY_CLIENT_ID"))
            .setClientSecret(dotenv.get("SPOTIFY_CLIENT_SECRET"))
            .setRedirectUri(redirectUri)
            .build();

    /**
     * The main authentication method. It orchestrates the entire OAuth 2.0 flow.
     * This method will block program execution until authentication is complete or has failed.
     */
    public void authenticate() {
        try {
            // Step 1: Build the authorization URI with the necessary scopes.
            AuthorizationCodeUriRequest uriRequest = spotifyApi.authorizationCodeUri()
                    .scope("user-read-private,user-read-email,playlist-read-private,app-remote-control,streaming,user-modify-playback-state,user-read-playback-state")
                    .build();

            URI authorizationURI = uriRequest.execute();

            // Step 2: Direct the user to the authorization URL.
            logger.info("Please log in with Spotify by visiting this URL in your browser:");
            logger.info(authorizationURI.toString());

            // Try to open the URL automatically in the user's default browser.
            openInBrowser(authorizationURI);

            // Step 3: Start a temporary local server to listen for the callback from Spotify.
            // This server will receive the authorization code.
            startServerAndExchangeCodeForTokens();

            logger.info("Authentication successful!");
            logger.info("Access Token: " + spotifyApi.getAccessToken().substring(0, 15) + "...");
            logger.info("Ready to make API calls.");

        } catch (Exception e) {
            logger.error("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * A helper method to attempt to open a URI in the default system browser.
     * @param uri The URI to open.
     */
    private void openInBrowser(URI uri) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                logger.info("Attempting to open the login page in your browser...");
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                logger.warn("Could not automatically open the browser. Please copy the link manually.", e);
            }
        }
    }

    /**
     * Starts a simple server on the redirect URI port, waits for the Spotify callback,
     * parses the authorization code, and exchanges it for access and refresh tokens.
     */
    private void startServerAndExchangeCodeForTokens() throws IOException, SpotifyWebApiException, ParseException {
        // The server will close itself after handling one request.
        try (ServerSocket serverSocket = new ServerSocket(redirectUri.getPort())) {
            logger.info("Waiting for Spotify callback on port " + serverSocket.getLocalPort() + "...");

            // This blocks until a connection is made.
            Socket clientSocket = serverSocket.accept();
            logger.info("Connection received from Spotify.");

            // Read the raw HTTP request to find the authorization code.
            byte[] buffer = new byte[1024];
            int read = clientSocket.getInputStream().read(buffer);
            String rawRequest = new String(buffer, 0, read);

            // Simple parsing to extract the code from the request line (e.g., "GET /callback?code=... HTTP/1.1").
            String code = rawRequest.split("code=")[1].split(" ")[0];
            logger.info("Authorization code extracted successfully.");

            // Send a user-friendly response to the browser and close the connection.
            String response = "HTTP/1.1 200 OK\r\n\r\n<html><body style='font-family: sans-serif; text-align: center;'>" +
                            "<h1>Success!</h1><p>You can close this tab now.</p>" +
                            "<script>window.close();</script></body></html>";
            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.close();

            // Step 4: Exchange the received code for API tokens.
            logger.info("Exchanging authorization code for an access token...");
            AuthorizationCodeRequest codeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials credentials = codeRequest.execute();

            // Step 5: Set the tokens on the SpotifyApi instance, making it ready for use.
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());
        }
    }

    /**
     * Returns the configured SpotifyApi instance. Call this *after* authenticate().
     * @return The SpotifyApi instance, ready to be used.
     */
    public SpotifyApi getSpotifyApi() {
        return this.spotifyApi;
    }
}