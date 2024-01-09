package com.example.hotdesk.desk;

import com.example.hotdesk.common.configuration.CustomPageImpl;
import com.example.hotdesk.desk.dto.DeskCreateDto;
import com.example.hotdesk.desk.dto.DeskResponseDto;
import com.example.hotdesk.desk.dto.DeskUpdateDto;
import com.example.hotdesk.desk.entity.Accessories;
import com.example.hotdesk.desk.entity.Desk;
import com.example.hotdesk.room.dto.RoomResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeskControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    DeskRepository deskRepository;

    @Test
    @Order(1)
    void createDesk() {

        DeskCreateDto deskCreateDto = new DeskCreateDto();
        List<Accessories> list = new ArrayList<>();

        list.addAll(Arrays.asList(Accessories.values()));


        deskCreateDto.setAccessories(list);
        deskCreateDto.setRoomId(1);

        ResponseEntity<DeskResponseDto> response = testRestTemplate
                .postForEntity("/desk", deskCreateDto, DeskResponseDto.class);


        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(201));

        DeskResponseDto body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getRoomId());

        Assertions.assertEquals(body.getRoomId(), deskCreateDto.getRoomId());
        Assertions.assertEquals(body.getAccessories(), deskCreateDto.getAccessories());


    }

    @Test
    @Order(2)
    void getAll() {

        ResponseEntity<CustomPageImpl<DeskResponseDto>> response = testRestTemplate.exchange("/desk", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<CustomPageImpl<DeskResponseDto>>() {
        });
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));

        CustomPageImpl<DeskResponseDto> body = response.getBody();
        Assertions.assertEquals(body.getNumberOfElements(), 1);
        DeskResponseDto roomResponseDto = body.getContent().get(0);
        Assertions.assertEquals(roomResponseDto.getRoomId(), 1);


    }

    @Test
    @Order(3)
    void getdesk() {
        ResponseEntity<DeskResponseDto> response = testRestTemplate
                .getForEntity("/desk/{id}", DeskResponseDto.class, 9);

        if (response.getStatusCode() == HttpStatus.OK) {
            Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
            DeskResponseDto body = response.getBody();
            Assertions.assertNotNull(body);

        } else {
            Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(404));
        }


    }

    @Test
    void update() {

        DeskUpdateDto deskUpdateDto = new DeskUpdateDto();
//        Desk desk = deskRepository.findAll().stream().findAny().get();
        List<Accessories> list = new ArrayList<>();
        list.addAll(Arrays.asList(Accessories.values()));

        deskUpdateDto.setRoomId(2);
        deskUpdateDto.setAccessories(list);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<DeskUpdateDto> requestEntity = new HttpEntity<>(deskUpdateDto, headers);


        String url = "/desk/{id}";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.queryParam("id", 1);

        ResponseEntity<RoomResponseDto> response = testRestTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.PUT,
                requestEntity,
                RoomResponseDto.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));

        Assertions.assertNotNull(response.getBody());
        System.out.println("update ishladi");

    }

    @Test
    void patch() {



    }

    @Test
    void delete() {
        Desk desk = deskRepository.findAll().stream().findAny().get();
        testRestTemplate.delete("/desk/%s".formatted(desk.getId()));

        Optional<Desk> desk1 = deskRepository.findById(desk.getId());
        Assertions.assertTrue(desk1.isEmpty());
    }
}