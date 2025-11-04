package com.terceiraAPI.Projeto3aAPI.Controller;

import com.terceiraAPI.Projeto3aAPI.Config.JwtUtil;
import com.terceiraAPI.Projeto3aAPI.Model.Usuario;
import com.terceiraAPI.Projeto3aAPI.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest().body("Email já cadastrado");
        }
        
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuário registrado com sucesso");
        response.put("userId", usuarioSalvo.getId().toString());
        
        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());
        
        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getSenha(), usuarioOpt.get().getSenha())) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
        
        String token = jwtUtil.generateToken(loginRequest.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", loginRequest.getEmail());
        response.put("message", "Login realizado com sucesso");
        
        return ResponseEntity.ok(response);
    }

    // VALIDAR TOKEN
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token != null && jwtUtil.validateToken(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("email", jwtUtil.getEmailFromToken(token));
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).body("Token inválido");
    }
    
    // Classe interna para o Login Request
    public static class LoginRequest {
        private String email;
        private String senha;
        
        // Getters e Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }
}