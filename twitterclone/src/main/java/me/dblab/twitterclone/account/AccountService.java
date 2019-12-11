package me.dblab.twitterclone.account;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService implements ReactiveUserDetailsService {

    private final AccountRepository accountRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Autowired
    public AccountService(AccountRepository accountRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    Mono<Account> getAccount(String id) {
        return accountRepository.findById(id);
    }

    public Mono<ResponseEntity> saveAccount(AccountDto accountDto) {
        return Mono.just(accountDto).map(user -> {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Collections.singletonList(Role.USER));
            return modelMapper.map(user, Account.class);
        }).flatMap(user -> accountRepository.findByEmail(user.getEmail())
                        .map(dupUser -> ResponseEntity.badRequest().build())
                        .switchIfEmpty(accountRepository.save(user)
                                .map(saveUser -> new ResponseEntity<>(saveUser, HttpStatus.CREATED))));

    }

    Mono<ResponseEntity> updateAccount(String id, AccountDto accountDto) {
        return Mono.just(accountDto)
                .flatMap(updatedUser ->
                        accountRepository.findById(id)
                                .map(user -> {
                                    user.update(modelMapper.map(updatedUser, Account.class));
                                    return user;
                                })
                                .flatMap(accountRepository::save)
                                .map(res -> new ResponseEntity<>("{}", HttpStatus.OK))

                );    
    }

    public Mono<ResponseEntity> login(Account account) {
        return Mono.just(account).flatMap(account1 -> accountRepository.findByEmail(account1.getEmail())
                .map(account2 -> {
                    if (passwordEncoder.matches(account1.getPassword(), account2.getPassword())) {
                        return ResponseEntity.ok().body(tokenProvider.generateToken(account2));
                    }
                    return ResponseEntity.badRequest().build();
                }).switchIfEmpty(Mono.just(ResponseEntity.notFound().build())));
    }

    public Mono<Void> deleteAccount(String id) {
        return accountRepository.deleteById(id);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return accountRepository.findByEmail(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("유저를 찾을 수 없습니다.")))
                .map(account -> new User(account.getUsername(), account.getPassword(), authorities(account)));
    }

    private Collection<? extends GrantedAuthority> authorities(Account account) {
        return account.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toList());
    }


    public Mono<Account> findCurrentUser() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findByEmail(principal).switchIfEmpty(Mono.empty());
    }
}
