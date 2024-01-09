package com.example.hotdesk.room;

import com.example.hotdesk.common.configuration.CustomPageImpl;
import com.example.hotdesk.room.dto.RoomCreateDto;
import com.example.hotdesk.room.dto.RoomPatchDto;
import com.example.hotdesk.room.dto.RoomResponseDto;
import com.example.hotdesk.room.dto.RoomUpdateDto;
import com.example.hotdesk.room.entity.Room;
import com.example.hotdesk.room.entity.RoomType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    RoomRepository roomRepository;

    @Test
    @Order(1)
    void createRoom() {

        RoomCreateDto roomCreateDto = new RoomCreateDto();

        roomCreateDto.setRoomType(RoomType.GAME_ROOM);
        roomCreateDto.setNumber("A102");
        roomCreateDto.setFloorNumber(1);
        roomCreateDto.setOfficeId(1);

        ResponseEntity<RoomResponseDto> response = testRestTemplate
                .postForEntity("/room", roomCreateDto, RoomResponseDto.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(201));
        } else {
            Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        }


        RoomResponseDto body = response.getBody();
        Assertions.assertNotNull(body);

        Assertions.assertEquals(body.getRoomType(), roomCreateDto.getRoomType());
        Assertions.assertEquals(body.getFloorNumber(), roomCreateDto.getFloorNumber());
        Assertions.assertEquals(body.getOfficeId(), roomCreateDto.getOfficeId());
        Assertions.assertEquals(body.getNumber(), roomCreateDto.getNumber());

        Optional<Room> room = roomRepository.findById(body.getId());

        Assertions.assertTrue(room.isPresent());

        Room room1 = room.get();
        Assertions.assertEquals(room1.getNumber(), roomCreateDto.getNumber());
    }

    @Test
    @Order(2)
    void getAllRoom() {
        ResponseEntity<CustomPageImpl<RoomResponseDto>> response = testRestTemplate.exchange("/room?predicate=floorNumber==1", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<CustomPageImpl<RoomResponseDto>>() {
        });
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));

        CustomPageImpl<RoomResponseDto> body = response.getBody();
        Assertions.assertEquals(body.getNumberOfElements(), 1);
        RoomResponseDto roomResponseDto = body.getContent().get(0);
//        Assertions.assertEquals(roomResponseDto.getNumber(), "A102");
    }

    @Test
    @Order(3)
    void getRoom() {
        ResponseEntity<RoomResponseDto> response = testRestTemplate
                .getForEntity("/room/{id}", RoomResponseDto.class, 18);

        if (response.getStatusCode() == HttpStatus.OK) {
            Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
            RoomResponseDto body = response.getBody();
            Assertions.assertNotNull(body);

        } else {
            Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(404));
        }


    }

    @Test
    @Order(4)
    void updateRoom() {

        RoomUpdateDto roomUpdateDto = new RoomUpdateDto();

        roomUpdateDto.setRoomType(RoomType.WORKROOM);
        roomUpdateDto.setNumber("A202");
        roomUpdateDto.setFloorNumber(2);
        roomUpdateDto.setOfficeId(1);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<RoomUpdateDto> requestEntity = new HttpEntity<>(roomUpdateDto, headers);


        String url = "/room/{id}";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.queryParam("id", 1);

        ResponseEntity<RoomResponseDto> response = testRestTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.PUT,
                requestEntity,
                RoomResponseDto.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));

        RoomResponseDto body = response.getBody();
        Assertions.assertNotNull(body);

    }

    @Test
    void patchRoom() {


//
//        String url = "/room/patch/{id}";
//        testRestTemplate
//        Assertions.assertNotNull(response);

        RoomPatchDto patchDto = new RoomPatchDto();
        patchDto.setNumber("B303");

    }

    @Test
    void delete() {

        Room room = roomRepository.findAll().stream().findAny().get();
        testRestTemplate.delete("/room/%s".formatted(room.getId()));

        Optional<Room> room1 = roomRepository.findById(room.getId());
        Assertions.assertTrue(room1.isEmpty());
    }
}