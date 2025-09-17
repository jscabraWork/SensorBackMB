package com.arquitectura.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {

    private String sub;
    private String email;
    private String name;
    private String picture;
}