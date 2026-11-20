package com.licensis.notaire.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.licensis.notaire.adapter.out.persistence.user.CachingRoleCatalog;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.CreateRoleCommand;
import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.application.usecase.user.RoleManagementService;
import com.licensis.notaire.application.usecase.user.RoleQueryService;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.User;

/**
 * The role list is cached in the outbound catalog adapter and a write drops it (#597, CU74).
 */
class RoleCatalogCacheTest {

    @Test
    void aSecondListReadSkipsThePortUntilAWrite() {
        try (var context = new AnnotationConfigApplicationContext(CacheHarness.class)) {
            var query = context.getBean(QueryRolesUseCase.class);
            var manage = context.getBean(ManageRolesUseCase.class);
            var catalog = context.getBean(CountingCatalog.class);

            assertEquals(1, query.findAll().size());
            assertEquals(1, query.findAll().size());
            assertEquals(1, catalog.reads, "the second list read is served from the cache");

            manage.create(new CreateRoleCommand("Clerk", "desk", true, List.of()));
            assertEquals(2, query.findAll().size());
            assertEquals(2, catalog.reads, "a write drops the list so the next read sees the new row");

            manage.delete(1);
            assertEquals(1, query.findAll().size());
            assertEquals(3, catalog.reads, "a delete drops the list too");
        }
    }

    @Configuration
    @EnableCaching
    static class CacheHarness {
        @Bean
        CacheManager cacheManager() {
            var manager = new CaffeineCacheManager(CachingRoleCatalog.CACHE);
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)));
            return manager;
        }

        @Bean
        CountingCatalog counting() {
            return new CountingCatalog();
        }

        @Bean
        @Primary
        RoleCatalogPort catalog(CountingCatalog counting) {
            return new CachingRoleCatalog(counting);
        }

        @Bean
        QueryRolesUseCase query(RoleCatalogPort catalog) {
            return new RoleQueryService(catalog);
        }

        @Bean
        ManageRolesUseCase manage(RoleCatalogPort catalog) {
            return new RoleManagementService(catalog, new UnusedUsers());
        }
    }

    static final class UnusedUsers implements UserCatalogPort {
        @Override
        public List<User> findAll() {
            return List.of();
        }

        @Override
        public Optional<User> findById(int id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findFirstByPersonId(int personId) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(int id) {
            return false;
        }

        @Override
        public User save(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteById(int id) {
            throw new UnsupportedOperationException();
        }
    }

    static final class CountingCatalog implements RoleCatalogPort {
        int reads;
        private final List<Role> rows = new ArrayList<>();
        private int nextId = 1;

        CountingCatalog() {
            rows.add(Role.restore(nextId++, "Admin", "all", true, List.of(), 0));
        }

        @Override
        public List<Role> findAll() {
            reads++;
            return List.copyOf(rows);
        }

        @Override
        public Optional<Role> findById(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst();
        }

        @Override
        public Optional<Role> findByName(String name) {
            return rows.stream().filter(row -> row.name().equals(name)).findFirst();
        }

        @Override
        public boolean existsById(int id) {
            return findById(id).isPresent();
        }

        @Override
        public Role save(Role role) {
            if (role.id() == null) {
                var saved = Role.restore(
                        nextId++, role.name(), role.description(), role.active(), role.modules(), 0);
                rows.add(saved);
                return saved;
            }
            rows.replaceAll(row -> row.id().equals(role.id()) ? role : row);
            return role;
        }

        @Override
        public boolean deleteById(int id) {
            return rows.removeIf(row -> row.id() == id);
        }
    }
}
