package com.licensis.notaire.unit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.CreateRoleCommand;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase.CreateUserCommand;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.application.usecase.user.RoleManagementService;
import com.licensis.notaire.application.usecase.user.RoleQueryService;
import com.licensis.notaire.application.usecase.user.UserManagementService;
import com.licensis.notaire.application.usecase.user.UserQueryService;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.User;

@DisplayName("Users and roles use cases (issue #1731)")
class UsersRolesUseCasesTest {

    private InMemoryUserCatalog users;
    private InMemoryRoleCatalog roles;
    private UserManagementService userManage;
    private UserQueryService userQuery;
    private RoleManagementService roleManage;
    private RoleQueryService roleQuery;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserCatalog();
        roles = new InMemoryRoleCatalog();
        PasswordHasherPort hasher = new PasswordHasherPort() {
            @Override
            public String encode(String rawPassword) {
                return "enc:" + rawPassword;
            }

            @Override
            public boolean matchesBcrypt(String rawPassword, String storedHash) {
                return storedHash.equals("enc:" + rawPassword);
            }

            @Override
            public boolean isBcryptHash(String hash) {
                return hash != null && hash.startsWith("enc:");
            }

            @Override
            public String legacyMd5Hash(String rawPassword) {
                return "md5:" + rawPassword;
            }
        };
        userManage = new UserManagementService(users, hasher);
        userQuery = new UserQueryService(users);
        roleManage = new RoleManagementService(roles, users);
        roleQuery = new RoleQueryService(roles);
    }

    @Test
    @DisplayName("create user encodes password and lists")
    void createUser() {
        User created = userManage.create(new CreateUserCommand("ana", "secret", "ROLE_USER", true));
        assertThat(created.passwordHash()).isEqualTo("enc:secret");
        assertThat(userQuery.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("create role rejects duplicate name")
    void duplicateRole() {
        roleManage.create(new CreateRoleCommand("ADMIN", "a", true, List.of("users")));
        assertThatThrownBy(() -> roleManage.create(new CreateRoleCommand("ADMIN", "b", true, List.of())))
                .isInstanceOf(RoleNameConflictException.class);
    }

    @Test
    @DisplayName("assign and unassign role on user")
    void assignRole() {
        User user = userManage.create(new CreateUserCommand("bob", "x", "ROLE_USER", true));
        Role role = roleManage.create(new CreateRoleCommand("OP", "op", true, List.of("folios")));
        roleManage.assignRoleToUser(role.id(), user.id());
        assertThat(userQuery.findById(user.id())).hasValueSatisfying(u -> {
            assertThat(u.roleId()).isEqualTo(role.id());
            assertThat(u.roleName()).isEqualTo("OP");
        });
        roleManage.unassignRoleFromUser(user.id());
        assertThat(userQuery.findById(user.id())).hasValueSatisfying(u -> assertThat(u.roleId()).isNull());
        assertThat(roleQuery.findById(role.id())).isPresent();
    }

    private static final class InMemoryUserCatalog implements UserCatalogPort {
        private final Map<Integer, User> byId = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public List<User> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public Optional<User> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findFirstByPersonId(int personId) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(int id) {
            return byId.containsKey(id);
        }

        @Override
        public User save(User user) {
            int id = user.id() == null ? seq.getAndIncrement() : user.id();
            User stored = user.withIdentity(id, user.version());
            byId.put(id, stored);
            return stored;
        }

        @Override
        public boolean deleteById(int id) {
            return byId.remove(id) != null;
        }
    }

    private static final class InMemoryRoleCatalog implements RoleCatalogPort {
        private final Map<Integer, Role> byId = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public List<Role> findAll() {
            return List.copyOf(byId.values());
        }

        @Override
        public Optional<Role> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Role> findByName(String name) {
            return byId.values().stream().filter(r -> r.name().equals(name)).findFirst();
        }

        @Override
        public boolean existsById(int id) {
            return byId.containsKey(id);
        }

        @Override
        public Role save(Role role) {
            int id = role.id() == null ? seq.getAndIncrement() : role.id();
            Role stored = role.withIdentity(id, role.version());
            byId.put(id, stored);
            return stored;
        }

        @Override
        public boolean deleteById(int id) {
            return byId.remove(id) != null;
        }
    }
}
