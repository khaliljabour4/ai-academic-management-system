package com.codewithkahlil.backend.dto.requests;

import com.codewithkahlil.backend.model.TimeSlot;
import lombok.Data;

import java.time.DayOfWeek;

@Data
public class moduleRequest {
    String name ;
    String description ;
    int profId ;
    int filiereId ;
    DayOfWeek dayOfWeek;
    TimeSlot timeSlot ;
}
