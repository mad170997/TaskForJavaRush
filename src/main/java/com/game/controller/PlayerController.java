package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/players") //1,6
    public ResponseEntity<List<Player>> getAllPlayers(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "title", required = false) String title, @RequestParam(value = "race", required = false) Race race, @RequestParam(value = "profession", required = false) Profession profession, @RequestParam(value = "after", required = false) Long after, @RequestParam(value = "before", required = false) Long before, @RequestParam(value = "banned", required = false) Boolean banned, @RequestParam(value = "minExperience", required = false) Integer minExperience, @RequestParam(value = "maxExperience", required = false) Integer maxExperience, @RequestParam(value = "minLevel", required = false) Integer minLevel, @RequestParam(value = "maxLevel", required = false) Integer maxLevel, @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order, @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber, @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        try {
            Pageable sorted = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
            Specification<Player> specification = Specification.where(playerService.selectByName(name).and(playerService.selectByTitle(title)).and(playerService.selectByRace(race)).and(playerService.selectByProfession(profession)).and(playerService.selectByBirthday(after, before)).and(playerService.selectByBanned(banned)).and(playerService.selectByExperience(minExperience, maxExperience)).and(playerService.selectByLevel(minLevel, maxLevel)));

            Page<Player> pages = playerService.getPlayers(specification, sorted);
            return new ResponseEntity<>(pages.getContent(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping("/players/count") //7
    public ResponseEntity<Integer> playersCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "title", required = false) String title, @RequestParam(value = "race", required = false) Race race, @RequestParam(value = "profession", required = false) Profession profession, @RequestParam(value = "after", required = false) Long after, @RequestParam(value = "before", required = false) Long before, @RequestParam(value = "banned", required = false) Boolean banned, @RequestParam(value = "minExperience", required = false) Integer minExperience, @RequestParam(value = "maxExperience", required = false) Integer maxExperience, @RequestParam(value = "minLevel", required = false) Integer minLevel, @RequestParam(value = "maxLevel", required = false) Integer maxLevel, @RequestParam(value = "order", required = false) PlayerOrder order, @RequestParam(value = "pageNumber", required = false) Integer pageNumber, @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        Specification<Player> specification = Specification.where(playerService.selectByName(name).and(playerService.selectByTitle(title)).and(playerService.selectByRace(race)).and(playerService.selectByProfession(profession)).and(playerService.selectByBirthday(after, before)).and(playerService.selectByBanned(banned)).and(playerService.selectByExperience(minExperience, maxExperience)).and(playerService.selectByLevel(minLevel, maxLevel)));

        return new ResponseEntity<>(playerService.countPlayers(specification), HttpStatus.OK);
    }

    @PostMapping("/players") //2
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!isRequestCorrectForCreate(player)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        try {
            Integer level = playerService.calculateLevel(player.getExperience());
            player.setLevel(level);
            Integer expUntilNextLvl = playerService.calculateExpUntilNextLevel(player.getExperience(), level);
            player.setUntilNextLevel(expUntilNextLvl);

            Player newPlayer = playerRepository.save(player);
            return new ResponseEntity<>(newPlayer, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/players/{id}") //5
    public ResponseEntity<Player> getPlayerByID(@PathVariable("id") long id) {
        Optional<Player> player = playerRepository.findById(id);
        if (isIDIncorrect(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!player.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player.get(), HttpStatus.OK);
    }

    @PostMapping("/players/{id}") //2 errors (10,11)
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long id, @RequestBody Player requestPlayer) {
        Optional<Player> oldPlayer = playerRepository.findById(id);


        if (isIDIncorrect(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!oldPlayer.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (isRequestEmpty(requestPlayer)) {
            return new ResponseEntity<>(oldPlayer.get(), HttpStatus.OK);
        }
//        if (isRequestIncorrect(requestPlayer)) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }


        Player updatedPlayer;
        try {
            updatedPlayer = oldPlayer.get();

            if (requestPlayer.getName() != null) {
                if (isNameIncorrect(requestPlayer)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                updatedPlayer.setName(requestPlayer.getName());
            }
            if (requestPlayer.getTitle() != null) {
                if (isTitleIncorrect(requestPlayer)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                updatedPlayer.setTitle(requestPlayer.getTitle());
            }
            if (requestPlayer.getRace() != null) {
                updatedPlayer.setRace(requestPlayer.getRace());
            }
            if (requestPlayer.getProfession() != null) {
                updatedPlayer.setProfession(requestPlayer.getProfession());
            }
            if (requestPlayer.getBirthday() != null) {
                if (isDateIncorrect(requestPlayer)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                updatedPlayer.setBirthday(requestPlayer.getBirthday());
            }
            if (requestPlayer.getBanned() != null) {
                updatedPlayer.setBanned(requestPlayer.getBanned());
            }
            if (requestPlayer.getExperience() != null) {
                if (isExpIncorrect(requestPlayer)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                updatedPlayer.setExperience(requestPlayer.getExperience());
                Integer level = playerService.calculateLevel(requestPlayer.getExperience());
                updatedPlayer.setLevel(level);
                Integer expUntilNextLvl = playerService.calculateExpUntilNextLevel(requestPlayer.getExperience(), level);
                updatedPlayer.setUntilNextLevel(expUntilNextLvl);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(playerRepository.save(updatedPlayer), HttpStatus.OK);
    }

    @DeleteMapping("/players/{id}") //4
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") long id) {
        Optional<Player> player = playerRepository.findById(id);
        if (isIDIncorrect(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!player.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        playerRepository.deleteById(id);
        return new ResponseEntity<>(player.get(), HttpStatus.OK);
    }


    public boolean isIDIncorrect(Long id) {
        return (id < 1);
    }

    public boolean isRequestEmpty(Player player) {
//        if (player.getBanned()==null) {
//            player.setBanned(false);
//        }
        return (player.getName() == null && player.getTitle() == null && player.getRace() == null
                && player.getProfession() == null && player.getBirthday() == null
                && player.getExperience() == null && player.getBanned() == null);
    }

    public boolean isRequestCorrectForCreate(Player player) {
        String name = player.getName();
        String title = player.getTitle();
        Race race = player.getRace();
        Profession profession = player.getProfession();
        Date birthday = player.getBirthday();
        Boolean banned = player.getBanned();
        Integer experience = player.getExperience();

        if (name == null || title == null || race == null || profession == null || birthday == null || experience == null) {
            return false;
        }
        if (isRequestIncorrect(player)) {
            return false;
        }
        if (banned == null) {
            player.setBanned(false);
        }
        return true;
    }

//    public boolean isRequestIncorrect(Player player) {
//        Date dateOfBirthday = player.getBirthday();
//        Integer exp = player.getExperience();
//        String title = player.getTitle();
//        String name = player.getName();
//
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTime(dateOfBirthday);
//        long birthday = calendar.getTimeInMillis();
//
//        Calendar minCalendar = new GregorianCalendar();
//        minCalendar.set(2000, 0, 1);
//        long min = minCalendar.getTimeInMillis();
//
//        Calendar maxCalendar = new GregorianCalendar();
//        maxCalendar.set(3001, 0, 1);
//        long max = maxCalendar.getTimeInMillis();
//
//        if (birthday < 0 || (birthday < min || birthday >= max)) {
//            return true;
//        }
//
//        if (exp < 0 || exp > 10000000) {
//            return true;
//        }
//
//        if (name.length() > 12) {
//            return true;
//        }
//
//        if (title.length() > 30) {
//            return true;
//        }
//
//        return false;
//    }

    public boolean isRequestIncorrect(Player player) {
        return (isDateIncorrect(player) || isExpIncorrect(player) || isNameIncorrect(player) || isTitleIncorrect(player));
    }

    public boolean isDateIncorrect(Player player) {
        Date dateOfBirthday = player.getBirthday();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateOfBirthday);
        long birthday = calendar.getTimeInMillis();

        Calendar minCalendar = new GregorianCalendar();
        minCalendar.set(2000, 0, 1);
        long min = minCalendar.getTimeInMillis();

        Calendar maxCalendar = new GregorianCalendar();
        maxCalendar.set(3001, 0, 1);
        long max = maxCalendar.getTimeInMillis();

        return (birthday < 0 || (birthday < min || birthday >= max));
    }

    public boolean isExpIncorrect(Player player) {
        Integer exp = player.getExperience();
        return (exp < 0 || exp > 10000000);
    }

    public boolean isNameIncorrect(Player player) {
        String name = player.getName();
        return (name.length() > 12);
    }

    public boolean isTitleIncorrect(Player player) {
        String title = player.getTitle();
        return (title.length() > 30);
    }

}
