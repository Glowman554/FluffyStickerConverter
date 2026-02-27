package de.toxicfox.foxbot.convert.filething;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import de.glowman554.bot.utils.StreamedFile;
import net.shadew.json.Json;
import net.shadew.json.JsonNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileThingApi {
    private final OkHttpClient client = new OkHttpClient();
    private final String uploadServer;
    private final String authToken;
    private final Json json = Json.json();

    public FileThingApi(String uploadServer, String authToken) {
        this.uploadServer = uploadServer;
        this.authToken = authToken;
    }

    public UploadResult prepareUpload(String fileName) {
        Request request = new Request.Builder()
                .url(uploadServer + "/api/v1/prepare")
                .post(RequestBody.create(json.serialize(JsonNode.object().set("name", fileName)), okhttp3.MediaType.parse("application/json")))
                .header("Authentication", authToken)
                .build();
        try (Response res = client.newCall(request).execute()) {
            if (!res.isSuccessful()) {
                throw new RuntimeException("Failed to prepare upload");
            }

            JsonNode parsed = json.parse(Objects.requireNonNull(res.body()).string());
            UploadResult result = new UploadResult();
            result.fromJSON(parsed);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare upload", e);
        }
    }

    public void deleteFile(String fileId) {
        Request request = new Request.Builder()
                .url(uploadServer + "/api/v1/delete/" + fileId)
                .header("Authentication", authToken)
                .build();
        try (Response res = client.newCall(request).execute()) {
            if (!res.isSuccessful()) {
                throw new RuntimeException("Failed to delete file");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public String idFromUrl(String url) {
        if (!url.startsWith(uploadServer)) {
            return null;
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }


    public void upload(StreamedFile file, UploadResult prepared) {
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                try (InputStream in = file.getStream()) {
                    sink.writeAll(okio.Okio.source(in));
                }
            }
        };

        Request request = new Request.Builder()
                .url(prepared.getUrl())
                .post(body)
                .addHeader("Authentication", prepared.getUploadToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException(String.format("Failed to upload file: HTTP %d %s", response.code(), response.message()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
