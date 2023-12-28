//package ru.genesizant.Professional.Timetable.controllers;
//
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class ImageController {
//
//    @GetMapping(value = "/static/img/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
//    @ResponseBody
//    public Resource getImage(@PathVariable String imageName) {
//        System.out.println();
//        return new ClassPathResource("static/img/" + imageName);
//    }
//}
//
////    @GetMapping(value = "/static/img/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)