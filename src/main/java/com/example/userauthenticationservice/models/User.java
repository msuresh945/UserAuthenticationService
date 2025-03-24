package com.example.userauthenticationservice.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User extends BaseModel{

    private String emailId;
    private String password;


}
