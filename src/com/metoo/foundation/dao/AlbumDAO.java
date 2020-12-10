package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.Album;
@Repository("albumDAO")
public class AlbumDAO extends GenericDAO<Album> {

}