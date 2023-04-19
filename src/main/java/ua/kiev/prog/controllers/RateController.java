package ua.kiev.prog.controllers;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.kiev.prog.dto.LocationDTO;
import ua.kiev.prog.json.Rate;
import ua.kiev.prog.retrievers.GeoRetriever;
import ua.kiev.prog.retrievers.RateRetriever;
import ua.kiev.prog.services.LocationService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RateController {
    private final RateRetriever rateRetriever;
    private final GeoRetriever geoRetriever;
    private final LocationService locationService;
    private CacheManager cacheManager;

    public RateController(RateRetriever rateRetriever, GeoRetriever geoRetriever,
                          LocationService locationService) {
        this.rateRetriever = rateRetriever;
        this.geoRetriever = geoRetriever;
        this.locationService = locationService;
    }
    @Cacheable("rates")
    @GetMapping("/rate")
    public Rate rate(HttpServletRequest request) { // Jackson
        String ip = request.getRemoteAddr();
        LocationDTO location = geoRetriever.getLocation(ip);
        locationService.save(location);
        Rate rate = rateRetriever.getRate();
        rate.setIp(ip);
        return rate;
    }
    @Scheduled(fixedRate = 60000)
    public void evictCache() {
        cacheManager.getCache("rates").clear();
    }
}
