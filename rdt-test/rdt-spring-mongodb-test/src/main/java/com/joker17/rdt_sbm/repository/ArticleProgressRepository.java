package com.joker17.rdt_sbm.repository;

import com.joker17.rdt_sbm.domain.ArticleProgress;
import com.joker17.rdt_sbm.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleProgressRepository extends BaseRepository<ArticleProgress> {
    ArticleProgress findTopBy();
}
