package me.dblab.twitterclone.account;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.config.jwt.Jwt;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
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
        return Mono.just(accountDto)
                .map(user -> modelMapper.map(setAccount(user), Account.class))
                .flatMap(user -> accountRepository.findByEmail(user.getEmail())
                .map(dupUser -> ResponseEntity.badRequest().build())
                .switchIfEmpty(accountRepository.save(user)
                        .map(saveUser -> new ResponseEntity<>(saveUser, HttpStatus.CREATED))));
    }

    public Mono<ResponseEntity<Jwt>> login(AccountDto accountDto) {
        return Mono.just(accountDto)
                .flatMap(account1 -> accountRepository.findByEmail(accountDto.getEmail()))
                .filter(account1 -> passwordEncoder.matches(accountDto.getPassword(), account1.getPassword()))
                .map(account1 -> new ResponseEntity<>(new Jwt(tokenProvider.generateToken(account1)), HttpStatus.OK))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    Mono<ResponseEntity> updateAccount(String id, AccountDto accountDto) {
        return Mono.just(accountDto)
                .flatMap(updatedUser -> accountRepository.findById(id)
                        .map(user -> {
                            updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                            user.update(modelMapper.map(updatedUser, Account.class));
                            return user;
                        })
                        .flatMap(accountRepository::save)
                        .map(res -> new ResponseEntity<>(res, HttpStatus.OK))
                        .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()))
                );
    }

    public Mono<ResponseEntity<Void>> deleteAccount(String id) {
        return findCurrentUser()
                .filter(account -> account.getId().equals(id))
                .flatMap(account -> accountRepository.delete(account).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
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
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> accountRepository.findByEmail((String) securityContext.getAuthentication().getPrincipal()))
                .switchIfEmpty(Mono.empty());
    }

    private AccountDto setAccount(AccountDto user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedDate(LocalDateTime.now());
        user.setRoles(Collections.singletonList(Role.USER));

        return user;
    }
}
