package com.example.jusik.service;

import com.example.jusik.exception.impl.NoCompanyException;
import com.example.jusik.model.Company;
import com.example.jusik.model.Dividend;
import com.example.jusik.model.ScrapedResult;
import com.example.jusik.model.constants.CacheKey;
import com.example.jusik.persistance.CompanyRepository;
import com.example.jusik.persistance.DividendRepository;
import com.example.jusik.persistance.entity.CompanyEntity;
import com.example.jusik.persistance.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    @Cacheable(value = CacheKey.KEY_FINANCE,key = "#companyName")
    public ScrapedResult getDividendByCompanyName(String companyName){
        log.info("cache");
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(()->new NoCompanyException());
        List< DividendEntity> dividendEntities= this.dividendRepository.findAllByCompanyId(company.getId());

        List<Dividend> dividends = dividendEntities.stream()
                .map(e ->  new Dividend(e.getDate(),e.getDividend()))
                .collect(Collectors.toList());
        return new ScrapedResult(new Company(company.getTicker(),company.getName()),dividends);

    }
}
