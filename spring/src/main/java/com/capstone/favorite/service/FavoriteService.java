package com.capstone.favorite.service;

import com.capstone.favorite.dto.FavoriteResponse;
import com.capstone.favorite.entity.Favorite;
import com.capstone.favorite.repository.FavoriteRepository;
import com.capstone.garment.entity.Garment;
import com.capstone.garment.repository.GarmentRepository;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(String email) {
        User user = findUser(email);

        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(fav -> {
                    Garment g = garmentRepository.findById(fav.getGarmentId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "의류를 찾을 수 없습니다: " + fav.getGarmentId()));
                    return new FavoriteResponse(
                            String.valueOf(g.getGarmentId()), g.getStatus(), g.getSourceType(),
                            g.getCategory(), g.getFilename(), g.getFileUrl(),
                            g.getBrandKey(), fav.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void add(String email, String garmentId) {
        User user = findUser(email);

        if (!garmentRepository.existsById(garmentId)) {
            throw new IllegalArgumentException("존재하지 않는 의류입니다: " + garmentId);
        }
        if (favoriteRepository.existsByUserIdAndGarmentId(user.getId(), garmentId)) {
            throw new IllegalStateException("이미 즐겨찾기에 추가된 의류입니다.");
        }

        Favorite fav = new Favorite();
        fav.setUserId(user.getId());
        fav.setGarmentId(garmentId);
        favoriteRepository.save(fav);
    }

    @Transactional
    public void remove(String email, String garmentId) {
        User user = findUser(email);

        Favorite fav = favoriteRepository.findByUserIdAndGarmentId(user.getId(), garmentId)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기에 없는 의류입니다."));

        favoriteRepository.delete(fav);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}