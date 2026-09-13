package com.linuxremediation.Engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResultDTO {
    private String command;
    private int exitCode;
    private String stdout;
    private String stderr;
    private boolean success;
}
