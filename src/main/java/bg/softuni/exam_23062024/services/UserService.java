package bg.softuni.exam_23062024.services;

import bg.softuni.exam_23062024.models.dtos.LoginDTO;
import bg.softuni.exam_23062024.models.dtos.RegisterDTO;
import bg.softuni.exam_23062024.models.entities.User;
import bg.softuni.exam_23062024.repositories.UserRepository;
import bg.softuni.exam_23062024.session.CurrentUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final CurrentUser currentUser;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
    }

//    public boolean userExits(String username) {
//        Optional<User> byUsername = this.userRepository.findByUsername(username);
//
//        return byUsername.isPresent();
//    }

    public void register(RegisterDTO registerDTO) {
        User optUser = this.userRepository.findByUsernameOrEmail(registerDTO.getUsername(), registerDTO.getEmail()).orElse(null);

        if (optUser != null) {
            throw new RuntimeException("User with email " + registerDTO.getEmail() + " already exists!");
        }

        User newUser = this.modelMapper.map(registerDTO, User.class);
        newUser.setPassword(this.passwordEncoder.encode(registerDTO.getPassword()));

        this.userRepository.save(newUser);
    }

    public boolean login(LoginDTO loginDTO) {
        Optional<User> byUsername = this.userRepository.findByUsername(loginDTO.getUsername());

        if (byUsername.isEmpty()) {
            return false;
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), byUsername.get().getPassword())) {
            return false;
        }
        this.currentUser.login(byUsername.get().getId(), loginDTO.getUsername());

        return true;
    }

    public boolean isLoggedIn() {
        return this.currentUser.isLoggedIn();
    }

    public User getUser() {
        return this.modelMapper.map(this.currentUser, User.class);
    }
}
