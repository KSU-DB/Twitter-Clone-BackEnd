package me.dblab.twitterclone.account;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.modelmapper.ModelMapper;
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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements ReactiveUserDetailsService {

    private final AccountRepository accountRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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

    public Mono<ResponseEntity> login(Account account) {
        return Mono.just(account)
                .flatMap(account1 -> accountRepository.findByEmail(account1.getEmail())
                        .map(account2 -> {
                            if (passwordEncoder.matches(account1.getPassword(), account2.getPassword())) {
                                return ResponseEntity.ok().body(tokenProvider.generateToken(account2));
                            }
                            return ResponseEntity.badRequest().build();
                        }).switchIfEmpty(Mono.just(ResponseEntity.badRequest().build())));
    }

    Mono<ResponseEntity> updateAccount(String id, AccountDto accountDto) {
        return Mono.just(accountDto)
                .flatMap(updatedUser -> accountRepository.findById(id)
                        .map(user -> {
                            user.update(modelMapper.map(updatedUser, Account.class));
                            return user;
                        })
                        .flatMap(accountRepository::save)
                        .map(res -> new ResponseEntity<>("{}", HttpStatus.OK))
                        .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("NOT EXIST!")))
                );
    }

    public Mono<ResponseEntity> deleteAccount(String id) {
        return findCurrentUser().flatMap(account -> {
            if (account.getId().equals(id)) {
                return accountRepository.deleteById(id).map(del -> ResponseEntity.ok().build());
            }
            return Mono.just(ResponseEntity.badRequest().build());
        });
    }

    public Mono<Account> isExistByEmail(String email) {
        return accountRepository.findByEmail(email);
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
