package com.example.sca.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

class InMemoryUserRepository implements UserRepository {
    private final Map<Long, AppUser> users = new HashMap<Long, AppUser>();
    private long nextId = 1L;

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return users.values().stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    @Override
    public List<AppUser> findAll() {
        return new ArrayList<AppUser>(users.values());
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public <S extends AppUser> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        users.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    @Override public List<AppUser> findAll(Sort sort) { return findAll(); }
    @Override public Page<AppUser> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public List<AppUser> findAllById(Iterable<Long> longs) { throw new UnsupportedOperationException(); }
    @Override public long count() { return users.size(); }
    @Override public void delete(AppUser entity) { users.remove(entity.getId()); }
    @Override public void deleteAllById(Iterable<? extends Long> longs) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll(Iterable<? extends AppUser> entities) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll() { users.clear(); }
    @Override public <S extends AppUser> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
    @Override public void flush() {}
    @Override public <S extends AppUser> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends AppUser> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllInBatch(Iterable<AppUser> entities) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllInBatch() { users.clear(); }
    @Override public AppUser getOne(Long id) { return users.get(id); }
    @Override public AppUser getById(Long id) { return users.get(id); }
    @Override public AppUser getReferenceById(Long id) { return users.get(id); }
    @Override public boolean existsById(Long id) { return users.containsKey(id); }
    @Override public <S extends AppUser> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser> long count(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser> boolean exists(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends AppUser, R> R findBy(Example<S> example, java.util.function.Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }
}
