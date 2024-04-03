package ru.andmosk;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class CrptApi {
    private static final int INITIAL_DELAY = 0;
    private static final Logger logger = Logger.getLogger(CrptApi.class.getName());

    private final Semaphore semaphore;
    private final String url;
    private final ScheduledExecutorService scheduledExecutorService;


    public CrptApi(TimeUnit timeUnit, int requestLimit, int intervalTime, String url) {
        this.semaphore = new Semaphore(requestLimit);
        this.url = url;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> semaphore.release(0),
                INITIAL_DELAY, intervalTime, timeUnit);
        logger.info("create semaphore and scheduledExecutorService");
    }

    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();
            logger.info("semaphore access has been");

            HttpResponse<String> response = httpRequest(document, signature);
            logger.info("document has been sent and response received from server, status: " + response.statusCode());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("interruption during document sending");
            throw new RuntimeException("interruption during document sending " + e.getMessage());
        } finally {
            semaphore.release();
            logger.info("semaphore released ");
        }
    }

    private HttpResponse<String> httpRequest(Document document, String signature) throws InterruptedException {
        HttpClient httpClient = createHttpClient();
        logger.info("create httpClient");

        HttpRequest request = createHttpRequest(document, signature);
        logger.info("create HttpRequest");

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.severe("error sending document");
            throw new RuntimeException("error sending document " + e.getMessage());
        }
    }

    private HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    private HttpRequest createHttpRequest(Document document, String signature) {
        String json = convertToJson(document);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private String convertToJson(Document document) {
        Gson gson = new Gson();
        return gson.toJson(document);
    }

    @Getter
    @Setter
    public static class Document {

        private Description description;
        private String docId;
        private String docStatus;
        private String docType;
        private Boolean importRequest;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private List<Product> products;
        private String regDate;
        private String regNumber;
    }

    @Getter
    @Setter
    public static class Description {
        private String participantInn;
    }

    @Getter
    @Setter
    public static class Product {
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;
    }
}
