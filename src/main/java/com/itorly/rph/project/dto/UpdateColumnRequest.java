package com.itorly.rph.project.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateColumnRequest {

    @Size(min = 2, max = 100)
    private String name;

    private Integer position;
}
