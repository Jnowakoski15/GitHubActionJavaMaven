package com.nowakoski.demo.Controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

@RestController
public class HelloController {

    @RequestMapping("/")
    public @ResponseBody String hello() {
        return "Hello";
    }

    @GetMapping("/poe")
    public @ResponseBody String poe() throws IOException {
        final URL url = new URL("http://api.pathofexile.com/public-stash-tabs");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        final StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    @GetMapping("/db")
    public @ResponseBody String db() {
        final DynamoDbClient client = DynamoDbClient.create();
        listAllTables(client);
        return "OK";
    }

    public static void listAllTables(final DynamoDbClient ddb) {

        boolean moreTables = true;
        String lastName = null;

        while (moreTables) {
            try {
                ListTablesResponse response = null;
                if (lastName == null) {
                    final ListTablesRequest request = ListTablesRequest.builder().build();
                    response = ddb.listTables(request);
                } else {
                    final ListTablesRequest request = ListTablesRequest.builder().exclusiveStartTableName(lastName)
                            .build();
                    response = ddb.listTables(request);
                }

                final List<String> tableNames = response.tableNames();

                if (tableNames.size() > 0) {
                    for (final String curName : tableNames) {
                        System.out.format("* %s\n", curName);
                    }
                } else {
                    System.out.println("No tables found!");
                    System.exit(0);
                }

                lastName = response.lastEvaluatedTableName();
                if (lastName == null) {
                    moreTables = false;
                }
            } catch (final DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

    }
}
