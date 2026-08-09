package com.codewithkahlil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DirectorFiliereOverviewResponse {
    List<StudentRow> students;
    List<ModuleRow> modules;

    @Data
    @Builder
    public static class StudentRow {
        String name;
        String prenom;
        String cne;
        Double moyenneGenerale;
    }

    @Data
    @Builder
    public static class ModuleRow {
        String name;
        String profName;
        Double moyenneClasse;
    }
}
