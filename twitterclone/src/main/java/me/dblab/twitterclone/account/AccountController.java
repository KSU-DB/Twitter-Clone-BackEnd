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
        if(this.validate(accountDto))
            return Mono.just(ResponseEntity.badRequest().build());
        else
            return accountService.saveAccount(accountDto);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Jwt>> login(@RequestBody Account account) {
        return accountService.login(account);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity> updateAccount(@PathVariable String id, @RequestBody AccountDto accountDto) {
        if(this.validate(accountDto))
            return Mono.just(ResponseEntity.badRequest().build());
        else
            return accountService.updateAccount(id, accountDto);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAccount(@PathVariable String id)  {
        return accountService.deleteAccount(id);
    }

    private boolean validate(AccountDto accountDto) {
        Errors errors = new BeanPropertyBindingResult(accountDto, "Account");
        this.accountValidator.validate(accountDto, errors);
        if (errors.hasErrors()) {
            return true;
        }
        return false;
    }
}

