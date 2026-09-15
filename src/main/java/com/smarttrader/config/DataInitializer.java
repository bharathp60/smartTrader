package com.smarttrader.config;

import com.smarttrader.entity.AppUser;
import com.smarttrader.entity.Role;
import com.smarttrader.repository.AppUserRepository;
import com.smarttrader.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public DataInitializer(AppUserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            String username = environment.getProperty("SMART_TRADER_BOOTSTRAP_USERNAME", "admin");
            String password = environment.getProperty("SMART_TRADER_BOOTSTRAP_PASSWORD", "admin");

            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ADMIN");
                r.setDescription("Administrator with full system access");
                return roleRepository.save(r);
            });

            Role traderRole = roleRepository.findByName("TRADER").orElseGet(() -> {
                Role r = new Role();
                r.setName("TRADER");
                r.setDescription("Trader capable of executing orders");
                return roleRepository.save(r);
            });

            AppUser admin = new AppUser();
            admin.setUsername(username);
            admin.setEmail("admin@smarttrader.local");
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole, traderRole));

            userRepository.save(admin);
        }
    }
}
