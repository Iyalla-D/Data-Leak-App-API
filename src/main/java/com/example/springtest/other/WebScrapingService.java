package com.example.springtest.other;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebScrapingService {
    Queue<LeakedData> leakedDataList = new ConcurrentLinkedQueue<>();
    int corePoolSize = Runtime.getRuntime().availableProcessors();
    long keepAliveTime = 60L;
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    
    public Queue<LeakedData> findLeakedData(String seedUrl, int maxNodes) {
        int maximumPoolSize = corePoolSize * (maxNodes/200);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);

        // Define the regex patterns for email and password detection
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Pattern passwordPattern = Pattern.compile("password:\\s*(['\"]?)(.+?)\\1");

        ConcurrentHashMap<String, Boolean> visited = new ConcurrentHashMap<>();
        ConcurrentLinkedDeque<String> queue = new ConcurrentLinkedDeque<>();
        
        queue.add(seedUrl);
        visited.put(seedUrl, true);

        try {
            while (!queue.isEmpty() && visited.size() < maxNodes) {
                List<String> urlsToProcess = new ArrayList<>();
                while (urlsToProcess.size() < 15 && !queue.isEmpty()) {
                    String currentUrl = queue.remove();
                    urlsToProcess.add(currentUrl);
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (String currentUrl : urlsToProcess) {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Document doc = Jsoup.connect(currentUrl).get();
                            Elements links = doc.select("a[href]");

                            Elements elements = doc.select("p");
                            for (Element element : elements) {
                                if (element != null) {
                                    String text = element.text();
                                    Matcher emailMatcher = emailPattern.matcher(text);
                                    Matcher passwordMatcher = passwordPattern.matcher(text);

                                    if (emailMatcher.find() && passwordMatcher.find()) {
                                        String email = emailMatcher.group();
                                        String password = passwordMatcher.group(2);
                                        leakedDataList.add(new LeakedData(currentUrl, email, password));
                                    }
                                }
                            }

                            for (Element link : links) {
                                String nextUrl = link.absUrl("href");
                                if (nextUrl.startsWith(seedUrl) && visited.putIfAbsent(nextUrl, true) == null) {
                                    if (visited.size() < maxNodes) {
                                        queue.offer(nextUrl);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error fetching URL: " + currentUrl);
                        }
                        return null;
                    }, executor);
                    futures.add(future);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        // Log the number of visited nodes
        System.out.println("Visited " + visited.size() + " nodes:");

        // Return the list of leaked data found
        return leakedDataList;
    }
}
