package com.klm.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klm.weather.model.Weather;
import com.klm.weather.model.WeatherDTO;
import com.klm.weather.repository.WeatherRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class WeatherApiRestControllerTest {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper om = new ObjectMapper();
    @Autowired
    WeatherRepository weatherRepository;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        weatherRepository.deleteAll();
        om.setDateFormat(simpleDateFormat);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithPOST() throws Exception {
        Weather expectedRecord = getTestData().get("chicago");
        Weather actualRecord = om.readValue(mockMvc.perform(post("/weather")
                .contentType("application/json")
                .content(om.writeValueAsString(getTestData().get("chicago"))))
                .andDo(print())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class);

        Assertions.assertTrue(new ReflectionEquals(expectedRecord, "id").matches(actualRecord));
        assertEquals(true, weatherRepository.findById(actualRecord.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETList() throws Exception {
        Map<String, Weather> data = getTestData();
        data.remove("moscow2");
        List<Weather> expectedRecords = new ArrayList<>();

        for (Map.Entry<String, Weather> kv : data.entrySet()) {
            expectedRecords.add(om.readValue(mockMvc.perform(post("/weather")
                    .contentType("application/json")
                    .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class));
        }
        Collections.sort(expectedRecords, Comparator.comparing(Weather::getId));

        List<Weather> actualRecords = om.readValue(mockMvc.perform(get("/weather"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETListAndDateFilter() throws Exception {
        Date date = simpleDateFormat.parse("2019-03-12");
        Map<String, Weather> data = getTestData();
        data.remove("moscow2");
        List<Weather> expectedRecords = new ArrayList<>();

        for (Map.Entry<String, Weather> kv : data.entrySet()) {
            expectedRecords.add(om.readValue(mockMvc.perform(post("/weather")
                    .contentType("application/json")
                    .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class));
        }
        expectedRecords = expectedRecords.stream().filter(r -> r.getDate().equals(date)).collect(Collectors.toList());

        List<Weather> actualRecords = om.readValue(mockMvc.perform(get("/weather?date=2019-03-12"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$", greaterThan(0)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }

        mockMvc.perform(get("/weather?date=2015-06-06"))
                .andDo(print())
                .andExpect(jsonPath("$", isA(ArrayList.class)))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETListAndCityFilter() throws Exception {
        List<Weather> originalResponse = new ArrayList<>();

        for (Map.Entry<String, Weather> kv : getTestData().entrySet()) {
            originalResponse.add(om.readValue(mockMvc.perform(post("/weather")
                            .contentType("application/json")
                            .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class));
        }

        //test single
        List<Weather> expectedRecords = originalResponse.stream().filter(r -> r.getCity().toLowerCase().equals("moscow")).collect(Collectors.toList());
        List<Weather> actualRecords = om.readValue(mockMvc.perform(get("/weather?city=moscow"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }

        //test multiple
        expectedRecords = originalResponse.stream().filter(r -> ("moscow,London,ChicaGo").toLowerCase().contains(r.getCity().toLowerCase())).collect(Collectors.toList());

        actualRecords = om.readValue(mockMvc.perform(get("/weather?city=moscow,London,ChicaGo"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }

        //test none
        mockMvc.perform(get("/weather?city=berlin,amsterdam"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETListAndDateOrder() throws Exception {
        List<Weather> expectedRecords = new ArrayList<>();

        for (Map.Entry<String, Weather> kv : getTestData().entrySet()) {
            expectedRecords.add(om.readValue(mockMvc.perform(post("/weather")
                    .contentType("application/json")
                    .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class));
        }
        Collections.sort(expectedRecords, Comparator.comparing(Weather::getDate).thenComparing(Weather::getId));

            List<Weather> actualRecords = om.readValue(mockMvc.perform(get("/weather?sort=date"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }

        Collections.sort(expectedRecords, Comparator.comparing(Weather::getDate, Comparator.reverseOrder()).thenComparing(Weather::getId));

        actualRecords = om.readValue(mockMvc.perform(get("/weather?sort=-date"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", hasSize(expectedRecords.size())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<Weather>>() {
        });

        for (int i = 0; i < expectedRecords.size(); i++) {
            Assertions.assertTrue(new ReflectionEquals(expectedRecords.get(i)).matches(actualRecords.get(i)));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETById() throws Exception {
        Weather expectedRecord = om.readValue(mockMvc.perform(post("/weather")
                .contentType("application/json")
                .content(om.writeValueAsString(getTestData().get("chicago"))))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class);

        Weather actualRecord = om.readValue(mockMvc.perform(get("/weather/" + expectedRecord.getId())
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Weather.class);

        Assertions.assertTrue(new ReflectionEquals(expectedRecord).matches(actualRecord));

        mockMvc.perform(get("/weather/" + Integer.MAX_VALUE))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Map<String, Weather> getTestData() throws ParseException {
        Map<String, Weather> data = new LinkedHashMap<>();

        Weather chicago = new Weather(
                simpleDateFormat.parse("2019-06-11"),
                41.8818f,
                -87.6231f,
                "Chicago",
                "Illinois",
                Arrays.asList(24.0, 21.5, 24.0, 19.5, 25.5, 25.5, 24.0, 25.0, 23.0, 22.0, 18.0, 18.0, 23.5, 23.0, 23.0, 25.5, 21.0, 20.5, 20.0, 18.5, 20.5, 21.0, 25.0, 20.5));
        data.put("chicago", chicago);

        Weather oakland = new Weather(
                simpleDateFormat.parse("2019-06-12"),
                37.8043f,
                -122.2711f,
                "Oakland",
                "California",
                Arrays.asList(24.0, 36.0, 28.5, 29.0, 32.0, 36.0, 28.5, 34.5, 30.5, 31.5, 29.5, 27.0, 30.5, 23.5, 29.0, 22.0, 28.5, 32.5, 24.5, 28.5, 22.5, 35.0, 26.5, 32.5));
        data.put("oakland", oakland);

        Weather london = new Weather(
                simpleDateFormat.parse("2019-03-12"),
                51.5098f,
                -0.1180f,
                "London",
                "N/A",
                Arrays.asList(11.0, 11.0, 5.5, 7.0, 5.0, 5.5, 6.0, 9.5, 11.5, 5.0, 6.0, 8.0, 9.5, 5.0, 9.0, 9.5, 12.0, 6.0, 9.5, 8.5, 8.0, 8.0, 9.0, 6.5));
        data.put("london", london);

        Weather moscow1 = new Weather(
                simpleDateFormat.parse("2019-03-12"),
                55.7512f,
                37.6184f,
                "Moscow",
                "N/A",
                Arrays.asList(-2.0, -4.5, 1.0, -6.0, 1.0, 1.5, -9.0, -2.5, -3.0, -0.5, -13.5, -9.0, -11.5, -5.5, -5.5, -3.5, -14.0, -9.5, 1.5, -15.0, -6.5, -7.0, -13.5, -14.5));
        data.put("moscow1", moscow1);

        Weather moscow2 = new Weather(
                simpleDateFormat.parse("2019-03-12"),
                55.7512f,
                37.6184f,
                "Moscow",
                "N/A",
                Arrays.asList(-2.0, -4.5, 1.0, -6.0, 1.0, 1.5, -9.0, -2.5, -3.0, -0.5, -13.5, -9.0, -11.5, -5.5, -5.5, -3.5, -14.0, -9.5, 1.5, -15.0, -6.5, -7.0, -13.5, -14.5));
        data.put("moscow2", moscow2);

        return data;
    }

    //pagination

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETListAndDateFilterPage() throws Exception {
        Date date = simpleDateFormat.parse("2019-03-12"); // Parse the filter date
        Map<String, Weather> data = getTestData();
        data.remove("moscow2"); // Remove unwanted test data
        List<Weather> expectedRecords = new ArrayList<>();

        // Create weather records by posting them
        for (Map.Entry<String, Weather> kv : data.entrySet()) {
            expectedRecords.add(om.readValue(mockMvc.perform(post("/weather")
                            .contentType("application/json")
                            .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), Weather.class));
        }

        // Filter expected records based on the date
        expectedRecords = expectedRecords.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());

        // Perform GET request to fetch weather records with date filter
        MvcResult mvcResult = mockMvc.perform(get("/weather?date=2019-03-12"))
                .andDo(print())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0)))) // Ensure content is not empty
                .andExpect(jsonPath("$.totalElements", greaterThan(0))) // Ensure totalElements > 0
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize the content part of the response to List<WeatherDTO>
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = om.readTree(jsonResponse);
        JsonNode contentNode = rootNode.path("content");

        // Convert contentNode to List<WeatherDTO>
        List<WeatherDTO> actualRecords = om.readValue(contentNode.toString(), new TypeReference<List<WeatherDTO>>() {});

        // Create a PageImpl instance using the content and pagination details
        PageImpl<WeatherDTO> actualPage = new PageImpl<>(actualRecords,
                Pageable.unpaged(), // Use an unpaged Pageable, as pagination details can be handled manually
                rootNode.path("totalElements").asLong());

        // Compare the expected and actual records
        for (int i = 0; i < expectedRecords.size(); i++) {
            WeatherDTO expectedDTO = convertToWeatherDTO(expectedRecords.get(i)); // Convert Weather to WeatherDTO
            Assertions.assertTrue(new ReflectionEquals(expectedDTO).matches(actualRecords.get(i)));
        }

        // Test with a non-matching date to ensure no records are returned
        mockMvc.perform(get("/weather?date=2015-06-06"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class))) // Expect empty array for non-matching date
                .andExpect(jsonPath("$.totalElements", is(0))) // Ensure totalElements is 0
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWeatherEndpointWithGETListAndCityFilterWithPagination() throws Exception {
        List<Weather> originalResponse = new ArrayList<>();

        // Insert test data by posting weather records
        for (Map.Entry<String, Weather> kv : getTestData().entrySet()) {
            originalResponse.add(om.readValue(mockMvc.perform(post("/weather")
                            .contentType("application/json")
                            .content(om.writeValueAsString(kv.getValue())))
                    .andDo(print())
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Weather.class));
        }

        // Test for a single city (pagination is considered here as well)
        List<Weather> expectedRecords = originalResponse.stream()
                .filter(r -> r.getCity().toLowerCase().equals("moscow"))
                .collect(Collectors.toList());

        // Make a paginated GET request
        MvcResult mvcResult = mockMvc.perform(get("/weather?city=moscow&page=0&size=2"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.content", hasSize(2)))  // Adjust the size based on your expected number of records per page
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))  // Total records should be > 0
                .andExpect(jsonPath("$.totalPages", greaterThan(0)))  // Ensure there are multiple pages if there are enough records
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize content from response
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = om.readTree(jsonResponse);
        JsonNode contentNode = rootNode.path("content");

        List<WeatherDTO> actualRecords = om.readValue(contentNode.toString(), new TypeReference<List<WeatherDTO>>() {});

        // Create a PageImpl instance using content and pagination details
        PageImpl<WeatherDTO> actualPage = new PageImpl<>(actualRecords,
                Pageable.ofSize(2), // Assume size=2 per page
                rootNode.path("totalElements").asLong());

        // Verify that the correct number of records is returned
        Assertions.assertEquals(expectedRecords.size(), actualPage.getTotalElements(), "Total records should match");

        // Compare expected and actual records for each page
        for (int i = 0; i < actualPage.getContent().size(); i++) {
            WeatherDTO expectedDTO = convertToWeatherDTO(expectedRecords.get(i));  // Convert Weather to WeatherDTO
            Assertions.assertTrue(new ReflectionEquals(expectedDTO).matches(actualPage.getContent().get(i)));
        }

        // Test for multiple cities with pagination
        expectedRecords = originalResponse.stream()
                .filter(r -> ("moscow,London,Chicago").toLowerCase().contains(r.getCity().toLowerCase()))
                .collect(Collectors.toList());

        mvcResult = mockMvc.perform(get("/weather?city=moscow,London,Chicago&page=0&size=2"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))
                .andExpect(jsonPath("$.content", hasSize(2)))  // Ensure it returns two records per page
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))  // Ensure the totalElements count is correct
                .andExpect(jsonPath("$.totalPages", greaterThan(0)))  // Ensure there are multiple pages if there are enough records
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize content from response for multiple cities
        jsonResponse = mvcResult.getResponse().getContentAsString();
        rootNode = om.readTree(jsonResponse);
        contentNode = rootNode.path("content");

        actualRecords = om.readValue(contentNode.toString(), new TypeReference<List<WeatherDTO>>() {});

        actualPage = new PageImpl<>(actualRecords,
                Pageable.ofSize(2), // Page size of 2
                rootNode.path("totalElements").asLong());

        // Verify the total number of records and page size
        Assertions.assertEquals(expectedRecords.size(), actualPage.getTotalElements(), "Total records should match for multiple cities");

        // Compare expected and actual records for each page
        for (int i = 0; i < actualPage.getContent().size(); i++) {
            WeatherDTO expectedDTO = convertToWeatherDTO(expectedRecords.get(i));  // Convert Weather to WeatherDTO
            Assertions.assertTrue(new ReflectionEquals(expectedDTO).matches(actualPage.getContent().get(i)));
        }

        // Test for no matching cities with pagination (ensure it returns an empty list)
        mockMvc.perform(get("/weather?city=berlin,amsterdam&page=0&size=2"))
                .andDo(print())
                .andExpect(jsonPath("$.content", isA(ArrayList.class)))  // Ensure empty array
                .andExpect(jsonPath("$.content", hasSize(0)))  // No content should be returned
                .andExpect(jsonPath("$.totalElements", is(0)))  // totalElements should be 0
                .andExpect(jsonPath("$.totalPages", is(0)))  // totalPages should also be 0
                .andExpect(status().isOk());
    }


    private WeatherDTO convertToWeatherDTO(Weather weather) {
        return new WeatherDTO(
                weather.getId(),
                weather.getDate(),  // No conversion needed, Date is retained
                weather.getLat(),
                weather.getLon(),
                weather.getCity(),
                weather.getState(),
                weather.getTemperatures()
        );
    }


}
