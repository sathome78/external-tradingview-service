package me.exrates.externalservice.services;

import me.exrates.externalservice.model.CurrencyPairDto;
import me.exrates.externalservice.repositories.CurrencyPairRepository;
import me.exrates.externalservice.services.impl.CurrencyPairServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.cache.Cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CurrencyPairServiceTest extends AbstractTest {

    @Mock
    private CurrencyPairRepository currencyPairRepository;
    @Mock
    private Cache currencyPairsCache;

    private CurrencyPairService currencyPairService;

    @Before
    public void setUp() throws Exception {
        currencyPairService = spy(new CurrencyPairServiceImpl(currencyPairRepository, currencyPairsCache));
    }

    @Test
    public void getCachedActiveCurrencyPairs_ok() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairsCache)
                .get(any(), any(Callable.class));

        Map<String, String> pairs = currencyPairService.getCachedActiveCurrencyPairs();

        assertNotNull(pairs);
        assertFalse(pairs.isEmpty());
        assertEquals(CONVERTED_TEST_PAIR, pairs.get(TEST_PAIR));

        verify(currencyPairsCache, atLeastOnce()).get(any(), any(Callable.class));
    }

    @Test
    public void getActiveCurrencyPairs_ok() {
        doReturn(Collections.singletonList(CurrencyPairDto.builder()
                .name(CONVERTED_TEST_PAIR)
                .hidden(false)
                .build()))
                .when(currencyPairRepository)
                .getAllCurrencyPairs();

        Map<String, String> pairs = currencyPairService.getActiveCurrencyPairs();

        assertNotNull(pairs);
        assertFalse(pairs.isEmpty());
        assertEquals(CONVERTED_TEST_PAIR, pairs.get(TEST_PAIR));

        verify(currencyPairRepository, atLeastOnce()).getAllCurrencyPairs();
    }

    @Test
    public void getActiveCurrencyPairs_is_empty_list() {
        doReturn(Collections.emptyList())
                .when(currencyPairRepository)
                .getAllCurrencyPairs();

        Map<String, String> pairs = currencyPairService.getActiveCurrencyPairs();

        assertNotNull(pairs);
        assertTrue(pairs.isEmpty());

        verify(currencyPairRepository, atLeastOnce()).getAllCurrencyPairs();
    }
}