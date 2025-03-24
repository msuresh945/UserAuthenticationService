package com.example.userauthenticationservice.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class BaseModel {
    private Long id;
    private Date createdDate;
    private Date updatedDate;
    private State state;
}
