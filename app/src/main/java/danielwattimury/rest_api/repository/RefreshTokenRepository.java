package danielwattimury.rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import danielwattimury.rest_api.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

}
