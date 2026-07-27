package com.codewithkahlil.backend.dto.response;

import com.codewithkahlil.backend.model.TimeSlot;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;

@Data
@Builder
public class ScheduleEntryResponse {
    DayOfWeek dayOfWeek;
    TimeSlot timeSlot;
    String moduleName;
    String teacherName;
}
