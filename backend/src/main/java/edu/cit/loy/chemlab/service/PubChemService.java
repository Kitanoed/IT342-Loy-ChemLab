package edu.cit.loy.chemlab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.loy.chemlab.dto.inventory.PubChemLookupResponse;
import edu.cit.loy.chemlab.entity.InventoryItem;
import edu.cit.loy.chemlab.exception.InventoryApiException;
import edu.cit.loy.chemlab.repository.InventoryItemRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class PubChemService {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s,().\\-]{2,120}$");
    private static final int LIMIT_PER_MINUTE = 30;

    private final InventoryItemRepository inventoryItemRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final Map<String, RateWindow> rateMap = new ConcurrentHashMap<>();

    public PubChemService(InventoryItemRepository inventoryItemRepository, ObjectMapper objectMapper) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public PubChemLookupResponse lookupByName(String rawName, String clientKey) {
        String chemicalName = sanitizeChemicalName(rawName);
        enforceRateLimit(clientKey == null ? "anonymous" : clientKey);

        Optional<InventoryItem> cached = Optional.empty();
        try {
            cached = inventoryItemRepository.findFirstByItemNameIgnoreCaseAndArchivedFalse(chemicalName)
                .filter(item -> item.getPubchemCid() != null);
        } catch (DataAccessException ignored) {
        }

        if (cached.isPresent()) {
            InventoryItem item = cached.get();
            return new PubChemLookupResponse(
                    item.getPubchemCid(),
                    item.getMolecularFormula(),
                    item.getMolecularWeight(),
                    item.getIupacName(),
                    "supabase-cache"
            );
        }

        try {
            String encodedName = URLEncoder.encode(chemicalName, StandardCharsets.UTF_8);
            String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + encodedName + "/property/MolecularFormula,MolecularWeight,IUPACName/JSON";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new InventoryApiException("PUBCHEM_NOT_FOUND", 404, "Chemical not found in PubChem.");
            }
            if (response.statusCode() >= 400) {
                throw new InventoryApiException("PUBCHEM_ERROR", 502, "PubChem service returned an error.");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode first = root.path("PropertyTable").path("Properties").path(0);

            int cidValue = first.path("CID").asInt(0);
            if (cidValue == 0) {
                throw new InventoryApiException("PUBCHEM_PARSE_ERROR", 502, "Unable to parse PubChem response.");
            }

            Integer cid = cidValue;
            String formula = first.path("MolecularFormula").asText(null);
            String weight = first.path("MolecularWeight").asText(null);
            String iupac = first.path("IUPACName").asText(null);

            return new PubChemLookupResponse(cid, formula, weight, iupac, "pubchem");
        } catch (InventoryApiException ex) {
            throw ex;
        } catch (java.net.http.HttpTimeoutException ex) {
            throw new InventoryApiException("PUBCHEM_TIMEOUT", 504, "PubChem request timed out.");
        } catch (java.io.IOException ex) {
            throw new InventoryApiException("PUBCHEM_INTEGRATION_ERROR", 502, "Failed to fetch chemical data.", ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InventoryApiException("PUBCHEM_INTEGRATION_ERROR", 502, "Failed to fetch chemical data.", ex.getMessage());
        }
    }

    public String sanitizeChemicalName(String rawName) {
        if (rawName == null) {
            throw new InventoryApiException("INVALID_INPUT", 400, "Chemical name is required.");
        }

        String normalized = rawName.trim().replaceAll("\\s+", " ");

        if (!NAME_PATTERN.matcher(normalized).matches()) {
            throw new InventoryApiException("INVALID_INPUT", 400, "Chemical name contains invalid characters or length.");
        }

        return normalized;
    }

    private void enforceRateLimit(String key) {
        long now = System.currentTimeMillis();
        RateWindow current = rateMap.computeIfAbsent(key, k -> new RateWindow(now, 0));

        synchronized (current) {
            if (now - current.windowStartMs > 60_000) {
                current.windowStartMs = now;
                current.count = 0;
            }

            current.count++;
            if (current.count > LIMIT_PER_MINUTE) {
                throw new InventoryApiException("RATE_LIMITED", 429, "Too many PubChem requests. Please retry shortly.");
            }
        }
    }

    private static class RateWindow {
        private long windowStartMs;
        private int count;

        private RateWindow(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
