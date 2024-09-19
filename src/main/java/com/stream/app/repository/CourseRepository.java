package com.stream.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stream.app.entity.Course;

public interface CourseRepository extends JpaRepository<Course, String>{

}
