package card.uz.config;

import card.uz.entity.UserEntity;
import card.uz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserEntity profile = userRepository.findById(Long.parseLong(id)).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        CustomUserDetails customUser = new CustomUserDetails();
        customUser.setId(profile.getId());
        customUser.setRoles(profile.getRoles());
        return customUser;
    }
}
