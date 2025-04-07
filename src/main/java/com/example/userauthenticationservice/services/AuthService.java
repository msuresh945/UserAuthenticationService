package com.example.userauthenticationservice.services;

import com.example.userauthenticationservice.exceptions.UserAlreadyExistsException;
import com.example.userauthenticationservice.exceptions.UserNotFoundException;
import com.example.userauthenticationservice.exceptions.WrongPasswordException;
import com.example.userauthenticationservice.models.Session;
import com.example.userauthenticationservice.models.SessionStatus;
import com.example.userauthenticationservice.models.User;
import com.example.userauthenticationservice.repos.SessionRepository;
import com.example.userauthenticationservice.repos.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.util.*;

@Service
public class AuthService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SecretKey secretkey;
    private SessionRepository sessionRepository;



    public AuthService(UserRepository userRepository,BCryptPasswordEncoder bCryptPasswordEncoder,SecretKey secretkey,SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.secretkey=secretkey;
        this.sessionRepository=sessionRepository;
    }

    public boolean signUp(String email, String password) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("user with email:"+email+" already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);

        return true;
    }

    @Transactional
    public String login(String email, String password) throws UserNotFoundException, WrongPasswordException {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            if (bCryptPasswordEncoder.matches(password, user.get().getPassword())) {
                sessionRepository.deleteByUserEmail(email);
                String token= createJwtToken(user.get().getId(), new ArrayList<>(), email);
                Session session = new Session();
                session.setToken(token);
                session.setUser(user.get());
                session.setExpiringAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                session.setSessionStatus(SessionStatus.ACTIVE);
                sessionRepository.save(session);
                return token;
            }else{
                throw new WrongPasswordException("wrong password");
            }
        }else{
            throw new UserNotFoundException("user with email:"+email+" not found");
        }
    }

    private String createJwtToken(Long userId, List<String> roles, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("User_Id",userId);
        claims.put("role",roles);
        claims.put("email",email);
        LocalDate localDate = LocalDate.now().plusDays(30);
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .issuedAt(new Date())
                .signWith(secretkey)
                .compact();
    }

    public boolean verifyJwtToken(String token) {
        try{
            JwtParser jwtparser = Jwts.parser().verifyWith(secretkey).build();
            Claims claims = jwtparser.parseSignedClaims(token).getPayload();
            String new_token = Jwts.builder()
                    .claims(claims)
                    .signWith(secretkey)
                    .compact();
            if(!new_token.equals(token)){
                throw new RuntimeException("Invalid Token");
            }

            Date expiration = claims.getExpiration();
            if(expiration.before(new Date())){
                throw new RuntimeException("Token Expired");
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public boolean logout(String email) {
        List<Session> sessions = sessionRepository.findByUserEmail(email);
        if (!sessions.isEmpty()) {
            Session session = sessions.get(0);
            session.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(session);
            return true;
        }
        return false;
    }






}
