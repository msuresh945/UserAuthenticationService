package com.example.userauthenticationservice.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User extends BaseModel{
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private List<Session> sessions;

}
