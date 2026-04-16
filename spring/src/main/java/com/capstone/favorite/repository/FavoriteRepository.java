package com.capstone.favorite.repository;

import com.capstone.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndGarmentId(Long userId, String garmentId);

    boolean existsByUserIdAndGarmentId(Long userId, String garmentId);
}