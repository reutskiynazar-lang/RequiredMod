package com.requiredmod.requiredmod.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.requiredmod.requiredmod.RequiredMod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UpdateService {
    private static final String UPDATE_MANIFEST_URL = "https://raw.githubusercontent.com/your-org/RequiredMod/main/updates.json";
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String LOADER = "forge";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public CompletableFuture<Optional<UpdateInfo>> checkForUpdates(String currentVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(UPDATE_MANIFEST_URL))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    RequiredMod.LOGGER.warn("Update check failed with HTTP {}", response.statusCode());
                    return Optional.empty();
                }

                JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray versions = root.getAsJsonArray("versions");
                if (versions == null) {
                    return Optional.empty();
                }

                UpdateInfo newest = null;
                for (JsonElement element : versions) {
                    JsonObject item = element.getAsJsonObject();
                    String mc = item.get("minecraft").getAsString();
                    String loader = item.get("loader").getAsString();
                    if (!MINECRAFT_VERSION.equals(mc) || !LOADER.equalsIgnoreCase(loader)) {
                        continue;
                    }

                    UpdateInfo candidate = new UpdateInfo(
                            item.get("version").getAsString(),
                            mc,
                            loader,
                            item.get("download_url").getAsString(),
                            item.has("sha256") ? item.get("sha256").getAsString() : "",
                            item.has("changelog") ? item.get("changelog").getAsString() : "No changelog",
                            item.has("release_page") ? item.get("release_page").getAsString() : item.get("download_url").getAsString()
                    );

                    if (newest == null || VersionComparator.isNewer(newest.version(), candidate.version())) {
                        newest = candidate;
                    }
                }

                if (newest == null || !VersionComparator.isNewer(currentVersion, newest.version())) {
                    return Optional.empty();
                }
                return Optional.of(newest);
            } catch (Exception e) {
                RequiredMod.LOGGER.warn("Unable to check updates", e);
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Path> downloadUpdate(UpdateInfo updateInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path modsDirectory = FMLPaths.GAMEDIR.get().resolve("mods");
                Files.createDirectories(modsDirectory);

                String fileName = "requiredmod-" + updateInfo.version() + ".jar";
                Path tempFile = modsDirectory.resolve(fileName + ".tmp");
                Path finalFile = modsDirectory.resolve(fileName);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(updateInfo.downloadUrl()))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200) {
                    throw new IOException("Unexpected HTTP status: " + response.statusCode());
                }

                try (InputStream body = response.body()) {
                    Files.copy(body, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!updateInfo.sha256().isBlank()) {
                    String actualChecksum = sha256(tempFile);
                    if (!actualChecksum.equalsIgnoreCase(updateInfo.sha256())) {
                        Files.deleteIfExists(tempFile);
                        throw new IOException("SHA-256 mismatch for downloaded update");
                    }
                }

                Files.move(tempFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
                return finalFile;
            } catch (Exception e) {
                throw new RuntimeException("Failed to download update", e);
            }
        });
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(file);
        byte[] hashed = digest.digest(bytes);
        return HexFormat.of().formatHex(hashed);
    }
}