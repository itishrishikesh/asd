package allure.sample.plugin;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Widget;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomPlugin implements Aggregator, Widget {

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launches,
                          final Path outputDirectory) throws IOException {
        final JacksonContext jacksonContext = configuration
                .requireContext(JacksonContext.class);
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve("data"));
        final Path dataFile = dataFolder.resolve("myplugindata.json");
        final Stream<TestResult> resultsStream = launches.stream()
                .flatMap(launch -> launch.getAllResults().stream());
        try (OutputStream os = Files.newOutputStream(dataFile)) {
            jacksonContext.getValue().writeValue(os, extractData(resultsStream));
        }
    }

    private Collection<Map> extractData(final Stream<TestResult> testResults) {
        // Collect all the test results into a list
        List<TestResult> results = testResults.collect(Collectors.toList());
        // Initialize a new collection to hold the extracted data
        Collection<Map> extractedData = new ArrayList<>();
        // Iterate over the test results
        for (TestResult result : results) {
            // Create a new map to hold the data for this test result
            Map<String, Object> data = new HashMap<String, Object>();
            // Extract the data that you need from the test result and add it to the map
            data.put("name", result.getName());
            data.put("status", result.getStatus().toString());
            // Add the map to the collection of extracted data
            extractedData.add(data);
        }
        // Return the collection of extracted data
        return extractedData;
    }

    @Override
    public Object getData(Configuration configuration, List<LaunchResults> launches) {
        Stream<TestResult> filteredResults = launches.stream().flatMap(launch -> launch.getAllResults().stream())
                .filter(result -> result.getStatus().equals(Status.FAILED));
        return extractData(filteredResults);
    }

    @Override
    public String getName() {
        return "mywidget";
    }
}
