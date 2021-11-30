package diplom.blogengine.security;

import diplom.blogengine.model.Role;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.security.UserDetailsExt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username;
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(username));
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return UserDetailsExt.builder()
                .id(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .isModerator(user.isModerator())
                .authorities(authorities)
                .build();
    }

}
