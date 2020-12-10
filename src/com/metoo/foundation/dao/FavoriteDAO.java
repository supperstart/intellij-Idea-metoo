package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.Favorite;
@Repository("favoriteDAO")
public class FavoriteDAO extends GenericDAO<Favorite> {

}