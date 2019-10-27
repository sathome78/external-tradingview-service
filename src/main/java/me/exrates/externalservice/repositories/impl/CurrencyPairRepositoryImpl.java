package me.exrates.externalservice.repositories.impl;

import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.model.CurrencyPairDto;
import me.exrates.externalservice.repositories.CurrencyPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

@Log4j2
@Repository
public class CurrencyPairRepositoryImpl implements CurrencyPairRepository {

    private final NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    public CurrencyPairRepositoryImpl(@Qualifier("masterTemplate") NamedParameterJdbcOperations masterJdbcTemplate) {
        this.masterJdbcTemplate = masterJdbcTemplate;
    }

    @Override
    public List<CurrencyPairDto> getAllCurrencyPairs() {
        final String sql = "SELECT cp.name, cp.market, cp.hidden FROM CURRENCY_PAIR cp";

        return masterJdbcTemplate.query(sql, (rs, row) -> CurrencyPairDto.builder()
                .name(rs.getString("name"))
                .market(rs.getString("market"))
                .hidden(rs.getBoolean("hidden"))
                .build());
    }
}