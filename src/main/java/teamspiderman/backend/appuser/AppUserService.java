package teamspiderman.backend.appuser;

import teamspiderman.backend.registration.Token.ConfirmationToken;
import teamspiderman.backend.registration.Token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
            "email %s or password is incorrect";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(AppUser appUser) {

        boolean userExists = appUserRepository
                .findByEmail(appUser.getEmail())
                .isPresent();

        if (userExists) {

            // TODO check of attributes are the same and
            // TODO if email not confirmed send confirmation email.
            boolean userEnabled = appUserRepository.findByEmail(appUser.getEmail()).get().getEnabled();
            if(userEnabled){
                throw new IllegalStateException("email already taken");
            }
            else{
                throw new IllegalStateException("confirmation email has been sent");
            }
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(
                confirmationToken);

//        TODO: SEND EMAIL

        return token;
    }

    public Long siginUser(String email, String password){

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(()->
                        new IllegalStateException(
                                String.format(USER_NOT_FOUND_MSG, email)));

        String encodedPassword = bCryptPasswordEncoder
                .encode(password);

        String userPassword = appUserRepository
                .findByEmail(email).get().getPassword();

        if(!encodedPassword.equals(userPassword)){
            new IllegalStateException(String.format(USER_NOT_FOUND_MSG, email));
        }

        return appUser.getUserID();
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
