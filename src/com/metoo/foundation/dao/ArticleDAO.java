package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.Article;
@Repository("articleDAO")
public class ArticleDAO extends GenericDAO<Article> {

}