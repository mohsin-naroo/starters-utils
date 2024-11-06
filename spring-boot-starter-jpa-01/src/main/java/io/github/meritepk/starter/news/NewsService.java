package io.github.meritepk.starter.news;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsService {

    private final NewsRepository repository;

    public NewsService(NewsRepository repository) {
        this.repository = repository;
    }

    private List<News> findAll(int page, int size) {
        return repository.findByOrderByIdDesc(PageRequest.of(page, size));
    }

    @Transactional
    public List<News> example() {
        int pageSize = 5;
        List<News> newsList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            String title = "Title @ " + LocalDateTime.now() + "-" + i;
            String details = title + " > Details";
            newsList.add(create(title, details));
        }
        repository.saveAll(newsList);
        List<News> page = findAll(0, pageSize);
        return page;
    }

    private News create(String title, String details) {
        News news = new News();
        news.setTitle(title);
        news.setDetails(details);
        news.setCreatedAt(LocalDateTime.now());
        news.setCreatedBy("create-user");
        news.setModifiedAt(LocalDateTime.now());
        news.setModifiedBy("create-user");
        return news;
    }
}
