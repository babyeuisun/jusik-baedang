package com.example.jusik.service;

import com.example.jusik.exception.impl.NoCompanyException;
import com.example.jusik.model.Company;
import com.example.jusik.model.ScrapedResult;
import com.example.jusik.persistance.CompanyRepository;
import com.example.jusik.persistance.DividendRepository;
import com.example.jusik.persistance.entity.CompanyEntity;
import com.example.jusik.persistance.entity.DividendEntity;
import com.example.jusik.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {
    private final Scraper YahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Trie trie;
    public Company save(String ticker){
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists){
            throw new RuntimeException("already");
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRepository.findAll(pageable);
    }
    private Company storeCompanyAndDividend(String ticker){
        Company company = this.YahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed");
        }
        ScrapedResult scrapedResult = this.YahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public void addAutoCompleteKeyword(String keyword){
        this.trie.put(keyword,null);
    }
    public List<String> autocomplete(String keyword){
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }

    public List<String> getCompanyNameByKeyword(String keyword){
        Pageable limit = PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword,limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());

    }
    public void deleteAutoCompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker){
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(()-> new NoCompanyException());
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);
        this.deleteAutoCompleteKeyword(company.getName());
        return company.getName();

    }
}
