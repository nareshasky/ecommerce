package com.ecom.controller;


import com.ecom.config.CustomerUserDetails;
import com.ecom.dto.CustomerDto;
import com.ecom.dto.CustomerRegistrationResponseDto;
import com.ecom.dto.JwtTokenResponseDto;
import com.ecom.dto.LoginTokenDto;
import com.ecom.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDto> addNewUser(
            @Validated @RequestBody CustomerDto customerDetailsDto) {

        CustomerRegistrationResponseDto response =
                service.saveCustomer(customerDetailsDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtTokenResponseDto> getToken(
            @RequestBody LoginTokenDto authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        CustomerUserDetails userDetails =
                (CustomerUserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
//                .replace("ROLE_", "");

        int userId = userDetails.getId();

        String token = service.generateToken(
                authRequest.getUsername(),
                role,
                userId
        );

        JwtTokenResponseDto response = new JwtTokenResponseDto(
                token,
                "Bearer",
                1800L, // 30 minutes (should match JWT exp)
                userId,
                authRequest.getUsername(),
                role
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/validate")
    public String validateToken() {//@RequestParam("token") String token
        //service.validateToken(token);
        return "Token is valid";
    }
}
