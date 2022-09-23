package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlayerService {

    Specification<Player> selectByName(String name);
    Specification<Player> selectByTitle(String title);
    Specification<Player> selectByRace(Race race);
    Specification<Player> selectByProfession(Profession profession);
    Specification<Player> selectByBirthday(Long after, Long before);
    Specification<Player> selectByBanned(Boolean banned);
    Specification<Player> selectByExperience(Integer minExperience, Integer maxExperience);
    Specification<Player> selectByLevel(Integer minLevel, Integer maxLevel);
    Page<Player> getPlayers(Specification<Player> specification, Pageable sortedBy);
    Integer countPlayers(Specification<Player> specification);
    Integer calculateLevel(Integer experience);
    Integer calculateExpUntilNextLevel(Integer experience, Integer level);
}
