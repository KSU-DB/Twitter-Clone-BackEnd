package me.dblab.twitterclone.account;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.config.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AccountController {

    private final AccountService accountService;
    private final AccountValidator accountValidator;

    @GetMapping("/{id}")
    public Mono<Account> getAccount(@PathVariable String id) {
        return accountService.getAccount(id);
    }

    @PostMapping
    public Mono<ResponseEntity> saveAccount(@RequestBody AccountDto accountDto)  {
        return Mono.just(accountDto)
                .filter(this::validate)
                .flatMap(accountService::saveAccount)
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Jwt>> login(@RequestBody Account account) {
        return accountService.login(account);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity> updateAccount(@PathVariable String id, @RequestBody AccountDto accountDto) {
        return Mono.just(accountDto)
                .filter(this::validate)
                .flatMap(accountDto1 -> accountService.updateAccount(id, accountDto1))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAccount(@PathVariable String id)  {
        return accountService.deleteAccount(id);
    }

    private boolean validate(AccountDto accountDto) {
        Errors errors = new BeanPropertyBindingResult(accountDto, "Account");
        this.accountValidator.validate(accountDto, errors);
        if (errors.hasErrors()) {
            return false;
        }
        return true;
    }
}

