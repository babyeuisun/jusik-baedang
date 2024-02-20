package com.example.jusik.scraper;

import com.example.jusik.model.Company;
import com.example.jusik.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
