package com.example.userauthenticationservice.controllers;

import com.example.userauthenticationservice.dtos.*;
import com.example.userauthenticationservice.exceptions.UserAlreadyExistsException;
import com.example.userauthenticationservice.exceptions.UserNotFoundException;
import com.example.userauthenticationservice.exceptions.WrongPasswordException;
import com.example.userauthenticationservice.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody SignupRequestDto request){
        SignUpResponseDto response = new SignUpResponseDto();

        try {
            if (authService.signUp(request.getEmail(), request.getPassword())) {
                response.setRequeststatus(RequestStatus.SUCCESS);
            } else {
                response.setRequeststatus(RequestStatus.FAILURE);
            }
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (UserAlreadyExistsException e) {
            response.setRequeststatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        try{
            String token = authService.login(request.getEmail(), request.getPassword());
            LoginResponseDto response = new LoginResponseDto();
            response.setRequestStatus(RequestStatus.SUCCESS);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Authorization_Token",token);
            ResponseEntity<LoginResponseDto> responseEntity = new ResponseEntity<>(response, headers,HttpStatus.OK);
            return responseEntity;
        }catch(WrongPasswordException | UserNotFoundException wpe){
            LoginResponseDto response = new LoginResponseDto();
            response.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }


    }

    @GetMapping("/verify")
    public boolean verifyJwtToken(@RequestParam("token") String token) {
        return authService.verifyJwtToken(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(@RequestBody LogoutRequestDto request) {
        LogoutResponseDto response = new LogoutResponseDto();
        if(authService.logout(request.getEmail())) {
            response.setRequestStatus(RequestStatus.SUCCESS);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } else {
            response.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

}
