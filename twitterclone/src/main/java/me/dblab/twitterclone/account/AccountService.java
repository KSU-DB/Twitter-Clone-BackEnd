package me.dblab.twitterclone.account;

import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class AccountService implements ReactiveUserDetailsService {

    private final AccountRepository accountRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AccountService(AccountRepository accountRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    Mono<Account> getAccount(String id) {
        return accountRepository.findById(id);
    }

    Mono<ResponseEntity> saveAccount(AccountDto accountDto) {
        return Mono.just(accountDto).map(user -> {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Collections.singletonList(Role.USER));
            return user;
        })
                .map(user -> modelMapper.map(user, Account.class))
                .flatMap(accountRepository::save)
                .map(acc -> new ResponseEntity<>("{}", HttpStatus.CREATED));
    }

    Mono<ResponseEntity> updateAccount(String id, Mono<Account> account) {
        return account.flatMap(updateAcc ->
                accountRepository.findById(id)
                        .flatMap(modiAcc -> {
                            modiAcc.update(updateAcc);
                            return accountRepository.save(modiAcc);
                        }).map(modiAcc -> new ResponseEntity<>(modiAcc, HttpStatus.OK))
                        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())));
    }

    public Mono<ResponseEntity> login(Mono<Account> account) {
        return account.flatMap(account1 -> accountRepository.findByEmail(account1.getEmail())
                .map(account2 -> {
                    if (passwordEncoder.matches(account1.getPassword(), account2.getPassword())) {
                        return ResponseEntity.ok().body(tokenProvider.generateToken(account2));
                    }
                    return ResponseEntity.badRequest().build();
                }).switchIfEmpty(Mono.just(ResponseEntity.notFound().build())));
    }

    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return accountRepository.findByEmail(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("유저를 찾을 수 없습니다.")))
                .map(account -> new User(account.getUsername(), account.getPassword(), authorities()));
    }

    private Collection<? extends GrantedAuthority> authorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("USER"));
        return grantedAuthorities;
    }
}
