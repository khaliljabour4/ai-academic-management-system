package com.codewithkahlil.backend.dto.response;

import com.codewithkahlil.backend.model.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;

@Data
@Builder
@AllArgsConstructor
public class AvailableSlotResponse {
    DayOfWeek dayOfWeek;
    TimeSlot timeSlot;
}
