package com.example.jusik.scheduler;

import com.example.jusik.model.Company;
import com.example.jusik.model.ScrapedResult;
import com.example.jusik.model.constants.CacheKey;
import com.example.jusik.persistance.CompanyRepository;
import com.example.jusik.persistance.DividendRepository;
import com.example.jusik.persistance.entity.CompanyEntity;
import com.example.jusik.persistance.entity.DividendEntity;
import com.example.jusik.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
@EnableCaching
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;

    @CacheEvict(value = CacheKey.KEY_FINANCE,allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("start");
        List< CompanyEntity> companies = this.companyRepository.findAll();

        for(var company:companies){
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));
            scrapedResult.getDividends().stream()
                    .map(e -> new DividendEntity(company.getId(),e))
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(),e.getDate());
                        if (!exists){
                            this.dividendRepository.save(e);
                        }

                    });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
