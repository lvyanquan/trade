/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.model.currency;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.util.JsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CurrencySupplierRegister {

    private static final String CSV_FILE_PATH = "/Users/yh/Desktop/currency_supply.csv";

    protected static final HashMap<String, Double> CURRENCY_SUPPLIER_HASH_MAP = new HashMap<>(500);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        if (Files.exists(Paths.get(CSV_FILE_PATH))) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    String name = parts[0];
                    double supply = Double.parseDouble(parts[1]);
                    CURRENCY_SUPPLIER_HASH_MAP.put(name, supply);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            updateMemory();
            updateCSV();
        }
        Runnable task = () -> {
            updateMemory();
            updateCSV();
        };
        // schedule the task to run once a day
        scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.DAYS);
    }

    public static Optional<Double> getSupplie(String symbol) {
        return Optional.ofNullable(CURRENCY_SUPPLIER_HASH_MAP.get(symbol));
    }

    private static void updateMemory() {
        OkHttpClient client = new OkHttpClient();

        int currPage = 1;
        int maxPage = Integer.MAX_VALUE;

        while (currPage <= maxPage) {
            Request request = new Request.Builder()
                    .url("https://dncapi.bostonteapartyevent.com/api/coin/web-coinrank?page=" + currPage + "&type=-1&pagesize=1000&webp=1")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Map<String, Object> map = JsonUtil.parseForMap(response.body().string());
                maxPage = (int) map.get("maxpage");
                if (map.get("data") instanceof List) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");

                    for (Map<String, Object> datum : data) {
                        String name = (String) datum.get("name");
                        double supply = (double) datum.get("supply");
                        if (supply <= 0) {
                            continue;
                        }

                        if(name.equalsIgnoreCase("BEAM")){
                            CURRENCY_SUPPLIER_HASH_MAP.put("BEAMX", supply);
                        }else{
                            CURRENCY_SUPPLIER_HASH_MAP.put(name, supply);
                        }
                    }
                }else{
                    System.out.println("error data: " + map.get("data") );
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            currPage++;
        }
    }

    private static void updateCSV() {
        try (PrintWriter writer = new PrintWriter(new File(CSV_FILE_PATH))) {
            for (Map.Entry<String, Double> entry : CURRENCY_SUPPLIER_HASH_MAP.entrySet()) {
                String name = entry.getKey();
                double supply = entry.getValue();
                String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                writer.println(name + "," + supply + "," + date);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
