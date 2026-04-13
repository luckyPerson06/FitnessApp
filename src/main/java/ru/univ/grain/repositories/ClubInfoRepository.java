package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.ClubInfo;

@Repository
public interface ClubInfoRepository extends JpaRepository<ClubInfo, Long> {
}
