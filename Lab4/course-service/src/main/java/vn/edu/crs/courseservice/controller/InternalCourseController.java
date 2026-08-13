package vn.edu.crs.courseservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.courseservice.service.CourseService;

@RestController
@RequestMapping("/internal/courses")
@RequiredArgsConstructor
public class InternalCourseController {

    private final CourseService courseService;

    @PatchMapping("/{id}/reserve-seat")
    public void reserveSeat(@PathVariable Long id) {
        courseService.reserveSeat(id);
    }

    @PatchMapping("/{id}/release-seat")
    public void releaseSeat(@PathVariable Long id) {
        courseService.releaseSeat(id);
    }
}