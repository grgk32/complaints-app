package com.example.complaints.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Service
@Slf4j
public class CountryLookupService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String defaultCountry;

    public CountryLookupService(@Value("${complaints.default-country:Unknown}") String defaultCountry) {
        this.defaultCountry = defaultCountry;
    }

    @Cacheable(value = "ipCache", key = "#ip", unless="#result == null")
    public String getCountryFromIp(String ip) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("http://ip-api.com/json/" + ip)
                    .toUriString();
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            log.error("Failed to get country for IP {}: {}", ip, e.getMessage());
        }
        log.warn("Falling back to default country for IP {}: {}", ip, defaultCountry);
        return defaultCountry;
    }
}
