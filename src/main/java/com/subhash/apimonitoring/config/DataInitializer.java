package com.subhash.apimonitoring.config;



import com.subhash.apimonitoring.role.entity.Role;
import com.subhash.apimonitoring.role.enums.RoleType;
import com.subhash.apimonitoring.role.repository.RoleRepository;
import com.subhash.apimonitoring.user.entity.User;
import com.subhash.apimonitoring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initializing default roles...");

            // Create ADMIN role
            Role adminRole = Role.fromEnum(RoleType.ADMIN);
            adminRole.setDefaultRole(false);
            System.out.println("Deleted = " + adminRole.getDeleted());
            System.out.println("Version = " + adminRole.getVersion());
            roleRepository.save(adminRole);

            // Create MANAGER role
            Role managerRole = Role.fromEnum(RoleType.MANAGER);
            managerRole.setDefaultRole(false);
            roleRepository.save(managerRole);

            // Create VIEWER role (default)
            Role viewerRole = Role.fromEnum(RoleType.VIEWER);
            viewerRole.setDefaultRole(true);
            roleRepository.save(viewerRole);

            log.info("Roles initialized successfully");
        }
    }

    private void initializeAdminUser() {
        if (!userRepository.existsByEmail("admin@monitoring.com")) {
            log.info("Creating admin user...");

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

            User admin = User.builder()
                    .username("admin")
                    .email("admin@monitoring.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .role(adminRole)
                    .active(true)
                    .emailVerified(true)
                    .build();


            userRepository.save(admin);
            log.info("Admin user created successfully");
        }
    }
}
