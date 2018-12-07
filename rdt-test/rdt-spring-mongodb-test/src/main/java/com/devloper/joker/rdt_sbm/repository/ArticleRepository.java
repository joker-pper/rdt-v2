package com.devloper.joker.rdt_sbm.repository;

import com.devloper.joker.rdt_sbm.domain.Article;
import com.devloper.joker.rdt_sbm.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends BaseRepository<Article> {

    Article findTopBy();

    Article findTopByType(Integer type);

}
