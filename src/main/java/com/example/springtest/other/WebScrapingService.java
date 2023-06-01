package com.example.springtest.other;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.springtest.model.Website;
import com.example.springtest.repository.WebsiteRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class WebScrapingService {

    private final WebsiteRepository websiteRepository;

    public WebScrapingService(WebsiteRepository websiteRepository) {
        this.websiteRepository = websiteRepository;
    }
    
    Queue<LeakedData> leakedDataList = new ConcurrentLinkedQueue<>();
    int corePoolSize = Runtime.getRuntime().availableProcessors();
    long keepAliveTime = 60L;
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    public void addSeedUrl(String seedUrl) {
        Website existingWebsite = websiteRepository.findByUrl(seedUrl);
        if (existingWebsite == null) {
            Website website = new Website(seedUrl);
            websiteRepository.save(website);
        } else {
            System.out.println("Website already exists: " + seedUrl);
        }
    }

    public List<String> getAllSeedUrls() {
        List<Website> websites = websiteRepository.findAll();
        List<String> seedUrls = new ArrayList<>();
        for (Website website : websites) {
            seedUrls.add(website.getUrl());
        }
        return seedUrls;
    }

    public Queue<LeakedData> processAllSeedUrls(String email,String password,int maxNodes) {
        List<String> seedUrls = getAllSeedUrls();
        Queue<LeakedData> leakedDataList = null;
        for (String seedUrl : seedUrls) {
            leakedDataList = findLeakedData(email,password,seedUrl, maxNodes);
        }
        return leakedDataList;
    }
    

    public Queue<LeakedData> findLeakedData(String email,String pass,String seedUrl, int maxNodes) {
        System.out.println(email + " "+pass);
        int maximumPoolSize = corePoolSize * (maxNodes/200);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);

        // Define the regex patterns for email and password detection
        Pattern emailPattern = Pattern.compile(email);
        Pattern passwordPattern = Pattern.compile(pass);

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
                            Document doc = Jsoup.connect(currentUrl).userAgent("Mozilla/5.0").timeout(10 * 1000).get();
                            Thread.sleep(3000);  
                            System.out.println(currentUrl);
                            Elements links = doc.select("a[href]");

                            Elements elements = doc.select("p");
                            for (Element element : elements) {
                                if (element != null) {
                                    String text = element.text();
                                    Matcher emailMatcher = emailPattern.matcher(text);
                                    Matcher passwordMatcher = passwordPattern.matcher(text);

                                    if (emailMatcher.find() && passwordMatcher.find()) {
                                        String foundEmail = emailMatcher.group();
                                        String foundPassword = passwordMatcher.group();
                                        leakedDataList.add(new LeakedData(currentUrl, foundEmail, foundPassword));
                                        System.out.println("URL: " + currentUrl+ " , Email: "+ foundEmail+" , Password: " +foundPassword );
                                    }
                                }
                               
                            }

                            String baseUrl=null;
                            try {
                                URL url = new URL(seedUrl);
                                baseUrl = url.getProtocol() + "://" + url.getHost();
                            }catch (MalformedURLException e) {
                                System.out.println("Invalid URL: " + seedUrl);
                            }

                            for (Element link : links) {
                                String nextUrl = link.absUrl("href");
                                
                                if (nextUrl.startsWith(baseUrl) && visited.putIfAbsent(nextUrl, true) == null) {
                                    if (visited.size() < maxNodes) {
                                        queue.offer(nextUrl);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error fetching URL: " + currentUrl);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
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
