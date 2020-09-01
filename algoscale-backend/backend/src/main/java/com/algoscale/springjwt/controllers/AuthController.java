package com.algoscale.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algoscale.springjwt.models.ERole;
import com.algoscale.springjwt.models.Role;
import com.algoscale.springjwt.models.User;
import com.algoscale.springjwt.payload.request.LoginRequest;
import com.algoscale.springjwt.payload.request.SignupRequest;
import com.algoscale.springjwt.payload.response.JwtResponse;
import com.algoscale.springjwt.payload.response.MessageResponse;
import com.algoscale.springjwt.repository.RoleRepository;
import com.algoscale.springjwt.repository.UserRepository;
import com.algoscale.springjwt.security.jwt.JwtUtils;
import com.algoscale.springjwt.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody final LoginRequest loginRequest) {

		final Authentication authentication = this.authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		final String jwt = this.jwtUtils.generateJwtToken(authentication);

		final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		final List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(
				new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody final SignupRequest signUpRequest) {
		if (this.userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (this.userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		final User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				this.encoder.encode(signUpRequest.getPassword()));

		final Set<String> strRoles = signUpRequest.getRole();
		final Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			final Role userRole = this.roleRepository.save(new Role(ERole.ROLE_USER));
			roles.add(userRole);
		}
		user.setRoles(roles);
		this.userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@GetMapping("/getUserList")
	public ResponseEntity<?> getUserList() {
		final List<User> usersList = this.userRepository.findAll();
		return ResponseEntity.ok(usersList);
	}

	@DeleteMapping("/deleteUserById")
	public ResponseEntity<?> deleteUserById(@RequestParam final Long id) {
		this.userRepository.deleteById(id);
		return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
	}

}
