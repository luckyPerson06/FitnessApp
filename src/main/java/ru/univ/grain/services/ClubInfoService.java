package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.AppCache;
import ru.univ.grain.cache.CacheKey;
import ru.univ.grain.cache.CacheRegion;
import ru.univ.grain.dto.ClubInfoDto;
import ru.univ.grain.entities.ClubInfo;
import ru.univ.grain.mapper.ClubInfoMapper;
import ru.univ.grain.repositories.ClubInfoRepository;

@Service
@RequiredArgsConstructor
public class ClubInfoService {

    private final ClubInfoRepository clubInfoRepository;
    private final ClubInfoMapper clubInfoMapper;
    private final AppCache appCache;

    @Transactional(readOnly = true)
    public ClubInfoDto getClubInfo() {
        final CacheKey key = CacheKey.forClubInfo();

        final ClubInfoDto cached = appCache.get(key);
        if (cached != null) {
            return cached;
        }

        final ClubInfo clubInfo = clubInfoRepository.findAll().stream().findFirst().orElse(null);
        if (clubInfo == null) {
            return ClubInfoDto.builder().build();
        }

        final ClubInfoDto result = clubInfoMapper.toDto(clubInfo);
        appCache.put(key, result);
        return result;
    }

    @Transactional
    public ClubInfoDto updateClubInfo(final ClubInfoDto dto) {
        final ClubInfo clubInfo = clubInfoRepository.findAll().stream().findFirst()
                .orElseGet(() -> ClubInfo.builder().build());

        clubInfoMapper.updateEntity(dto, clubInfo);
        final ClubInfo saved = clubInfoRepository.save(clubInfo);
        final ClubInfoDto result = clubInfoMapper.toDto(saved);

        appCache.clearRegion(CacheRegion.CLUB_INFO);
        return result;
    }
}
