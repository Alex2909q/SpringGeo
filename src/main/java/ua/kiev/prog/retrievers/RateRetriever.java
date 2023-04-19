package ua.kiev.prog.retrievers;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.kiev.prog.json.Rate;

import java.time.LocalTime;

@Component
public class RateRetriever {
    private static final String URL = "https://api.apilayer.com/fixer/latest?symbols=UAH&base=EUR";
    private static final String KEY = "5uo4bYoYmLA2ua2EGk5hpC47qB6Gl4N4";
    private CacheManager cacheManager;

    public RateRetriever(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Cacheable("rates")
    @Scheduled(cron =  "0 * * * * *")
    public Rate getRate() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", KEY);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Rate> response = restTemplate.exchange(
                URL,
                HttpMethod.GET,
                entity,
                Rate.class
        );
        System.out.println("Method \"getRate\" is call at " + LocalTime.now()); // for tests
        return response.getBody();
    }

    /**
     * Method run every on 59 second to clear Cache with name "rates"
     */
    @CacheEvict(value = "rates", allEntries = true)
    @Scheduled( cron = "59 * * * * *")
    public void evictRateCache() {
        Cache cache = cacheManager.getCache("rates");
        if (cache != null) {
            cache.clear();
        }
        System.out.println("Clear cache " + LocalTime.now()); // for tests
    }
}
