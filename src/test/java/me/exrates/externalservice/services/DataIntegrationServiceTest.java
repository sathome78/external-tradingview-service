package me.exrates.externalservice.services;

import me.exrates.externalservice.api.ChartApi;
import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.model.QuotesDto;
import me.exrates.externalservice.services.impl.DataIntegrationServiceImpl;
import me.exrates.externalservice.utils.ResolutionUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class DataIntegrationServiceTest extends AbstractTest {

    @Mock
    private CurrencyPairService currencyPairService;
    @Mock
    private ChartApi chartApi;
    @Mock
    private ResolutionUtil resolutionUtil;

    private DataIntegrationService dataIntegrationService;

    @Before
    public void setUp() throws Exception {
        dataIntegrationService = spy(new DataIntegrationServiceImpl(
                100,
                currencyPairService,
                chartApi,
                resolutionUtil));
    }

    @Test
    public void getSymbolInfo_ok() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();

        Map<String, Object> data = dataIntegrationService.getSymbolInfo();

        assertNotNull(data);
        assertFalse(data.isEmpty());
        List<String> symbols = (List<String>) data.get("symbol");
        assertNotNull(symbols);
        assertFalse(symbols.isEmpty());
        assertEquals(1, symbols.size());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
    }

    @Test
    public void getSymbolInfo_empty_map() {
        doReturn(Collections.emptyMap())
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();

        Map<String, Object> data = dataIntegrationService.getSymbolInfo();

        assertNotNull(data);
        assertFalse(data.isEmpty());
        List<String> symbols = (List<String>) data.get("symbol");
        assertNotNull(symbols);
        assertTrue(symbols.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
    }

    @Test
    public void getQuotes_ok() {
        TickerResponse tickerResponse = new TickerResponse();
        tickerResponse.setCurrencyPairId(1);
        tickerResponse.setCurrencyPairName(CONVERTED_TEST_PAIR);
        tickerResponse.setFirst(BigDecimal.ONE);
        tickerResponse.setLast(BigDecimal.ONE);
        tickerResponse.setBaseVolume(BigDecimal.ONE);
        tickerResponse.setQuoteVolume(BigDecimal.ONE);
        tickerResponse.setHigh24hr(BigDecimal.ONE);
        tickerResponse.setLow24hr(BigDecimal.ONE);
        tickerResponse.setIsFrozen(0);
        tickerResponse.setPercentChange(BigDecimal.ONE);
        tickerResponse.setValueChange(BigDecimal.ONE);
        tickerResponse.setLowestAsk(BigDecimal.ONE);
        tickerResponse.setHighestBid(BigDecimal.ONE);

        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doReturn(tickerResponse)
                .when(chartApi)
                .getCachedTickerInfo(anyString());

        List<QuotesDto> data = dataIntegrationService.getQuotes(Collections.singletonList(TEST_PAIR));

        assertNotNull(data);
        assertFalse(data.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(chartApi, atLeastOnce()).getCachedTickerInfo(anyString());
    }

    @Test
    public void getQuotes_empty_list() {
        List<QuotesDto> data = dataIntegrationService.getQuotes(Collections.emptyList());

        assertNotNull(data);
        assertTrue(data.isEmpty());

        verify(currencyPairService, never()).getCachedActiveCurrencyPairs();
        verify(chartApi, never()).getCachedTickerInfo(anyString());
    }

    @Test
    public void getQuotes_api_response_is_null() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doReturn(null)
                .when(chartApi)
                .getCachedTickerInfo(anyString());

        List<QuotesDto> data = dataIntegrationService.getQuotes(Collections.singletonList(TEST_PAIR));

        assertNotNull(data);
        assertTrue(data.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(chartApi, atLeastOnce()).getCachedTickerInfo(anyString());
    }

    @Test
    public void getHistory_ok() {
        CandleResponse candleResponse = new CandleResponse();
        candleResponse.setTime(NOW);
        candleResponse.setOpen(BigDecimal.ONE);
        candleResponse.setClose(BigDecimal.ONE);
        candleResponse.setHigh(BigDecimal.ONE);
        candleResponse.setLow(BigDecimal.ONE);
        candleResponse.setVolume(BigDecimal.ONE);

        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doNothing()
                .when(resolutionUtil)
                .check(anyString());
        doReturn(Collections.singletonList(candleResponse))
                .when(chartApi)
                .getCandleChartData(anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyString());

        Map<String, Object> data = dataIntegrationService.getHistory(TEST_PAIR, NOW.minusDays(1), NOW, null, "D");

        assertNotNull(data);
        assertFalse(data.isEmpty());

        List<Long> times = (List<Long>) data.get("t");
        assertNotNull(times);
        assertFalse(times.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(resolutionUtil, atLeastOnce()).check(anyString());
        verify(chartApi, atLeastOnce()).getCandleChartData(anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyString());
    }

    @Test
    public void getHistory_symbol_not_found() {
        CandleResponse candleResponse = new CandleResponse();
        candleResponse.setTime(NOW);
        candleResponse.setOpen(BigDecimal.ONE);
        candleResponse.setClose(BigDecimal.ONE);
        candleResponse.setHigh(BigDecimal.ONE);
        candleResponse.setLow(BigDecimal.ONE);
        candleResponse.setVolume(BigDecimal.ONE);

        doReturn(Collections.emptyMap())
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();

        Map<String, Object> data = dataIntegrationService.getHistory(TEST_PAIR, NOW.minusDays(1), NOW, null, "D");

        assertNotNull(data);
        assertTrue(data.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(resolutionUtil, never()).check(anyString());
        verify(chartApi, never()).getCandleChartData(anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyString());
    }

    @Test(expected = Exception.class)
    public void getHistory_resolution_failed() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doThrow(new Exception())
                .when(resolutionUtil)
                .check(anyString());

        dataIntegrationService.getHistory(TEST_PAIR, NOW.minusDays(1), NOW, null, "D");
    }

    @Test
    public void getHistory_candles_list_is_empty() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doNothing()
                .when(resolutionUtil)
                .check(anyString());
        doReturn(Collections.emptyList())
                .when(chartApi)
                .getCandleChartData(anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyString());

        Map<String, Object> data = dataIntegrationService.getHistory(TEST_PAIR, NOW.minusDays(1), NOW, null, "D");

        assertNotNull(data);
        assertFalse(data.isEmpty());

        List<Long> times = (List<Long>) data.get("t");
        assertNotNull(times);
        assertTrue(times.isEmpty());

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(resolutionUtil, atLeastOnce()).check(anyString());
        verify(chartApi, atLeastOnce()).getCandleChartData(anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyString());
    }

    @Test
    public void getLastCandleTimeBeforeDate_ok() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doReturn(NOW)
                .when(chartApi)
                .getLastCandleTimeBeforeDate(anyString(), any(LocalDateTime.class), anyString());

        LocalDateTime dateTime = dataIntegrationService.getLastCandleTimeBeforeDate(TEST_PAIR, NOW, "D");

        assertNotNull(dateTime);
        assertEquals(NOW, dateTime);

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(chartApi, atLeastOnce()).getLastCandleTimeBeforeDate(anyString(), any(LocalDateTime.class), anyString());
    }

    @Test
    public void getLastCandleTimeBeforeDate_symbol_not_found() {
        doReturn(Collections.emptyMap())
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();

        LocalDateTime dateTime = dataIntegrationService.getLastCandleTimeBeforeDate(TEST_PAIR, NOW, "D");

        assertNull(dateTime);

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(chartApi, never()).getLastCandleTimeBeforeDate(anyString(), any(LocalDateTime.class), anyString());
    }

    @Test
    public void getLastCandleTimeBeforeDate_date_is_null() {
        doReturn(Collections.singletonMap(TEST_PAIR, CONVERTED_TEST_PAIR))
                .when(currencyPairService)
                .getCachedActiveCurrencyPairs();
        doReturn(null)
                .when(chartApi)
                .getLastCandleTimeBeforeDate(anyString(), any(LocalDateTime.class), anyString());

        LocalDateTime dateTime = dataIntegrationService.getLastCandleTimeBeforeDate(TEST_PAIR, NOW, "D");

        assertNull(dateTime);

        verify(currencyPairService, atLeastOnce()).getCachedActiveCurrencyPairs();
        verify(chartApi, atLeastOnce()).getLastCandleTimeBeforeDate(anyString(), any(LocalDateTime.class), anyString());
    }

    @Test
    public void getLongPoolingResult_queue_is_not_empty() {
        BlockingQueue<String> bufferQueue = new LinkedBlockingDeque<>();
        bufferQueue.add("result");

        ReflectionTestUtils.setField(dataIntegrationService, "bufferQueue", bufferQueue);

        String result = dataIntegrationService.getLongPoolingResult();

        assertNotNull(result);
        assertEquals("result\n", result);
    }

    @Test
    public void getLongPoolingResult_queue_is_empty() {
        String result = dataIntegrationService.getLongPoolingResult();

        assertNotNull(result);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void getBufferQueue_is_not_empty() {
        BlockingQueue<String> bufferQueue = new LinkedBlockingDeque<>();
        bufferQueue.add("result");

        ReflectionTestUtils.setField(dataIntegrationService, "bufferQueue", bufferQueue);

        BlockingQueue<String> queue = dataIntegrationService.getBufferQueue();

        assertNotNull(queue);
        assertFalse(queue.isEmpty());
    }

    @Test
    public void getBufferQueue_is_empty() {
        BlockingQueue<String> queue = dataIntegrationService.getBufferQueue();

        assertNotNull(queue);
        assertTrue(queue.isEmpty());
    }
}